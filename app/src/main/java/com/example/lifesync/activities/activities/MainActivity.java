package com.example.lifesync.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.lifesync.R;
import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.models.User;

public class MainActivity extends AppCompatActivity {

    EditText Email, Password;
    TextView tvSignUp;
    Button btnLogin;
    AppDatabase db;
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);

        Email = findViewById(R.id.etEmail);
        Password = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnSignin);
        tvSignUp = findViewById(R.id.tvSignup);

        //database connectivity
        db = Room.databaseBuilder(
                getApplicationContext(), AppDatabase.class,"lifesync-db"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build();
        btnLogin.setOnClickListener(v-> loginUser());

        tvSignUp.setOnClickListener(v->{
            Intent i = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void loginUser(){
          String email = Email.getText().toString().trim();
          String password = Password.getText().toString().trim();

          if(email.isEmpty() || password.isEmpty()){
              Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
              return;
          }

          //check user is in database
        try {
            User user = db.userDao().login(email, password);
            if (user != null) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }
}