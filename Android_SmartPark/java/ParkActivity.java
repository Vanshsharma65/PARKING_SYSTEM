package com.vansh.smartparkin;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ParkActivity extends AppCompatActivity {

    private RadioGroup rgType;
    private EditText etPlate, etEmail;
    private Button btnPark;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park);

        dbHelper = new DBHelper(this);

        rgType = findViewById(R.id.rgType);
        etPlate = findViewById(R.id.etPlate);
        etEmail = findViewById(R.id.etEmail);
        btnPark = findViewById(R.id.btnPark);

        btnPark.setOnClickListener(v -> performParking());
    }

    private void performParking() {
        int selectedId = rgType.getCheckedRadioButtonId();
        RadioButton selectedButton = findViewById(selectedId);
        String type = selectedButton.getText().toString();
        
        String plate = etPlate.getText().toString().trim().toUpperCase();
        String email = etEmail.getText().toString().trim();

        if (plate.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Get available spots
        Cursor cursor = dbHelper.getAvailableSpots(type);
        if (cursor.moveToFirst()) {
            int spotId = cursor.getInt(0);
            cursor.close();

            // 2. Park vehicle
            long ticketId = dbHelper.parkVehicle(plate, spotId, email);
            if (ticketId != -1) {
                Toast.makeText(this, "Parked at Spot #" + spotId + "! Ticket: " + ticketId, Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Parking failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No spots available for " + type, Toast.LENGTH_SHORT).show();
            cursor.close();
        }
    }
}
