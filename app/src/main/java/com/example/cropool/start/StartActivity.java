package com.example.cropool.start;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cropool.R;
import com.example.cropool.api.Tokens;
import com.example.cropool.home.HomeActivity;
import com.example.cropool.notifications.TokenActions;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Tokens.isAccessTokenSet(this) || Tokens.isRefreshTokenSet(this)) {
            startActivity(new Intent(this, HomeActivity.class));
            this.finish();
        }

        this.setTheme(R.style.Theme_Cropool);

        setContentView(R.layout.activity_main);

        WelcomeFragment welcomeFragment = new WelcomeFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.start_activity_fragment_container, welcomeFragment).commit();

        String token = TokenActions.getLocalRegistrationToken(getApplicationContext());
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("notifications", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}