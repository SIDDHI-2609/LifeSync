package com.example.lifesync.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lifesync.R;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button btnNotes = findViewById(R.id.button);
        Button btnTodo = findViewById(R.id.button2);
        Button btnExpense = findViewById(R.id.button3);

        btnNotes.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, NotesActivity.class);
            startActivity(i);
            finish();
        });

        btnTodo.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, TodoActivity.class);
            startActivity(i);
            finish();
        });

        btnExpense.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, ExpenseActivity.class);
            startActivity(i);
            finish();
        });



    }
}