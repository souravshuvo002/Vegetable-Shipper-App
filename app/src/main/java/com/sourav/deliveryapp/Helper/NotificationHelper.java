package com.sourav.deliveryapp.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.sourav.deliveryapp.Activity.ShowOrderHistoryActivity;
import com.sourav.deliveryapp.R;

public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_ID = "com.sourav.deliveryap";
    private static final String CHANNEL_NAME = "Vegetable Shipper";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createChannel() {
        android.app.NotificationChannel channel = new android.app.NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getNotification(String title, String message, Uri soundUri) {

        Intent intent = new Intent(this, ShowOrderHistoryActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pIntent)
                .setShowWhen(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(true);
    }
}