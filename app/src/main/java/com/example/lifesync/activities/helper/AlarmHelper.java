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
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        // Android 12+ exact alarm permission check → auto-open settings if missing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + context.getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                Toast.makeText(context,
                        "Enable 'Alarms & Reminders' then set alarm again",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("TODO_ALARM_" + taskId);   // unique per task — prevents PendingIntent collision
        intent.putExtra(AlarmReceiver.EXTRA_TITLE, taskTitle);
        intent.putExtra(AlarmReceiver.EXTRA_ID, taskId);

        PendingIntent pi = PendingIntent.getBroadcast(
                context, taskId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // setAlarmClock is the ONLY method that survives battery optimization on real phones
        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTimeMillis, pi);
        am.setAlarmClock(clockInfo, pi);

        Log.d(TAG, "Alarm set (setAlarmClock): taskId=" + taskId + " at=" + alarmTimeMillis);
        Toast.makeText(context, "✅ Alarm set: " + taskTitle, Toast.LENGTH_SHORT).show();
    }

    public static void cancelAlarm(Context context, int taskId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("TODO_ALARM_" + taskId);   // must match setAlarm action

        PendingIntent pi = PendingIntent.getBroadcast(
                context, taskId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        am.cancel(pi);
        Log.d(TAG, "Alarm cancelled: taskId=" + taskId);
    }
}