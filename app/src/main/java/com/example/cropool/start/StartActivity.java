package com.example.cropool.start;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cropool.R;
import com.example.cropool.home.HomeActivity;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = this.getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
        if (sharedPreferences.contains(getResources().getString(R.string.ACCESS_TOKEN_KEY_NAME)) ||
                sharedPreferences.contains(getResources().getString(R.string.REFRESH_TOKEN_KEY_NAME))) {
            startActivity(new Intent(this, HomeActivity.class));
            this.finish();
        }

        this.setTheme(R.style.Theme_Cropool);

        setContentView(R.layout.activity_main);

        WelcomeFragment welcomeFragment = new WelcomeFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.start_activity_fragment_container, welcomeFragment).commit();
    }
}