package com.jby.vegeapp.others;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.jby.vegeapp.R;

import static com.jby.vegeapp.others.CustomNotificationManager.ID_SMALL_NOTIFICATION;

public class NotificationChannelHelper extends ContextWrapper {
    public static String NotificationChannel = "Ride";
    public static String NotificationName = "Channel 1";
    private NotificationManager notificationManager;
    private Context context;
    private Intent intent;

    public NotificationChannelHelper(Context base) {
        super(base);
        context = base;

        createChannel1();
    }

    public void createChannel1(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NotificationChannel, NotificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setLightColor(R.color.red);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(notificationChannel);
        }
    }

    public NotificationManager getManager(){
        if(notificationManager == null)
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }

    public NotificationCompat.Builder getChannel1Notification(String title, String message, Intent intent){
        long[] pattern = {500,500,500,500,500};
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, ID_SMALL_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(getApplicationContext(), NotificationChannel)
                .setSmallIcon(R.mipmap.ic_launcher).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentText(message)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setShowWhen(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setVibrate(pattern)
                .setContentIntent(resultPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }
}
