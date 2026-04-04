package com.example.lifesync.activities.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.lifesync.activities.activities.AlarmFullScreenActivity;
import com.example.lifesync.activities.helper.AlarmHelper;

/**
 * Handles the "Stop" action button tapped directly from the notification shade.
 * Cancels the notification and broadcasts to AlarmFullScreenActivity to finish.
 */
public class StopAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra(AlarmReceiver.EXTRA_ID, -1);
        Log.d("StopAlarmReceiver", "Stop tapped for taskId=" + taskId);

        // 1. Cancel the ongoing notification
        NotificationManager mgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mgr != null) mgr.cancel(taskId);

        // 2. Tell AlarmFullScreenActivity to close (if it's open)
        Intent dismiss = new Intent(AlarmFullScreenActivity.ACTION_STOP_ALARM);
        context.sendBroadcast(dismiss);

        // 3. Cancel future alarm instance (in case it was rescheduled)
        AlarmHelper.cancelAlarm(context, taskId);
    }
}
