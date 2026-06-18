const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const dbPath = path.resolve(__dirname, 'parking_app.db');
const db = new sqlite3.Database(dbPath, (err) => {
    if (err) {
        console.error('Error opening database', err);
    } else {
        console.log('Database connected.');
        db.serialize(() => {
            // Users table
            db.run(`CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE,
                mobile TEXT UNIQUE,
                username TEXT,
                gender TEXT,
                role TEXT DEFAULT 'user',
                theme TEXT DEFAULT 'dark',
                createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
            )`);

            // OTPs table
            db.run(`CREATE TABLE IF NOT EXISTS otps (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT,
                code TEXT,
                createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
            )`);

            // Vehicles table
            db.run(`CREATE TABLE IF NOT EXISTS vehicles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId INTEGER,
                type TEXT,
                plate TEXT UNIQUE,
                isPrimary BOOLEAN DEFAULT 0,
                FOREIGN KEY(userId) REFERENCES users(id)
            )`);

            // Parking Sessions table
            db.run(`CREATE TABLE IF NOT EXISTS parking_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId INTEGER,
                vehicleId INTEGER,
                spotId INTEGER,
                startTime DATETIME DEFAULT CURRENT_TIMESTAMP,
                lastEmailTime DATETIME DEFAULT CURRENT_TIMESTAMP,
                endTime DATETIME,
                isActive BOOLEAN DEFAULT 1,
                FOREIGN KEY(userId) REFERENCES users(id),
                FOREIGN KEY(vehicleId) REFERENCES vehicles(id)
            )`);

            // Insert admin if not exists
            db.get(`SELECT id FROM users WHERE email = ?`, ['admin@parking.com'], (err, row) => {
                if (!row) {
                    db.run(`INSERT INTO users (email, mobile, username, gender, role) VALUES (?, ?, ?, ?, ?)`, 
                    ['admin@parking.com', '1111111111', 'System Admin', 'Other', 'admin']);
                }
            });
        });
    }
});

module.exports = db;
