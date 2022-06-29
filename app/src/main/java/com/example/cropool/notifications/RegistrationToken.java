package com.example.cropool.notifications;

// https://firebase.google.com/docs/cloud-messaging/android/client#java_1

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.cropool.R;
import com.example.cropool.home.HomeActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class RegistrationToken extends FirebaseMessagingService {

    private static final String TAG = "notificationActions";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, HomeActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notifications")
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle((remoteMessage.getData().get("title") == null) ? "N/A" : remoteMessage.getData().get("title"))
                .setContentText((remoteMessage.getData().get("body") == null) ? "N/A" : remoteMessage.getData().get("body"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        // Set the intent that will fire when the user taps the notificatios

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