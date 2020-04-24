package com.homathon.tdudes.utills;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.homathon.tdudes.R;

import java.util.Random;

public class PushNotification {

    private final static String NOTIFICATION_CHANNEL_ID = "1000002";

    public static void pushOrderNotification(Context context, String title, String body) {
        NavDeepLinkBuilder navDeepLinkBuilder = new NavDeepLinkBuilder(context);
        PendingIntent notificationIntent = navDeepLinkBuilder.setGraph(R.navigation.nav_app).setDestination(R.id.nav_notifications).createPendingIntent();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder;

        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(context.getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(notificationIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel;
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert notificationManager != null;
            notificationBuilder.setContentTitle(title);
            notificationBuilder.setContentText(body);
            notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Random random = new Random();
        int notificationID = random.nextInt(9999 - 1000) + 1000;
        if (notificationManager != null) {
            notificationManager.notify(notificationID, notificationBuilder.build());
        }

    }
}

