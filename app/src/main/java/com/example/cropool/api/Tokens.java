package com.example.cropool.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.cropool.R;

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
}