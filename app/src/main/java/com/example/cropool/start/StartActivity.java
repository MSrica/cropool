package com.example.cropool.start;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    }
}