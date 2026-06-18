package com.vansh.smartparkin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private TextView tvAvailableSpots, tvWelcome;
    private MaterialCardView cardPark, cardUnpark, cardSpots, cardHistory;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        tvAvailableSpots = findViewById(R.id.tvAvailableSpots);
        tvWelcome = findViewById(R.id.tvWelcome);
        cardPark = findViewById(R.id.cardPark);
        cardUnpark = findViewById(R.id.cardUnpark);
        cardSpots = findViewById(R.id.cardSpots);
        cardHistory = findViewById(R.id.cardHistory);

        // Get role from intent
        String role = getIntent().getStringExtra("ROLE");
        if (role != null) {
            tvWelcome.setText("Logged in as " + role);
        }

        updateSpotCount();

        // Setup Navigation
        cardPark.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ParkActivity.class));
        });

        cardUnpark.setOnClickListener(v -> {
             startActivity(new Intent(MainActivity.this, UnparkActivity.class));
        });

        cardSpots.setOnClickListener(v -> {
            // Intent to ViewSpotsActivity
        });

        cardHistory.setOnClickListener(v -> {
            // Intent to HistoryActivity
        });
    }

    private void updateSpotCount() {
        android.database.Cursor carSpots = dbHelper.getAvailableSpots("Car");
        android.database.Cursor bikeSpots = dbHelper.getAvailableSpots("Bike");
        int totalAvailable = carSpots.getCount() + bikeSpots.getCount();
        tvAvailableSpots.setText(String.valueOf(totalAvailable));
        carSpots.close();
        bikeSpots.close();
    }
}
