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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity2 extends AppCompatActivity {
    EditText etName, etMobile, etEmail, etPassword, etConfirmPassword;
    Button btnRegister;
    TextView tvLogin;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);
        auth = FirebaseAuth.getInstance();
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        //Register Button
        btnRegister.setOnClickListener(v -> registerUser());

        //Go to Login
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity2.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }
    private void registerUser() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        //validation
        if(TextUtils.isEmpty(name)){
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(mobile)){
            etMobile.setError("Mobile is required");
            etMobile.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(email)){
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(password)){
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if(password.length() < 6){
            etPassword.setError("Password must be at least 6+ characters");
            etPassword.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(confirmPassword)){
            etConfirmPassword.setError("Confirm Password is required");
            etConfirmPassword.requestFocus();
            return;
        }
        if(!password.equals(confirmPassword)){
            etConfirmPassword.setError("Password do not match");
            etConfirmPassword.requestFocus();
            return;
        }
        //firebase signup
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                FirebaseUser user = auth.getCurrentUser();
                String uid = user.getUid();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                //create user data map
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("name", name);
                userMap.put("mobile", mobile);
                userMap.put("email", email);

                //save user data to firestore
                db.collection("users").document(uid).set(userMap).addOnSuccessListener(unused -> {
                            Toast.makeText(this, "User Saved Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity2.this, LoginActivity.class));
                            finish();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
            else{
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //Auto Login
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(SignupActivity2.this, DashboardActivity.class));
            finish();
        }
    }
}
