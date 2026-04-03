package com.example.lifesync.activities.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.lifesync.activities.receivers.AlarmReceiver;

public class AlarmHelper {

    private static final String TAG = "AlarmHelper";

    public static void setAlarm(Context context, int taskId, String taskTitle, long alarmTimeMillis) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        // ── Android 12+ exact alarm permission ───────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Auto-open the exact alarm permission settings screen
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + context.getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                Toast.makeText(context,
                        "Enable 'Alarms & Reminders' then set the alarm again",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        // ── Build the PendingIntent ───────────────────────────────────────────
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("TODO_ALARM_" + taskId);          // unique action prevents intent collision
        intent.putExtra(AlarmReceiver.EXTRA_TITLE, taskTitle);
        intent.putExtra(AlarmReceiver.EXTRA_ID, taskId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // ── Schedule exact alarm ──────────────────────────────────────────────
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                alarmTimeMillis,
                pendingIntent   // shown when user taps the status bar clock icon
        );
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);

        Log.d(TAG, "Alarm set via setAlarmClock: taskId=" + taskId + " at " + alarmTimeMillis);
        Toast.makeText(context, "✅ Alarm set: " + taskTitle, Toast.LENGTH_SHORT).show();
    }

    public static void cancelAlarm(Context context, int taskId) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("TODO_ALARM_" + taskId);          // must match setAlarm action

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Alarm cancelled: taskId=" + taskId);
    }
}