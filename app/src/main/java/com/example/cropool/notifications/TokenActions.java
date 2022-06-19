package com.example.cropool.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.cropool.R;
import com.example.cropool.start.RegisterFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressLint("MissingPermission")
public class TokenActions {

    private static final String TAG = "notificationActions";

    public static void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("notifications")
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    String msg = "Subscribed";
                    if (!task.isSuccessful()) {
                        msg = "Subscribe failed";
                    }
                    Log.d(TAG, msg);
                }
            });
    }

    public static void getRegistrationToken(Context context) {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();

                    changeLocalRegistrationToken(context, token);
                    // TODO
                    changeDatabaseRegistrationToken(token);
                }
            });
        //Log.d(TAG, "FCM Registration token in getToken: " + RegisterFragment.registrationId);
    }

    public static void changeLocalRegistrationToken(Context context, String token){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            editor.putString(context.getResources().getString(R.string.REGISTRATION_TOKEN_KEY_NAME), token);
            editor.commit();
        } catch (Exception e) {
            Log.e("EXCEPTION", e.getMessage());
        }
    }

    public static String getLocalRegistrationToken(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        return sharedPreferences.getString(context.getResources().getString(R.string.REGISTRATION_TOKEN_KEY_NAME), null);
    }

    public static void changeDatabaseRegistrationToken(String id){

    }
}