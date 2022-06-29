package com.example.cropool.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.cropool.R;
import com.example.cropool.home.HomeActivity;
import com.example.cropool.notifications.TokenActions;
import com.example.cropool.start.StartActivity;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@SuppressLint("ApplySharedPref")
public abstract class Tokens {
    public static void save(@NonNull Context context, String accessToken, String refreshToken, String firebaseToken) {
        // TODO: Store in a more secure way?
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            editor.putString(context.getResources().getString(R.string.ACCESS_TOKEN_KEY_NAME), accessToken);
            editor.putString(context.getResources().getString(R.string.REFRESH_TOKEN_KEY_NAME), refreshToken);
            editor.putString(context.getResources().getString(R.string.FIREBASE_TOKEN_KEY_NAME), firebaseToken);
            editor.commit();
        } catch (Exception e) {
            Log.e("EXCEPTION", e.getMessage());
        }
    }

    public static void save(@NonNull Context context, String accessToken, String refreshToken, String firebaseToken, Callable<Void> postExecution) {
        // TODO: Store in a more secure way?
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            editor.putString(context.getResources().getString(R.string.ACCESS_TOKEN_KEY_NAME), accessToken);
            editor.putString(context.getResources().getString(R.string.REFRESH_TOKEN_KEY_NAME), refreshToken);
            editor.putString(context.getResources().getString(R.string.FIREBASE_TOKEN_KEY_NAME), firebaseToken);
            editor.commit();

            // Run the method that required refreshing access and firebase tokens again
            try {
                postExecution.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e("EXCEPTION", e.getMessage());
        }
    }

    public static void clearAllTokens(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            editor.remove(context.getResources().getString(R.string.ACCESS_TOKEN_KEY_NAME));
            editor.remove(context.getResources().getString(R.string.REFRESH_TOKEN_KEY_NAME));
            editor.remove(context.getResources().getString(R.string.FIREBASE_TOKEN_KEY_NAME));
            editor.commit();
        } catch (Exception e) {
            Log.e("EXCEPTION", e.getMessage());
        }
    }

    public static String getAccessToken(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);

        return sharedPreferences.getString(context.getResources().getString(R.string.ACCESS_TOKEN_KEY_NAME), null);
    }

    public static String getRefreshToken(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);

        return sharedPreferences.getString(context.getResources().getString(R.string.REFRESH_TOKEN_KEY_NAME), null);
    }

    public static String getFirebaseToken(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);

        return sharedPreferences.getString(context.getResources().getString(R.string.FIREBASE_TOKEN_KEY_NAME), null);
    }

    public static void setAccessToken(@NonNull Context context, String accessToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            editor.putString(context.getResources().getString(R.string.ACCESS_TOKEN_KEY_NAME), accessToken);
            editor.commit();
        } catch (Exception e) {
            Log.e("EXCEPTION", e.getMessage());
        }
    }

    public static void setRefreshToken(@NonNull Context context, String refreshToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            editor.putString(context.getResources().getString(R.string.REFRESH_TOKEN_KEY_NAME), refreshToken);
            editor.commit();
        } catch (Exception e) {
            Log.e("EXCEPTION", e.getMessage());
        }
    }

    public static void setFirebaseToken(@NonNull Context context, String firebaseToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            editor.putString(context.getResources().getString(R.string.FIREBASE_TOKEN_KEY_NAME), firebaseToken);
            editor.commit();
        } catch (Exception e) {
            Log.e("EXCEPTION", e.getMessage());
        }
    }

    public static boolean isAccessTokenSet(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);

        return sharedPreferences.contains(context.getResources().getString(R.string.ACCESS_TOKEN_KEY_NAME));
    }

    public static boolean isRefreshTokenSet(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);

        return sharedPreferences.contains(context.getResources().getString(R.string.REFRESH_TOKEN_KEY_NAME));
    }

    public static boolean isFirebaseTokenSet(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);

        return sharedPreferences.contains(context.getResources().getString(R.string.FIREBASE_TOKEN_KEY_NAME));
    }

    public static void loginRequiredProcedure(@NonNull Context context, @NonNull Activity activity) {
        Tokens.clearAllTokens(context);
        TokenActions.clearLocalRegistrationToken(context);
        HomeActivity.signOutCurrentFBUser();
        context.startActivity(new Intent(context, StartActivity.class));
        activity.finish();
    }

    public static void logoutProcedure(@NonNull Context context, @NonNull Activity activity) {
        HomeActivity.signOutCurrentFBUser();
        Tokens.loginRequiredProcedure(context, activity);
    }

    public static void refreshTokensOnServer(Activity activity, Context context, Callable<Void> postExecution) {
        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.tokens(context.getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getRefreshToken(context));

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/tokens", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 403) {
                        Toast.makeText(context, "Please login again.", Toast.LENGTH_LONG).show();
                        Tokens.loginRequiredProcedure(context, activity);
                    } else {
                        Toast.makeText(context, "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                Feedback feedback = response.body();
                if (feedback == null) {
                    Toast.makeText(context, "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.code() == 201) {   // Tokens issued
                    // SAVE TOKENS
                    Tokens.setAccessToken(context,
                            response.headers().get(context.getResources().getString(R.string.ACCESS_TOKEN_HEADER_KEY)));
                    Tokens.setFirebaseToken(context,
                            response.headers().get(context.getResources().getString(R.string.FIREBASE_TOKEN_HEADER_KEY)));

                    // Run the method that required refreshing access and firebase tokens again
                    try {
                        postExecution.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(context, "Sorry, there was an error. " + feedback.getFeedback(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                Log.e("/register", "onFailure: Something went wrong. " + t.getMessage());
                Toast.makeText(context, "Sorry, there was an error. " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}