package com.example.lifesync.activities.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.lifesync.activities.activities.AlarmFullScreenActivity;
import com.example.lifesync.activities.helper.AlarmHelper;

/**
 * Handles the "Snooze 5m" action button tapped from the notification shade.
 * Cancels current notification and reschedules alarm +5 minutes.
 */
public class SnoozeAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int    taskId = intent.getIntExtra(AlarmReceiver.EXTRA_ID, -1);
        String title  = intent.getStringExtra(AlarmReceiver.EXTRA_TITLE);
        if (title == null) title = "Task Reminder";

        Log.d("SnoozeAlarmReceiver", "Snooze tapped for taskId=" + taskId);

        // 1. Cancel the ongoing notification
        NotificationManager mgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mgr != null) mgr.cancel(taskId);

        // 2. Tell AlarmFullScreenActivity to close (if open)
        Intent dismiss = new Intent(AlarmFullScreenActivity.ACTION_SNOOZE_ALARM);
        context.sendBroadcast(dismiss);

        // 3. Reschedule alarm for +5 minutes from now
        long snoozeTime = System.currentTimeMillis()
                + (AlarmFullScreenActivity.SNOOZE_MINUTES * 60 * 1000L);
        AlarmHelper.setAlarm(context, taskId, title, snoozeTime);

        Toast.makeText(context,
                "Snoozed " + AlarmFullScreenActivity.SNOOZE_MINUTES + " minutes",
                Toast.LENGTH_SHORT).show();
    }
}
