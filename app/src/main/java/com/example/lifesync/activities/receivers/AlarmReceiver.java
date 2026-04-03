package com.example.lifesync.activities.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lifesync.activities.activities.TodoActivity;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG          = "AlarmReceiver";
    public static final  String CHANNEL_ID   = "todo_alarm_channel";
    public static final  String EXTRA_TITLE  = "task_title";
    public static final  String EXTRA_ID     = "task_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive fired"); // confirm receiver is triggered

        // ── Acquire WakeLock so CPU stays awake during processing ─────────────
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "TodoApp:AlarmWakeLock");
        wakeLock.acquire(10_000L); // hold for max 10 seconds

        try {
            int    taskId = intent.getIntExtra(EXTRA_ID, -1);
            String title  = intent.getStringExtra(EXTRA_TITLE);
            if (title == null) title = "Task Reminder";

            boolean done = context
                    .getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
                    .getBoolean("task_completed_" + taskId, false);
            if (done) {
                Log.d(TAG, "Task already completed, skipping");
                return;
            }

            createChannel(context);
            playSound(context);
            vibrate(context);
            showNotification(context, title, taskId);

        } finally {
            if (wakeLock.isHeld()) wakeLock.release();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mgr =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mgr == null) return;

            // Delete old channel if exists so audio attributes update properly
            mgr.deleteNotificationChannel(CHANNEL_ID);

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            AudioAttributes audioAttr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "To-Do Alarms",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Task alarm reminders");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setSound(soundUri, audioAttr);
            channel.setBypassDnd(true);           // bypass Do Not Disturb

            mgr.createNotificationChannel(channel);
        }
    }

    private void playSound(Context context) {
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null)
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (uri == null)
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.setLooping(false);   // play once
                }
                ringtone.play();
                Log.d(TAG, "Ringtone playing: " + uri);
            }
        } catch (Exception e) {
            Log.e(TAG, "playSound error: " + e.getMessage());
        }
    }

    private void vibrate(Context context) {
        try {
            long[] pattern = {0, 500, 200, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm =
                        (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) {
                    vm.getDefaultVibrator().vibrate(
                            VibrationEffect.createWaveform(pattern, -1));
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null)
                    v.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) v.vibrate(pattern, -1);
            }
        } catch (Exception e) {
            Log.e(TAG, "vibrate error: " + e.getMessage());
        }
    }

    private void showNotification(Context context, String title, int taskId) {
        Intent tap = new Intent(context, TodoActivity.class);
        tap.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                context, taskId, tap,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // built-in icon — no drawable needed
                        .setContentTitle("⏰ Task Reminder")
                        .setContentText(title)
                        .setPriority(NotificationCompat.PRIORITY_MAX)        // MAX not just HIGH
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // show on lock screen
                        .setAutoCancel(true)
                        .setContentIntent(pi)
                        .setFullScreenIntent(pi, true);                      // heads-up popup

        NotificationManagerCompat mgr = NotificationManagerCompat.from(context);

        // NotificationManagerCompat.areNotificationsEnabled() guards Android 13+ permission
        if (!mgr.areNotificationsEnabled()) {
            Log.e(TAG, "Notifications are DISABLED for this app — go to App Settings and enable them");
            return;
        }

        mgr.notify(taskId, builder.build());
        Log.d(TAG, "Notification posted for taskId=" + taskId);
    }
}