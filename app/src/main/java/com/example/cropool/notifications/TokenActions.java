package com.example.cropool.notifications;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.widget.Toast;

import com.example.cropool.R;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Feedback;
import com.example.cropool.api.RegIdReq;
import com.example.cropool.api.Tokens;
import com.example.cropool.api.UpdatePasswordReq;
import com.example.cropool.home.navigation_endpoints.MyAccountFragment;
import com.example.cropool.start.RegisterFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

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
                }
            });
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

    public static void changeDatabaseRegistrationToken(Context context){
        String token = getLocalRegistrationToken(context);

        RegIdReq regIdReq = new RegIdReq(token);

        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.updateRegistrationToken(context.getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(context),
                regIdReq);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/updateRegToken", "notSuccessful: Something went wrong. - " + response.code());
                    return;
                }

                Feedback feedback = response.body();

                if (response.code() == 201) {   // User registration token updated
                    Toast.makeText(context, (feedback != null) ? feedback.getFeedback() : "User registration token updated.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Sorry, there was an error.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                Toast.makeText(context, "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/updateRegToken", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }
}