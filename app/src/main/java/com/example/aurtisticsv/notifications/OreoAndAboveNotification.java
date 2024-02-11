package com.example.aurtisticsv.notifications;

import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build.VERSION;

public class OreoAndAboveNotification extends ContextWrapper {
    private static final String ID = "some_id";
    private static final String NAME = "FirebaseAPP";
    private NotificationManager notificationManager;

    public OreoAndAboveNotification(Context base) {
        super(base);
        if (VERSION.SDK_INT >= 26) {
            createChannel();
        }
    }

    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(ID, NAME, 3);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(0);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager() {
        if (this.notificationManager == null) {
            this.notificationManager = (NotificationManager) getSystemService("notification");
        }
        return this.notificationManager;
    }

    public Builder getONotifications(String title, String body, PendingIntent pIntent, Uri soundUri, String icon) {
        return new Builder(getApplicationContext(), ID).setContentIntent(pIntent).setContentTitle(title).setContentText(body).setSound(soundUri).setAutoCancel(true).setSmallIcon(Integer.parseInt(icon));
    }
}
