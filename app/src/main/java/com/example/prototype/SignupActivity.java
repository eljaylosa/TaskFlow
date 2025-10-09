package com.example.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    EditText etFullName, etPhone, etEmail, etPassword;
    Button btnCreateAccount, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up); // <-- replace with your signup XML file name

        // Initialize
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnLogin = findViewById(R.id.btnLogin);

        // Create Account Button
        btnCreateAccount.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Later connect this to Firebase or SQLite
                MyDatabaseHelper myDB = new MyDatabaseHelper(SignupActivity.this);
                myDB.insertUser(etFullName.getText().toString().trim(), etPhone.getText().toString().trim(), etEmail.getText().toString().trim(), etPassword.getText().toString().trim());
                Toast.makeText(SignupActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();

                // Redirect to Login screen
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Already have account? -> Go back to Login
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
