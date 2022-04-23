package com.example.cropool;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTheme(R.style.Theme_Cropool);

        setContentView(R.layout.activity_main);

        WelcomeFragment welcomeFragment = new WelcomeFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_activity_fragment_container, welcomeFragment).commit();
    }
}