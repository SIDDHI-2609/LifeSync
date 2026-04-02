package com.example.lifesync.activities.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifesync.R;
import com.example.lifesync.activities.services.AlarmService;

public class AlarmActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        //show over lock screen
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );

        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
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

        Intent intent =
                new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        startActivity(intent);

    }

    private void stopAlarm() {
        stopService(new Intent(this, AlarmService.class));

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
//        intent.putExtra("title", title);
        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                snoozeTime,
                pendingIntent
        );
        stopAlarm();

    }
}