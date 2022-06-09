package com.example.cropool.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cropool.R;
import com.example.cropool.api.Tokens;
import com.example.cropool.home.navigation_fragments.AddRouteFragment;
import com.example.cropool.home.navigation_fragments.ConversationListFragment;
import com.example.cropool.home.navigation_fragments.FindRouteFragment;
import com.example.cropool.home.navigation_fragments.MyAccountFragment;
import com.example.cropool.start.StartActivity;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        // Check if the refresh token is set
        // (we can get the access token with the refresh token)
        if (!Tokens.isRefreshTokenSet(this)) {
            Toast.makeText(this, "Sorry, you need to log in again.", Toast.LENGTH_LONG).show();

            startActivity(new Intent(this, StartActivity.class));
            this.finish();
        }

        // Initialize bottom navigation view
        NavigationBarView navigationBarView = findViewById(R.id.home_activity_bottom_navigation);
        navigationBarView.setOnItemSelectedListener(this);
        navigationBarView.setSelectedItemId(R.id.find_route);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.find_route) {
            getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_fragment_container, new FindRouteFragment()).commit();
            return true;
        } else if (itemId == R.id.add_route) {
            getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_fragment_container, new AddRouteFragment()).commit();
            return true;
        } else if (itemId == R.id.chat) {
            getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_fragment_container, new ConversationListFragment()).commit();
            return true;
        } else if (itemId == R.id.my_account) {
            getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_fragment_container, new MyAccountFragment()).commit();
            return true;
        }

        return false;
    }
}