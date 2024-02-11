package com.example.aurtisticsv.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import androidx.core.app.NotificationCompat.Builder;
import com.blogspot.atifsoftwares.firebaseapp.ChatActivity;
import com.blogspot.atifsoftwares.firebaseapp.PostDetailActivity;
import com.blogspot.atifsoftwares.firebaseapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.RemoteMessage.Notification;
import java.util.Random;

public class FirebaseMessaging extends FirebaseMessagingService {
    private static final String ADMIN_CHANNEL_ID = "admin_channel";

    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String savedCurrentUser = getSharedPreferences("SP_USER", 0).getString("Current_USERID", "None");
        String notificationType = (String) remoteMessage.getData().get("notificationType");
        String pId;
        if (notificationType.equals("PostNotification")) {
            pId = (String) remoteMessage.getData().get("pId");
            String pTitle = (String) remoteMessage.getData().get("pTitle");
            String pDescription = (String) remoteMessage.getData().get("pDescription");
            if (!((String) remoteMessage.getData().get("sender")).equals(savedCurrentUser)) {
                String str = "";
                showPostNotification(str + pId, str + pTitle, str + pDescription);
            }
        } else if (notificationType.equals("ChatNotification")) {
            String sent = (String) remoteMessage.getData().get("sent");
            pId = (String) remoteMessage.getData().get("user");
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fUser != null && sent.equals(fUser.getUid()) && !savedCurrentUser.equals(pId)) {
                if (VERSION.SDK_INT >= 26) {
                    sendOAndAboveNotification(remoteMessage);
                } else {
                    sendNormalNotification(remoteMessage);
                }
            }
        }
    }

    private void showPostNotification(String pId, String pTitle, String pDescription) {
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        int notificationID = new Random().nextInt(3000);
        if (VERSION.SDK_INT >= 26) {
            setupPostNotificationChannel(notificationManager);
        }
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("postId", pId);
        intent.addFlags(67108864);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, null, intent, 1073741824);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.firebase_logo);
        notificationManager.notify(notificationID, new Builder(this, ADMIN_CHANNEL_ID).setSmallIcon(R.drawable.firebase_logo).setLargeIcon(largeIcon).setContentTitle(pTitle).setContentText(pDescription).setSound(RingtoneManager.getDefaultUri(2)).setContentIntent(pendingIntent).build());
    }

    private void setupPostNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, "New Notification", 4);
        adminChannel.setDescription("Device to device post notification");
        adminChannel.enableLights(true);
        adminChannel.setLightColor(-65536);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String str = "";
        String user = str + ((String) remoteMessage.getData().get("user"));
        String icon = (String) remoteMessage.getData().get("icon");
        String title = (String) remoteMessage.getData().get("title");
        String body = (String) remoteMessage.getData().get("body");
        Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", str));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(67108864);
        Builder builder = new Builder(this).setSmallIcon(Integer.parseInt(icon)).setContentText(body).setContentTitle(title).setAutoCancel(true).setSound(RingtoneManager.getDefaultUri(2)).setContentIntent(PendingIntent.getActivity(this, i, intent, 1073741824));
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        int j = 0;
        if (i > 0) {
            j = i;
        }
        notificationManager.notify(j, builder.build());
    }

    private void sendOAndAboveNotification(RemoteMessage remoteMessage) {
        String user = (String) remoteMessage.getData().get("user");
        String icon = (String) remoteMessage.getData().get("icon");
        String title = (String) remoteMessage.getData().get("title");
        String body = (String) remoteMessage.getData().get("body");
        Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(67108864);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, 1073741824);
        Uri defSoundUri = RingtoneManager.getDefaultUri(2);
        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        android.app.Notification.Builder builder = notification1.getONotifications(title, body, pIntent, defSoundUri, icon);
        int j = 0;
        if (i > 0) {
            j = i;
        }
        notification1.getManager().notify(j, builder.build());
    }

    public void onNewToken(String s) {
        super.onNewToken(s);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            updateToken(s);
        }
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        ref.child(user.getUid()).setValue(new Token(tokenRefresh));
    }
}
