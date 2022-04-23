package com.example.cropool;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class WelcomeFragment extends Fragment {
    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        Button signIn = view.findViewById(R.id.sign_in);
        Button signUp = view.findViewById(R.id.sign_up);

        signIn.setOnClickListener(v -> {
            LoginFragment loginFragment = new LoginFragment();
            requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.main_activity_fragment_container, loginFragment).addToBackStack(null).commit();
        });

        signUp.setOnClickListener(v -> {
            RegisterFragment registerFragment = new RegisterFragment();
            requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.main_activity_fragment_container, registerFragment).addToBackStack(null).commit();
        });

        return view;
    }
}