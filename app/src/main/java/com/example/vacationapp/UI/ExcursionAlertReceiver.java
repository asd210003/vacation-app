package com.example.vacationapp.UI;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.vacationapp.R;

public class ExcursionAlertReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "excursion_reminders";
    private static final String CHANNEL_NAME = "Excursion Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        if (title == null || title.trim().isEmpty()) title = "Excursion";

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Excursion Reminder")
                .setContentText(title)
                .setAutoCancel(true);

        nm.notify((int) (System.currentTimeMillis() & 0x7fffffff),
                builder.build());
    }
}
