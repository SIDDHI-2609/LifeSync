package com.example.lifesync.activities.activities;

import android.app.AlarmManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lifesync.R;

public class DashboardActivity extends AppCompatActivity {
    Button btnNotes, btnTodo, btnExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                    new String[]{"android.permission.POST_NOTIFICATIONS"},
                    1
            );
        }

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {

            startActivity(
                    new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            );
        }

        btnNotes = findViewById(R.id.button);
        btnTodo = findViewById(R.id.button2);
        btnExpense = findViewById(R.id.button3);

        btnNotes.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, NotesActivity.class));
        });

        btnTodo.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, TodoActivity.class));
        });

        btnExpense.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, ExpenseActivity.class));
        });

    }
}