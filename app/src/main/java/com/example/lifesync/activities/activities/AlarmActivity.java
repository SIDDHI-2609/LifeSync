package com.example.lifesync.activities.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lifesync.R;

public class AlarmActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        MediaPlayer mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        Button btnStop = findViewById(R.id.btnStop);
        Button btnSnooze = findViewById(R.id.btnSnooze);

        btnStop.setOnClickListener(v -> {
            stopAlarm();
        });

        btnSnooze.setOnClickListener(v -> {
            snoozeAlarm();
        });
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        finish();
    }

    private void snoozeAlarm() {
        long snoozeTimeInMillis = 5 * 60 * 1000;  //5 min
        long currentTime = System.currentTimeMillis();
        long snoozeTime = currentTime + snoozeTimeInMillis;

        Intent intent = new Intent(this, com.example.lifesync.activities.receivers.AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                snoozeTime,
                pendingIntent
        );
        stopAlarm();

    }
}