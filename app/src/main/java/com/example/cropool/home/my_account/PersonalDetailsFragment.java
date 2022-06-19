package com.example.cropool.home.my_account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.cropool.R;
import com.example.cropool.api.AccountInfo;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Feedback;
import com.example.cropool.api.Tokens;
import com.example.cropool.custom.InputElement;
import com.example.cropool.home.navigation_endpoints.MyAccountFragment;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PersonalDetailsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_details, container, false);

        InputElement firstName = new InputElement(view.findViewById(R.id.first_name_layout), view.findViewById(R.id.first_name));
        InputElement lastName = new InputElement(view.findViewById(R.id.last_name_layout), view.findViewById(R.id.last_name));
        Button update = view.findViewById(R.id.update_info);
        TextView updatePassword = view.findViewById(R.id.update_password_link);

        update.setOnClickListener(v -> updateName(firstName, lastName, true));
        updatePassword.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new ChangePasswordFragment()).addToBackStack(null).commit());

        return view;
    }

    private void updateName(InputElement firstName, InputElement lastName, boolean refreshIfNeeded) {
        if (firstName.getTextInput() == null || firstName.getInputLayout() == null
                || lastName.getInputLayout() == null || lastName.getTextInput() == null)
            return;

        if (!validateData(firstName, lastName))
            return;

        AccountInfo updateInfo = new AccountInfo(Objects.requireNonNull(firstName.getTextInput().getText()).toString(), Objects.requireNonNull(lastName.getTextInput().getText()).toString(), null);

        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.updateInfo(requireContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(requireContext()),
                requireContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getFirebaseToken(requireContext()), updateInfo);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/updateInfo", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 403) {
                        // Access or Firebase tokens invalid
                        // Toast.makeText(getContext(), "Refreshing tokens...", Toast.LENGTH_SHORT).show();

                        // Try to refresh tokens using refresh tokens and re-run updateUserData() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if(refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(requireActivity(), requireContext(), () -> {
                                updateName(firstName, lastName, false);
                                return null;
                            });
                        } else {
                            Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                Feedback feedback = response.body();

                if (response.code() == 201) {   // User info updated
                    Toast.makeText(getContext(), (feedback != null) ? feedback.getFeedback() : "User info updated.", Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new MyAccountFragment()).addToBackStack(null).commit();
                } else {
                    Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/updateInfo", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    private boolean validateData(InputElement firstName, InputElement lastName) {
        boolean isValid = true;
        for (InputElement ie : new InputElement[]{firstName, lastName}) {
            ie.getInputLayout().setErrorEnabled(false);
        }

        for (InputElement ie : new InputElement[]{firstName, lastName}) {
            if (ie == null || ie.getTextInput() == null || ie.getInputLayout() == null) {
                isValid = false;
                continue;
            }

            if (ie.getTextInput().getText() == null || ie.getTextInput().getText().toString().isEmpty()) {
                ie.getInputLayout().setError(requireContext().getResources().getString(R.string.CANNOT_BE_EMPTY));
                isValid = false;
            }
        }

        return isValid;
    }
}