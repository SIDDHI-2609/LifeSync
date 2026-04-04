package com.example.lifesync.activities.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lifesync.activities.activities.AlarmFullScreenActivity;
import com.example.lifesync.activities.activities.TodoActivity;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG        = "AlarmReceiver";
    public static final  String CHANNEL_ID = "todo_alarm_channel";
    public static final  String EXTRA_TITLE = "task_title";
    public static final  String EXTRA_ID    = "task_id";

    // Action for "Stop" button inside the notification
    public static final String ACTION_STOP_FROM_NOTIF = "com.yourapp.todo.STOP_FROM_NOTIF";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive fired");

        // ── WakeLock: keep CPU awake for entire method ────────────────────────
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "TodoApp:AlarmWakeLock");
        wl.acquire(15_000L);

        try {
            int    taskId = intent.getIntExtra(EXTRA_ID, -1);
            String title  = intent.getStringExtra(EXTRA_TITLE);
            if (title == null) title = "Task Reminder";

            // ── Completion guard ──────────────────────────────────────────────
            boolean done = context
                    .getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
                    .getBoolean("task_completed_" + taskId, false);
            if (done) { Log.d(TAG, "Task done, skip"); return; }

            createChannel(context);
            vibrate(context);

            // ── Launch full-screen activity (lock screen / screen-off) ────────
            Intent fullScreenIntent = new Intent(context, AlarmFullScreenActivity.class);
            fullScreenIntent.putExtra(AlarmFullScreenActivity.EXTRA_ID, taskId);
            fullScreenIntent.putExtra(AlarmFullScreenActivity.EXTRA_TITLE, title);
            fullScreenIntent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_NO_USER_ACTION);

            PendingIntent fullScreenPi = PendingIntent.getActivity(
                    context, taskId, fullScreenIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // ── "Open app" tap on notification body ───────────────────────────
            Intent openApp = new Intent(context, TodoActivity.class);
            openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent openPi = PendingIntent.getActivity(
                    context, taskId + 1000, openApp,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // ── "Stop" action button on the notification ──────────────────────
            Intent stopIntent = new Intent(context, StopAlarmReceiver.class);
            stopIntent.putExtra(EXTRA_ID, taskId);
            PendingIntent stopPi = PendingIntent.getBroadcast(
                    context, taskId + 2000, stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // ── "Snooze" action button on the notification ────────────────────
            Intent snoozeIntent = new Intent(context, SnoozeAlarmReceiver.class);
            snoozeIntent.putExtra(EXTRA_ID, taskId);
            snoozeIntent.putExtra(EXTRA_TITLE, title);
            PendingIntent snoozePi = PendingIntent.getBroadcast(
                    context, taskId + 3000, snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // ── Build notification ────────────────────────────────────────────
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                            .setContentTitle("⏰ Task Reminder")
                            .setContentText(title)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setCategory(NotificationCompat.CATEGORY_ALARM)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setOngoing(true)                     // can't be swiped away
                            .setAutoCancel(false)
                            .setContentIntent(openPi)             // tap → opens TodoActivity
                            .setFullScreenIntent(fullScreenPi, true) // fires fullscreen on lock screen
                            // Action buttons visible in the notification shade
                            .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                                    "Stop", stopPi)
                            .addAction(android.R.drawable.ic_popup_reminder,
                                    "Snooze 5m", snoozePi);

            NotificationManagerCompat mgr = NotificationManagerCompat.from(context);
            if (!mgr.areNotificationsEnabled()) {
                Log.e(TAG, "Notifications disabled!");
                return;
            }
            mgr.notify(taskId, builder.build());
            Log.d(TAG, "Notification posted id=" + taskId);

        } finally {
            if (wl.isHeld()) wl.release();
        }
    }

    // ── Notification channel ──────────────────────────────────────────────────

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mgr =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mgr == null) return;

            // Recreate channel so audio attributes always apply
            mgr.deleteNotificationChannel(CHANNEL_ID);

            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "To-Do Alarms", NotificationManager.IMPORTANCE_HIGH);
            ch.setSound(sound, aa);
            ch.enableVibration(true);
            ch.setVibrationPattern(new long[]{0, 500, 200, 500});
            ch.setBypassDnd(true);
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mgr.createNotificationChannel(ch);
        }
    }

    // ── Vibrate ───────────────────────────────────────────────────────────────

    private void vibrate(Context context) {
        long[] pattern = {0, 500, 200, 500};
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm =
                        (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null)
                    vm.getDefaultVibrator().vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) v.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) v.vibrate(pattern, -1);
            }
        } catch (Exception e) {
            Log.e(TAG, "vibrate: " + e.getMessage());
        }
    }
}