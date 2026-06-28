package com.example.lifesync.activities.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import android.view.MenuItem;
import android.view.Menu;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.lifesync.R;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle s) {

        SharedPreferences prefs =
                getSharedPreferences("SmartAssistantPrefs", MODE_PRIVATE);

        boolean isDarkMode = prefs.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                isDarkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(s);
    }
    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            Toast.makeText(this, "Opening Profile...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ThemeActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_settings) {
            Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_logout) {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
