package com.example.lifesync.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifesync.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnSignin;
    TextView tvSignup, tvForgotPassword;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //firebase auth initialize
        auth = FirebaseAuth.getInstance();
        //initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignin = findViewById(R.id.btnSignin);
        tvSignup = findViewById(R.id.tvSignup);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        //Login button
        btnSignin.setOnClickListener(v -> loginUser());
        //Signup button
        tvSignup.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, SignupActivity2.class);
            startActivity(i);
            finish();
        });
        //Forgot password
        tvForgotPassword.setOnClickListener(v -> resetPassword());

    }

    // Login
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Forgot password
    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Reset link sent to email", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Error :" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //Auto Login
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}