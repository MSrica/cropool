package com.example.cropool;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Uncomment for testing the splash screen
//        try {
//            TimeUnit.SECONDS.sleep(3);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        this.setTheme(R.style.Theme_Cropool);

        setContentView(R.layout.activity_main);

        WelcomeFragment welcomeFragment = new WelcomeFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_activity_fragment_container, welcomeFragment).addToBackStack(null).commit();
    }
}