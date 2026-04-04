package com.example.lifesync.activities.activities;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifesync.R;
import com.example.lifesync.activities.helper.AlarmHelper;
import com.google.android.material.button.MaterialButton;

public class AlarmFullScreenActivity extends AppCompatActivity {

    public static final String ACTION_STOP_ALARM  = "com.yourapp.todo.STOP_ALARM";
    public static final String ACTION_SNOOZE_ALARM = "com.yourapp.todo.SNOOZE_ALARM";
    public static final String EXTRA_TITLE         = "task_title";
    public static final String EXTRA_ID            = "task_id";

    public static final int    SNOOZE_MINUTES      = 5;

    private Ringtone ringtone;
    private int      taskId;
    private String   taskTitle;

    // Receiver to stop this activity when STOP/SNOOZE broadcast arrives
    private final BroadcastReceiver dismissReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_STOP_ALARM.equals(intent.getAction())
                    || ACTION_SNOOZE_ALARM.equals(intent.getAction())) {
                stopRingtoneAndFinish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── Make activity show on lock screen & turn screen on ────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = getSystemService(KeyguardManager.class);
            if (km != null) km.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        setContentView(R.layout.activity_alarm_full_screen);

        taskId    = getIntent().getIntExtra(EXTRA_ID, -1);
        taskTitle = getIntent().getStringExtra(EXTRA_TITLE);
        if (taskTitle == null) taskTitle = "Task Reminder";

        // Bind views
        TextView tvTitle = findViewById(R.id.tvAlarmTitle);
        tvTitle.setText(taskTitle);

        MaterialButton btnStop   = findViewById(R.id.btnStopAlarm);
        MaterialButton btnSnooze = findViewById(R.id.btnSnoozeAlarm);

        btnStop.setOnClickListener(v -> stopAlarm());
        btnSnooze.setOnClickListener(v -> snoozeAlarm());

        // Register dismiss receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP_ALARM);
        filter.addAction(ACTION_SNOOZE_ALARM);
        registerReceiver(dismissReceiver, filter);

        // Start playing ringtone
        playRingtone();
    }

    // ── Ringtone ──────────────────────────────────────────────────────────────

    private void playRingtone() {
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, uri);
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ringtone.setLooping(true);
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRingtoneAndFinish() {
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
        finish();
    }

    // ── Stop ──────────────────────────────────────────────────────────────────

    private void stopAlarm() {
        // Mark notification dismissed
        AlarmHelper.cancelAlarm(this, taskId);
        stopRingtoneAndFinish();

        // Open TodoActivity
        Intent open = new Intent(this, TodoActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(open);
    }

    // ── Snooze ────────────────────────────────────────────────────────────────

    private void snoozeAlarm() {
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();

        long snoozeTime = System.currentTimeMillis() + (SNOOZE_MINUTES * 60 * 1000L);
        AlarmHelper.setAlarm(this, taskId, taskTitle, snoozeTime);

        android.widget.Toast.makeText(this,
                "Snoozed for " + SNOOZE_MINUTES + " minutes", android.widget.Toast.LENGTH_SHORT).show();

        // Small delay so toast shows before finishing
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 800);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(dismissReceiver); } catch (Exception ignored) {}
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
    }

    // Prevent back button from dismissing without stopping ringtone
    @Override
    public void onBackPressed() {
        // Do nothing — user must tap Stop or Snooze
    }
}