package com.example.cropool.notifications;

// https://firebase.google.com/docs/cloud-messaging/android/client#java_1

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.cropool.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class RegistrationToken extends FirebaseMessagingService {

    private static final String TAG = "notificationActions";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notifications")
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle((remoteMessage.getData().get("title") == null) ? "N/A" : remoteMessage.getData().get("title"))
                .setContentText((remoteMessage.getData().get("body") == null) ? "N/A" : remoteMessage.getData().get("body"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Set the intent that will fire when the user taps the notification
        //.setContentIntent(pendingIntent)

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        Random random = new Random();
        int notificationId = random.nextInt(1000000);
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "New token: " + token);
        TokenActions.changeLocalRegistrationToken(getApplicationContext(), token);
    }

}