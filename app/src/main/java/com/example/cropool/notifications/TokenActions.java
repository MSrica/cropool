package com.example.cropool.notifications;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.cropool.R;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Feedback;
import com.example.cropool.api.RegIdReq;
import com.example.cropool.api.Tokens;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@SuppressLint("ApplySharedPref")
public abstract class TokenActions {

    private static final String TAG = "notificationActions";

    public static void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("notifications")
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed";
                    if (!task.isSuccessful()) {
                        msg = "Subscribe failed";
                    }
                    Log.d(TAG, msg);
                });
    }

    public static void getRegistrationToken(Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    changeLocalRegistrationToken(context, token);
                });
    }

    public static void clearLocalRegistrationToken(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            editor.remove(context.getResources().getString(R.string.REGISTRATION_TOKEN_KEY_NAME));
            editor.commit();
        } catch (Exception e) {
            Log.e("EXCEPTION", e.getMessage());
        }
    }

    public static void changeLocalRegistrationToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            editor.putString(context.getResources().getString(R.string.REGISTRATION_TOKEN_KEY_NAME), token);
            editor.commit();
        } catch (Exception e) {
            Log.e("EXCEPTION", e.getMessage());
        }
    }

    public static String getLocalRegistrationToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        return sharedPreferences.getString(context.getResources().getString(R.string.REGISTRATION_TOKEN_KEY_NAME), null);
    }

    public static void changeDatabaseRegistrationToken(Activity activity, Context context, boolean refreshIfNeeded) {
        String token = getLocalRegistrationToken(context);

        RegIdReq regIdReq = new RegIdReq(token);

        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.updateRegistrationToken(context.getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(context),
                context.getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getFirebaseToken(context),
                regIdReq);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/changeDatabaseRegToken", "notSuccessful: Something went wrong. - " + response.code() + response);

                    if (response.code() == 403 || response.code() == 401) {
                        // Access or Firebase tokens invalid

                        // Try to refresh tokens using refresh tokens and re-run addRoute() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if (refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(activity, context, () -> {
                                changeDatabaseRegistrationToken(activity, context, false);
                                return null;
                            });
                        }
                    }

                    return;
                }

                Feedback feedback = response.body();

                if (response.code() == 200) {   // Token updated
                    // Toast.makeText(context, (feedback != null) ? feedback.getFeedback() : "Token updated.", Toast.LENGTH_LONG).show();
                } else {
                    // Toast.makeText(context, "Sorry, couldn't accept.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                // Toast.makeText(context, "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/changeDatabaseRegToken", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }
}