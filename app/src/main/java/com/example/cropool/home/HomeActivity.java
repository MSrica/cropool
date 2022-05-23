package com.example.cropool.home;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.cropool.R;
import com.example.cropool.start.WelcomeFragment;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        WelcomeFragment welcomeFragment = new WelcomeFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new HomeFragment()).commit();

        // TODO: Check JWT token, refresh if needed, prompt user to login again if needed
        // TODO: Navigation bar...
    }
}