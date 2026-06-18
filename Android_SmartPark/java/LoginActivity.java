package com.vansh.smartparkin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etIdentifier, etPassword;
    private Button btnLogin;
    private TextView tvSignupLink, tvAdminLogin;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);

        etIdentifier = findViewById(R.id.etIdentifier);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignupLink = findViewById(R.id.tvSignupLink);
        tvAdminLogin = findViewById(R.id.tvAdminLogin);

        btnLogin.setOnClickListener(v -> performLogin());

        tvSignupLink.setOnClickListener(v -> {
            // Navigate to Signup Activity
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        tvAdminLogin.setOnClickListener(v -> {
            // Quick Admin Login logic or show a specific dialog
            etIdentifier.setText("admin");
            Toast.makeText(this, "Enter admin password to continue", Toast.LENGTH_SHORT).show();
        });
    }

    private void performLogin() {
        String identifier = etIdentifier.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = dbHelper.authenticateUser(identifier, password);

        if (role != null) {
            Toast.makeText(this, "Login Successful as " + role, Toast.LENGTH_SHORT).show();
            
            // Navigate based on role
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("ROLE", role);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
