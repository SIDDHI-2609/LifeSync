package com.example.lifesync.activities.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.lifesync.R;
import com.google.android.material.card.MaterialCardView;

public class ThemeActivity extends BaseActivity {

    private MaterialCardView cardLightMode, cardDarkMode;
    private TextView checkLightMode, checkDarkMode;
    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prefs = getSharedPreferences("SmartAssistantPrefs", MODE_PRIVATE);

        boolean dark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
             setupToolbar();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        cardLightMode = findViewById(R.id.cardLightMode);
        cardDarkMode = findViewById(R.id.cardDarkMode);
        checkLightMode = findViewById(R.id.checkLightMode);
        checkDarkMode = findViewById(R.id.checkDarkMode);

        updateChecks();

        cardLightMode.setOnClickListener(v -> applyTheme(false));
        cardDarkMode.setOnClickListener(v -> applyTheme(true));
    }

    private void applyTheme(boolean dark) {
        prefs.edit().putBoolean("dark_mode", dark).apply();

        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);

        updateChecks();

        Toast.makeText(this,
                dark ? "Dark Mode Enabled" : "Light Mode Enabled",
                Toast.LENGTH_SHORT).show();

        recreate();
    }

    private void updateChecks() {
        boolean dark = prefs.getBoolean("dark_mode", false);
        checkDarkMode.setVisibility(dark ? android.view.View.VISIBLE : android.view.View.INVISIBLE);
        checkLightMode.setVisibility(dark ? android.view.View.INVISIBLE : android.view.View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    }