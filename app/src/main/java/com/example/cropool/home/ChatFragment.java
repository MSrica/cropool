package com.example.cropool.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.cropool.R;
import com.example.cropool.api.Tokens;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChatFragment extends Fragment {
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser == null) {
            // User not signed in, sign him in
            customSignIn();
            return;
        }

        // TODO: Update user interface
    }

    private void customSignIn() {
        if (!Tokens.isFirebaseTokenSet(requireContext())){
            Log.e("firebaseLogin", "signInWithCustomToken: failed");
            Toast.makeText(getContext(), "Chat authentication failed. Please sign in again.", Toast.LENGTH_LONG).show();
            Tokens.loginRequiredProcedure(requireContext(), requireActivity());
        }

        mAuth.signInWithCustomToken(Tokens.getFirebaseToken(requireContext()))
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in successful
                        Log.i("firebaseLogin", "signInWithCustomToken: success");

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        updateUI(firebaseUser);
                    } else {
                        // Sign in failed
                        Log.e("firebaseLogin", "signInWithCustomToken: failed");

                        Toast.makeText(getContext(), "Chat authentication failed.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        return v;
    }
}