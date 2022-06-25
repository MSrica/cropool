package com.example.cropool.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cropool.BuildConfig;
import com.example.cropool.R;
import com.example.cropool.api.Tokens;
import com.example.cropool.home.navigation_endpoints.AddRouteFragment;
import com.example.cropool.home.navigation_endpoints.ConversationListActivity;
import com.example.cropool.home.navigation_endpoints.FindRouteFragment;
import com.example.cropool.home.navigation_endpoints.MyAccountFragment;
import com.example.cropool.start.StartActivity;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private static FirebaseAuth mAuth;
    private static FirebaseUser currentUser;
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_RTDB_URL);
    private NavigationBarView navigationBarView;

    public static FirebaseUser getCurrentFBUser() {
        return currentUser;
    }

    public static void signOutCurrentFBUser() {
        mAuth.signOut();
        currentUser = null;
    }

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
        navigationBarView = findViewById(R.id.home_activity_bottom_navigation);
        navigationBarView.setOnItemSelectedListener(this);
        navigationBarView.setSelectedItemId(getIntent().getIntExtra(getResources().getString(R.string.HOME_ACTIVITY_NAVIGATION_EXTRA), R.id.find_route));

        // Set FB auth/user
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            customSignIn();
        }
    }

    // Signs the user in if he has the Firebase token that is set when logging in or registering
    private void customSignIn() {
        if (!Tokens.isFirebaseTokenSet(getApplicationContext())) {
            Log.e("firebaseLogin", "signInWithCustomToken: failed");
            Toast.makeText(getApplicationContext(), "Text authentication failed. Please sign in again.", Toast.LENGTH_LONG).show();
            Tokens.loginRequiredProcedure(getApplicationContext(), this);
        }

        mAuth.signInWithCustomToken(Tokens.getFirebaseToken(getApplicationContext()))
                .addOnCompleteListener(HomeActivity.this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in successful
                        Log.i("firebaseLogin", "signInWithCustomToken: success");

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        // Check the user's record in RTDB
                        checkUserRTDBRecord(firebaseUser);

                        currentUser = firebaseUser;
                    } else {
                        // Sign in failed
                        Log.e("firebaseLogin", "signInWithCustomToken: failed");

                        Toast.makeText(getApplicationContext(), "Chat authentication failed. Please sign in again.", Toast.LENGTH_LONG).show();
                        Tokens.loginRequiredProcedure(getApplicationContext(), HomeActivity.this);
                    }
                });
    }

    // Checks whether the signed in user has a record in the FB RTDB
    // If not, inserts him into the RTDB along with his email, display name and profile picture URL
    private void checkUserRTDBRecord(FirebaseUser firebaseUser) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Toast.makeText(requireContext(), "Deciding whether to create user " + firebaseUser.getUid(), Toast.LENGTH_LONG).show();
                if (!snapshot.child("user").hasChild(firebaseUser.getUid())) {
                    // User hasn't been inserted to FB RTDB yet
                    // Toast.makeText(requireContext(), "Creating user " + firebaseUser.getUid(), Toast.LENGTH_LONG).show();
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(firebaseUser.getUid()).child(getResources().getString(R.string.FB_RTDB_E_MAIL_KEY)).setValue(firebaseUser.getEmail());
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(firebaseUser.getUid()).child(getResources().getString(R.string.FB_RTDB_DISPLAY_NAME_KEY)).setValue(firebaseUser.getDisplayName());
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(firebaseUser.getUid()).child(getResources().getString(R.string.FB_RTDB_PROFILE_PICTURE_KEY)).setValue(firebaseUser.getPhotoUrl() == null ? getResources().getString(R.string.FB_RTDB_DEFAULT_PICTURE_VALUE) : firebaseUser.getPhotoUrl().toString());
                    databaseReference.child(getResources().getString(R.string.FB_RTDB_USER_TABLE_NAME)).child(firebaseUser.getUid()).child(getResources().getString(R.string.FB_RTDB_LAST_SEEN_AT_KEY)).setValue(System.currentTimeMillis());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void changeNavigationFragment(int rid) {
        navigationBarView.setSelectedItemId(rid);
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
            this.startActivity(new Intent(getApplicationContext(), ConversationListActivity.class));
            overridePendingTransition(0, 0);
            this.finish();
            return true;
        } else if (itemId == R.id.my_account) {
            getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_fragment_container, new MyAccountFragment()).commit();
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set FB auth/user
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            customSignIn();
        }
    }
}