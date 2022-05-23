package com.example.cropool.start;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.cropool.api.RegisterReq;
import com.example.cropool.api.RegisterRes;
import com.example.cropool.custom.InputElement;
import com.example.cropool.home.HomeActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

import at.favre.lib.crypto.bcrypt.BCrypt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterFragment extends Fragment {

    private InputElement firstName, lastName, email, password, passwordConfirm;

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

        firstName = new InputElement(view.findViewById(R.id.first_name_layout), view.findViewById(R.id.first_name));
        lastName = new InputElement(view.findViewById(R.id.last_name_layout), view.findViewById(R.id.last_name));
        email = new InputElement(view.findViewById(R.id.e_mail_layout), view.findViewById(R.id.e_mail));
        password = new InputElement(view.findViewById(R.id.password_layout), view.findViewById(R.id.password));
        passwordConfirm = new InputElement(view.findViewById(R.id.password_confirm_layout), view.findViewById(R.id.password_confirm));
        Button signUp = view.findViewById(R.id.sign_up);
        TextView loginLink = view.findViewById(R.id.login_link);

        loginLink.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

            if (!"LoginToRegisterLink".equals(fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName()))
                fragmentManager.beginTransaction().add(R.id.start_activity_fragment_container, new LoginFragment()).addToBackStack("RegisterToLoginLink").commit();
            else
                requireActivity().onBackPressed();
        });

        signUp.setOnClickListener(v -> {
            signUp.setEnabled(false);

            if (informationValid(firstName, lastName, email, password, passwordConfirm))
                registerUser(v, Objects.requireNonNull(firstName.getTextInput().getText()).toString(),
                        Objects.requireNonNull(lastName.getTextInput().getText()).toString(),
                        Objects.requireNonNull(email.getTextInput().getText()).toString(),
                        Objects.requireNonNull(password.getTextInput().getText()).toString());

            signUp.setEnabled(true);
        });

        return view;
    }

    private boolean informationValid(InputElement firstName, InputElement lastName, InputElement email, InputElement password, InputElement passwordConfirm) {
        for (InputElement ie : new InputElement[]{firstName, lastName, email, password, passwordConfirm}) {
            ie.getInputLayout().setErrorEnabled(false);
        }

        final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[\\W])(?=.*[0-9])(?=.*[a-z]).{8,64}$");
        boolean valid = true;

        if (firstName.getTextInput() == null || Objects.requireNonNull(firstName.getTextInput().getText()).toString().isEmpty()) {
            valid = false;
            assert firstName.getInputLayout() != null;
            firstName.getInputLayout().setError("Please enter your first name.");
        }

        if (lastName.getTextInput() == null || Objects.requireNonNull(lastName.getTextInput().getText()).toString().isEmpty()) {
            valid = false;
            assert lastName.getInputLayout() != null;
            lastName.getInputLayout().setError("Please enter your last name.");
        }

        if (email.getTextInput() == null || !Patterns.EMAIL_ADDRESS.matcher(Objects.requireNonNull(email.getTextInput().getText()).toString()).matches()) {
            valid = false;
            assert email.getInputLayout() != null;
            email.getInputLayout().setError("Please enter a valid e-mail address.");
        }

        if (password.getTextInput() == null || !PASSWORD_PATTERN.matcher(Objects.requireNonNull(password.getTextInput().getText()).toString()).matches()) {
            valid = false;
            assert password.getInputLayout() != null;
            password.getInputLayout().setError("Please enter a valid password. It should contain uppercase letter, lowercase letter, special character, a number and [8, 64] characters without whitespaces.");
        }

        if (passwordConfirm.getTextInput() == null || !Objects.requireNonNull(password.getTextInput().getText()).toString().equals(Objects.requireNonNull(passwordConfirm.getTextInput().getText()).toString())) {
            valid = false;
            assert passwordConfirm.getInputLayout() != null;
            passwordConfirm.getInputLayout().setError("Passwords do not match.");
        }

        return valid;
    }

    private void registerUser(View view, String firstName, String lastName, String email, String password) {
        // Hashing variable password and storing it to passwordHashed
        String passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        RegisterReq registerReq = new RegisterReq(firstName, lastName, email, passwordHash);

        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<RegisterRes> call = cropoolAPI.register(registerReq);

        call.enqueue(new Callback<RegisterRes>() {
            @Override
            public void onResponse(@NotNull Call<RegisterRes> call, @NotNull Response<RegisterRes> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/register", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 409) {
                        Toast.makeText(view.getContext(), "Sorry, there was an error. " + getResources().getString(R.string.FEEDBACK_EMAIL_UNAVAILABLE), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(view.getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    if (response.body() != null)
                        Toast.makeText(view.getContext(), "Sorry, there was an error. " + response.body().getFeedback(), Toast.LENGTH_LONG).show();

                    return;
                }

                RegisterRes registerRes = response.body();
                if (registerRes == null) {
                    return;
                }

                if (response.code() == 201) {   // User is registered
                    Toast.makeText(view.getContext(), "Congratulations, you are successfully registered.", Toast.LENGTH_LONG).show();

                    // TODO: Store in a more secure way?
                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    try {
                        editor.putString(getResources().getString(R.string.ACCESS_TOKEN_KEY_NAME), response.headers().get("access_token"));
                        editor.putString(getResources().getString(R.string.REFRESH_TOKEN_KEY_NAME), response.headers().get("refresh_token"));
                        editor.commit();
                    } catch (Exception e) {
                        Log.e("EXCEPTION", e.getMessage());
                    }

                    startActivity(new Intent(view.getContext(), HomeActivity.class));
                    requireActivity().finish();
                } else {
                    Toast.makeText(view.getContext(), "Sorry, there was an error. " + registerRes.getFeedback(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<RegisterRes> call, @NotNull Throwable t) {
                Toast.makeText(view.getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/register", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }
}