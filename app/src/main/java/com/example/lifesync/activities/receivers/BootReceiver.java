package com.example.lifesync.activities.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * AlarmManager alarms are cleared when the device reboots.
 * This receiver listens for BOOT_COMPLETED and reschedules all active alarms.
 *
 * How to use:
 * 1. Load all tasks with alarms from your Room DB / SharedPreferences
 * 2. Re-schedule each one using AlarmHelper.setAlarm(...)
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleAllAlarms(context);
        }
    }

    private void rescheduleAllAlarms(Context context) {
        // Example: fetch all tasks from your database that have alarms
        // List<TodoItem> tasksWithAlarms = YourDatabase.getInstance(context)
        //         .todoDao().getTasksWithAlarms();

        // for (TodoItem task : tasksWithAlarms) {
        //     if (task.hasAlarm() && task.getAlarmTimeMillis() > System.currentTimeMillis()) {
        //         AlarmHelper.setAlarm(context, task.getId(),
        //                 task.getTitle(), task.getAlarmTimeMillis());
        //     }
        // }

        // Replace the comments above with your actual DB call.
        // This stub is intentionally left for you to wire to your data layer.
    }
}
