package com.vansh.smartparkin;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.time.Duration;
import java.time.LocalDateTime;

public class UnparkActivity extends AppCompatActivity {

    private EditText etPlate;
    private Button btnUnpark;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unpark);

        dbHelper = new DBHelper(this);

        etPlate = findViewById(R.id.etUnparkPlate);
        btnUnpark = findViewById(R.id.btnUnpark);

        btnUnpark.setOnClickListener(v -> processExit());
    }

    private void processExit() {
        String plate = etPlate.getText().toString().trim().toUpperCase();

        if (plate.isEmpty()) {
            Toast.makeText(this, "Enter license plate", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = dbHelper.getActiveTicket(plate);
        if (cursor.moveToFirst()) {
            long ticketId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            int spotId = cursor.getInt(cursor.getColumnIndexOrThrow("spot_id"));
            String entryTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("entry_time"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("customer_email"));

            LocalDateTime entryTime = LocalDateTime.parse(entryTimeStr);
            LocalDateTime exitTime = LocalDateTime.now();
            Duration d = Duration.between(entryTime, exitTime);
            long mins = d.toMinutes();

            double fee;
            String billed;
            if (mins < 60) {
                fee = 10.0;
                billed = "Flat Rs.10 (under 1 hour)";
            } else {
                long hrs = (long) Math.ceil(mins / 60.0);
                fee = 20.0 * hrs;
                billed = hrs + " hr(s) x Rs.20";
            }

            // Update Database
            dbHelper.processExit(ticketId, spotId, fee);

            // Show Receipt Dialog
            showReceipt(plate, entryTimeStr, exitTime.toString(), billed, fee);

        } else {
            Toast.makeText(this, "No active parking found for this plate", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    private void showReceipt(String plate, String entry, String exit, String billed, double fee) {
        new AlertDialog.Builder(this)
                .setTitle("Parking Receipt")
                .setMessage("Plate: " + plate + "\n" +
                        "Entry: " + entry + "\n" +
                        "Exit: " + exit + "\n" +
                        "Billed: " + billed + "\n" +
                        "Total Fee: Rs." + (int) fee)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
