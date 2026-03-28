package com.example.lifesync.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.lifesync.R;
import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.models.User;

public class SignupActivity extends AppCompatActivity {
    EditText name, Email, Password, ConfirmPassword, Mobile;
    Button SignUpbtn;
    AppDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

         name = findViewById(R.id.etName);
         Email = findViewById(R.id.etEmail);
         Password = findViewById(R.id.etPassword);
         ConfirmPassword = findViewById(R.id.etConfirmPassword);
         Mobile = findViewById(R.id.etMobile);
         SignUpbtn = findViewById(R.id.btnRegister);

         db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class, "lifesync-db"
        ).allowMainThreadQueries().build();
        SignUpbtn.setOnClickListener(v -> registerUser());
    }
    private void registerUser(){
        String userName = name.getText().toString().trim();
        String userEmail = Email.getText().toString().trim();
        String userPass = Password.getText().toString().trim();
        String confirmPass = ConfirmPassword.getText().toString().trim();
        String mobile = Mobile.getText().toString().trim();

        //validation
        if (userName.isEmpty() || userEmail.isEmpty() || userPass.isEmpty()
        || confirmPass.isEmpty() || mobile.isEmpty()){
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        //pass = confirm pass
        if(!userPass.equals(confirmPass)){
            Toast.makeText(this, "Password do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        //check user exists or not
        User existingUser = db.userDao().getUserByEmail(userEmail);
        if (existingUser != null){
            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        //New user entry
        User user = new User();
        user.email = userEmail;
        user.password = userPass;
        user.mobile = mobile;
        user.name = userName;

        try {
            db.userDao().insert(user);
            Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Intent i = new Intent(SignupActivity.this, DashboardActivity.class);
        startActivity(i);
        finish();
    }
}