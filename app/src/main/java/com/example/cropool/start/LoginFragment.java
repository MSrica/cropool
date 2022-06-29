package com.example.cropool.start;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cropool.R;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Feedback;
import com.example.cropool.api.LoginReq;
import com.example.cropool.api.Tokens;
import com.example.cropool.custom.InputElement;
import com.example.cropool.home.HomeActivity;
import com.example.cropool.notifications.TokenActions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginFragment extends Fragment {
    public LoginFragment() {
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        InputElement email = new InputElement(view.findViewById(R.id.e_mail_layout), view.findViewById(R.id.e_mail));
        InputElement password = new InputElement(view.findViewById(R.id.password_layout), view.findViewById(R.id.password));
        Button signIn = view.findViewById(R.id.sign_in);
        TextView registerLink = view.findViewById(R.id.register_link);

        TokenActions.getRegistrationToken(view.getContext());

        registerLink.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

            if (!"RegisterToLoginLink".equals(fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName()))
                fragmentManager.beginTransaction().add(R.id.start_activity_fragment_container, new RegisterFragment()).addToBackStack("LoginToRegisterLink").commit();
            else
                requireActivity().onBackPressed();
        });

        signIn.setOnClickListener(v -> {
            signIn.setEnabled(false);

            if (informationValid(email, password)) {
                if (email == null || password == null || email.getTextInput() == null || password.getTextInput() == null || email.getTextInput().getText() == null || password.getTextInput().getText() == null) {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Check input.", Toast.LENGTH_LONG).show();
                } else {
                    loginUser(v, email.getTextInput().getText().toString(), password.getTextInput().getText().toString(), signIn);
                }
            }
        });

        return view;
    }

    private boolean informationValid(InputElement email, InputElement password) {
        for (InputElement ie : new InputElement[]{email, password}) {
            ie.getInputLayout().setErrorEnabled(false);
        }

        boolean valid = true;

        if (email.getTextInput() == null || !Patterns.EMAIL_ADDRESS.matcher(Objects.requireNonNull(email.getTextInput().getText()).toString()).matches()) {
            valid = false;
            assert email.getInputLayout() != null;
            email.getInputLayout().setError(getResources().getString(R.string.INPUT_ERROR_EMAIL));
        }

        if (password.getTextInput() == null || password.getTextInput().toString().isEmpty()) {
            valid = false;
            assert password.getInputLayout() != null;
            password.getInputLayout().setError(getResources().getString(R.string.INPUT_ERROR_PASSWORD_MISSING));
        }

        return valid;
    }

    private void loginUser(View view, String email, String password, Button signIn) {
        // TODO: Find a way to send hashed/encrypted passwords over the internet
        // Hashing variable password and storing it to passwordHashed
        // String passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        LoginReq loginReq = new LoginReq(email, password);

        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);
        Call<Feedback> call = cropoolAPI.login(loginReq);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/login", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.body() != null) {
                        if (view.getContext() != null)
                            Toast.makeText(view.getContext(), "Sorry, there was an error. " + response.body().getFeedback(), Toast.LENGTH_LONG).show();
                    } else if (response.code() == 403) {
                        if (view.getContext() != null)
                            Toast.makeText(view.getContext(), "Sorry, there was an error. " + getResources().getString(R.string.FEEDBACK_CREDS_INVALID), Toast.LENGTH_LONG).show();
                    } else {
                        if (view.getContext() != null)
                            Toast.makeText(view.getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    signIn.setEnabled(true);
                    return;
                }

                Feedback feedback = response.body();
                if (feedback == null) {
                    signIn.setEnabled(true);
                    return;
                }

                if (response.code() == 201) {   // User is logged in
                    // SAVE TOKENS
                    Tokens.save(view.getContext(),
                            response.headers().get(getResources().getString(R.string.ACCESS_TOKEN_HEADER_KEY)),
                            response.headers().get(getResources().getString(R.string.REFRESH_TOKEN_HEADER_KEY)),
                            response.headers().get(getResources().getString(R.string.FIREBASE_TOKEN_HEADER_KEY)),
                            () -> {
                                TokenActions.changeDatabaseRegistrationToken(getActivity(), getContext(), true);
                                return null;
                            });

                    startActivity(new Intent(view.getContext(), HomeActivity.class));

                    if (getActivity() != null)
                        getActivity().finish();
                } else {
                    if (view.getContext() != null)
                        Toast.makeText(view.getContext(), "Sorry, there was an error. " + feedback.getFeedback(), Toast.LENGTH_LONG).show();
                }

                signIn.setEnabled(true);
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                if (view.getContext() != null)
                    Toast.makeText(view.getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/login", "onFailure: Something went wrong. " + t.getMessage());

                signIn.setEnabled(true);
            }
        });
    }
}