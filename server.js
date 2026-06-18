const express = require('express');
const cors = require('cors');
const nodemailer = require('nodemailer');
const db = require('./database');

const app = express();
app.use(cors());
app.use(express.json());

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'vanshsharmagraphic44@gmail.com',
        pass: 'ftqaxhawknrvatmt'
    }
});

// Helper to send email
const sendEmail = (to, subject, text) => {
    const mailOptions = {
        from: 'vanshsharmagraphic44@gmail.com',
        to,
        subject,
        text
    };
    transporter.sendMail(mailOptions, (error, info) => {
        if (error) console.log('Error sending email:', error);
        else console.log('Email sent:', info.response);
    });
};

// 1. Auth APIs
app.post('/api/auth/send-otp', (req, res) => {
    const { email, mobile, type } = req.body;
    
    // Check if user exists
    db.get('SELECT * FROM users WHERE email = ? AND mobile = ?', [email, mobile], (err, user) => {
        if (err) return res.status(500).json({ error: 'Database error' });
        
        if (type === 'login' && !user) {
            return res.status(404).json({ error: 'User not found. Please register.' });
        }
        
        const otp = Math.floor(1000 + Math.random() * 9000).toString(); // 4-digit OTP
        
        db.run('INSERT INTO otps (email, code) VALUES (?, ?)', [email, otp], (err) => {
            if (err) return res.status(500).json({ error: 'Failed to generate OTP' });
            
            sendEmail(email, 'Your Smart Parking OTP', `Your OTP is: ${otp}`);
            res.json({ message: 'OTP sent successfully' });
        });
    });
});

app.post('/api/auth/verify-otp', (req, res) => {
    const { email, mobile, otp, isRegister, username, gender } = req.body;
    
    // Check latest OTP
    db.get('SELECT * FROM otps WHERE email = ? ORDER BY createdAt DESC LIMIT 1', [email], (err, row) => {
        if (err || !row) return res.status(400).json({ error: 'OTP not found' });
        
        if (row.code !== otp) return res.status(400).json({ error: 'Invalid OTP' });
        
        if (isRegister) {
            db.run('INSERT INTO users (email, mobile, username, gender) VALUES (?, ?, ?, ?)', 
            [email, mobile, username, gender], function(err) {
                if (err) return res.status(400).json({ error: 'Registration failed. User may exist.' });
                
                db.get('SELECT * FROM users WHERE id = ?', [this.lastID], (err, newUser) => {
                    res.json({ user: newUser });
                });
            });
        } else {
            db.get('SELECT * FROM users WHERE email = ? AND mobile = ?', [email, mobile], (err, user) => {
                if (err || !user) return res.status(404).json({ error: 'User not found' });
                res.json({ user });
            });
        }
    });
});

// 2. Vehicles APIs
app.get('/api/vehicles/:userId', (req, res) => {
    db.all('SELECT * FROM vehicles WHERE userId = ?', [req.params.userId], (err, rows) => {
        res.json({ vehicles: rows || [] });
    });
});

app.post('/api/vehicles', (req, res) => {
    const { userId, type, plate } = req.body;
    
    db.get('SELECT COUNT(*) as count FROM vehicles WHERE userId = ?', [userId], (err, row) => {
        const isPrimary = row.count === 0 ? 1 : 0;
        
        db.run('INSERT INTO vehicles (userId, type, plate, isPrimary) VALUES (?, ?, ?, ?)',
        [userId, type, plate, isPrimary], function(err) {
            if (err) return res.status(400).json({ error: 'Failed to add vehicle' });
            res.json({ id: this.lastID, type, plate, isPrimary: !!isPrimary });
        });
    });
});

app.put('/api/vehicles/set-primary/:id', (req, res) => {
    const { userId } = req.body;
    db.serialize(() => {
        db.run('UPDATE vehicles SET isPrimary = 0 WHERE userId = ?', [userId]);
        db.run('UPDATE vehicles SET isPrimary = 1 WHERE id = ? AND userId = ?', [req.params.id, userId], (err) => {
            res.json({ success: true });
        });
    });
});

app.delete('/api/vehicles/:id', (req, res) => {
    db.run('DELETE FROM vehicles WHERE id = ?', [req.params.id], (err) => {
        res.json({ success: true });
    });
});

// 3. Parking APIs
app.get('/api/parking/status/:userId', (req, res) => {
    db.get('SELECT * FROM parking_sessions WHERE userId = ? AND isActive = 1', [req.params.userId], (err, session) => {
        res.json({ session: session || null });
    });
});

app.post('/api/parking/start', (req, res) => {
    const { userId, vehicleId, spotId } = req.body;
    db.run('INSERT INTO parking_sessions (userId, vehicleId, spotId) VALUES (?, ?, ?)',
    [userId, vehicleId, spotId], function(err) {
        if (err) return res.status(400).json({ error: 'Failed to start parking' });
        
        db.get('SELECT * FROM parking_sessions WHERE id = ?', [this.lastID], (err, session) => {
            // Get user to send email
            db.get('SELECT email FROM users WHERE id = ?', [userId], (err, user) => {
                if (user) sendEmail(user.email, 'Parking Started', `You have parked your vehicle at SPOT-${spotId}.`);
            });
            res.json({ session });
        });
    });
});

app.post('/api/parking/stop', (req, res) => {
    const { userId } = req.body;
    db.get('SELECT * FROM parking_sessions WHERE userId = ? AND isActive = 1', [userId], (err, session) => {
        if (!session) return res.status(400).json({ error: 'No active session' });
        
        const durationMs = new Date() - new Date(session.startTime);
        const hours = durationMs / (1000 * 60 * 60);
        const fee = hours < 1 ? 10 : Math.ceil(hours) * 20; // ₹10 flat under 1hr, ₹20/hr after
        
        db.run('UPDATE parking_sessions SET isActive = 0, endTime = CURRENT_TIMESTAMP WHERE id = ?', [session.id], (err) => {
            db.get('SELECT email FROM users WHERE id = ?', [userId], (err, user) => {
                if (user) sendEmail(user.email, 'Parking Receipt', `Your parking session has ended. Fee: Rs.${fee}. Thank you!`);
            });
            res.json({ success: true, fee });
        });
    });
});

// 4. Background Job (Check every minute for testing, could be every 5 mins in prod)
setInterval(() => {
    const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000).toISOString();
    
    // Find sessions active for more than 1 hour and email wasn't sent recently
    db.all(`SELECT ps.id, ps.startTime, ps.lastEmailTime, u.email 
            FROM parking_sessions ps 
            JOIN users u ON ps.userId = u.id 
            WHERE ps.isActive = 1 
            AND ps.lastEmailTime <= datetime('now', '-1 hour')`, (err, sessions) => {
        if (err) return;
        
        sessions.forEach(session => {
            sendEmail(session.email, 'Parking Reminder', `Your vehicle is parked from ${session.startTime} till now.`);
            db.run('UPDATE parking_sessions SET lastEmailTime = CURRENT_TIMESTAMP WHERE id = ?', [session.id]);
        });
    });
}, 60000); // Check every minute

app.listen(3000, () => {
    console.log('Backend server running on http://localhost:3000');
});
