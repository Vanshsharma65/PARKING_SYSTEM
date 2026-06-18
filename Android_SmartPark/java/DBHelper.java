package com.vansh.smartparkin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartPark.db";
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Spots Table
        db.execSQL("CREATE TABLE spots (id INTEGER PRIMARY KEY, type TEXT, is_occupied INTEGER DEFAULT 0)");
        
        // Create Tickets Table
        db.execSQL("CREATE TABLE tickets (id INTEGER PRIMARY KEY AUTOINCREMENT, license_plate TEXT, spot_id INTEGER, " +
                "entry_time TEXT, exit_time TEXT, fee REAL, customer_email TEXT)");

        // Create Users Table
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, email TEXT, " +
                "password TEXT, role TEXT DEFAULT 'user')");

        // Initialize Spots
        initializeSpots(db);
        
        // Add a default admin
        ContentValues adminValues = new ContentValues();
        adminValues.put("username", "admin");
        adminValues.put("password", "admin123");
        adminValues.put("role", "admin");
        db.insert("users", null, adminValues);
    }

    private void initializeSpots(SQLiteDatabase db) {
        for (int i = 1; i <= 5; i++) {
            ContentValues values = new ContentValues();
            values.put("id", i);
            values.put("type", "Car");
            db.insert("spots", null, values);
        }
        for (int i = 6; i <= 10; i++) {
            ContentValues values = new ContentValues();
            values.put("id", i);
            values.put("type", "Bike");
            db.insert("spots", null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS spots");
        db.execSQL("DROP TABLE IF EXISTS tickets");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // --- Authentication Logic ---

    public boolean registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("password", password);
        values.put("role", "user");
        long result = db.insert("users", null, values);
        return result != -1;
    }

    // --- Parking Case Logic ---

    public Cursor getAvailableSpots(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id FROM spots WHERE type=? AND is_occupied=0", new String[]{type});
    }

    public long parkVehicle(String plate, int spotId, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Mark spot as occupied
            ContentValues spotValues = new ContentValues();
            spotValues.put("is_occupied", 1);
            db.update("spots", spotValues, "id=?", new String[]{String.valueOf(spotId)});

            // Create ticket
            ContentValues ticketValues = new ContentValues();
            ticketValues.put("license_plate", plate);
            ticketValues.put("spot_id", spotId);
            ticketValues.put("entry_time", java.time.LocalDateTime.now().toString());
            ticketValues.put("customer_email", email);
            long ticketId = db.insert("tickets", null, ticketValues);

            db.setTransactionSuccessful();
            return ticketId;
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getActiveTicket(String plate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM tickets WHERE license_plate=? AND exit_time IS NULL", new String[]{plate});
    }

    public void processExit(long ticketId, int spotId, double fee) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Update ticket
            ContentValues ticketValues = new ContentValues();
            ticketValues.put("exit_time", java.time.LocalDateTime.now().toString());
            ticketValues.put("fee", fee);
            db.update("tickets", ticketValues, "id=?", new String[]{String.valueOf(ticketId)});

            // Free the spot
            ContentValues spotValues = new ContentValues();
            spotValues.put("is_occupied", 0);
            db.update("spots", spotValues, "id=?", new String[]{String.valueOf(spotId)});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public double getTotalRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(fee) FROM tickets", null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }
}
