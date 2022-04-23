package com.example.cropool;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class RegisterFragment extends Fragment {
    public RegisterFragment() {
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
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        TextView loginLink = view.findViewById(R.id.login_link);

        loginLink.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

            if(!"LoginToRegisterLink".equals(fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName()))
                fragmentManager.beginTransaction().add(R.id.main_activity_fragment_container, new LoginFragment()).addToBackStack("RegisterToLoginLink").commit();
            else
                requireActivity().onBackPressed();
        });

        return view;
    }
}