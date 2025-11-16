package com.example.prototype;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnSignUp;
    private static final long THIRTY_DAYS_IN_MILLIS = 30L * 24 * 60 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for existing session before setting the content view
        if (isSessionValid()) {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish(); // Finish LoginActivity so user can't navigate back to it
            return; // Stop further execution of this method
        }

        setContentView(R.layout.login); // your login XML

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        MyDatabaseHelper myDB = MyDatabaseHelper.getInstance(this);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = myDB.checkUser(email, password);
            if(userId != -1) {
                // Save the user ID and last active time to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences(DashboardActivity.USER_PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(DashboardActivity.USER_ID_KEY, userId);
                editor.putLong(DashboardActivity.LAST_ACTIVE_TIME_KEY, System.currentTimeMillis());
                editor.apply();

                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                intent.putExtra(DashboardActivity.USER_ID_KEY, userId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });

        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });
    }

    private boolean isSessionValid() {
        SharedPreferences prefs = getSharedPreferences(DashboardActivity.USER_PREFS_NAME, MODE_PRIVATE);
        int userId = prefs.getInt(DashboardActivity.USER_ID_KEY, -1);
        long lastActiveTime = prefs.getLong(DashboardActivity.LAST_ACTIVE_TIME_KEY, 0);

        if (userId == -1 || lastActiveTime == 0) {
            return false; // No user ID or timestamp means not logged in
        }

        long currentTime = System.currentTimeMillis();
        // Check if the session has expired
        if (currentTime - lastActiveTime > THIRTY_DAYS_IN_MILLIS) {
            // Session expired, clear it
            prefs.edit().clear().apply();
            return false;
        }

        return true; // Session is valid
    }
}
