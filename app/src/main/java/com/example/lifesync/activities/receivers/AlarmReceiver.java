package com.example.lifesync.activities.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.lifesync.R;
import com.example.lifesync.activities.activities.AlarmActivity;
import com.example.lifesync.activities.services.AlarmService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Start Alarm Service
        Intent serviceIntent =
                new Intent(context, AlarmService.class);

        ContextCompat.startForegroundService(
                context,
                serviceIntent
        );
        // Full Screen Intent
        Intent fullScreenIntent =
                new Intent(context, AlarmActivity.class);

        PendingIntent fullScreenPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        fullScreenIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        String channelId = "alarm_channel";

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            channelId,
                            "Alarm",
                            NotificationManager.IMPORTANCE_HIGH);

            channel.setLockscreenVisibility(
                    android.app.Notification.VISIBILITY_PUBLIC);

            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Alarm")
                        .setContentText("Wake up!")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setFullScreenIntent(fullScreenPendingIntent, true);

        manager.notify(1, builder.build());
    }
}