package com.jby.admin.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";
    private int numMessages = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                if (pushPermission(json.getJSONObject("data").getString("channel_id")))
                    sendNotification(json.getJSONObject("data"));

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    @Override
    public void onNewToken(String token) {

    }

    private boolean pushPermission(String channel_id) {
        switch (channel_id) {
            case "1":
                return SharedPreferenceManager.getShowNotification(this, "remark_notification");
            case "2":
                return SharedPreferenceManager.getShowNotification(this, "remark_notification");
            case "3":
                return SharedPreferenceManager.getShowNotification(this, "delivery_notification");

        }
        return false;
    }

    private void sendNotification(JSONObject messageBody) throws JSONException {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("channel_id", messageBody.getString("channel_id"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = messageBody.getString("channel_id");
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.basket_icon)
                        .setContentTitle(messageBody.getString("title"))
                        .setContentText(messageBody.getString("message"))
                        .setContentInfo("Testing")
                        .setTicker(messageBody.getString("title"))
                        .setSubText("Tap to view the detail.")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setVibrate(new long[]{Notification.DEFAULT_VIBRATE})
                        .setLights(Color.RED, 1000, 1000) //sets the color of the LED , ON-state duration , OFF-state duration
                        .setContentIntent(pendingIntent)
                        .setNumber(++numMessages)
                        .addAction(0, "View", pendingIntent)
                        .setVibrate(new long[]{Notification.DEFAULT_VIBRATE})
                        .setPriority(Notification.PRIORITY_MAX);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, messageBody.getString("channel_name"), NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        assert notificationManager != null;
        notificationManager.notify(Integer.valueOf(channelId), notificationBuilder.build());
    }

}