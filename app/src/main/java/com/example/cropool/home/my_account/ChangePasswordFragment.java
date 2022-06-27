package com.example.cropool.home.my_account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.cropool.R;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Feedback;
import com.example.cropool.api.Tokens;
import com.example.cropool.api.UpdatePasswordReq;
import com.example.cropool.custom.InputElement;
import com.example.cropool.home.navigation_endpoints.MyAccountFragment;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

import at.favre.lib.crypto.bcrypt.BCrypt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChangePasswordFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        InputElement currentPassword = new InputElement(view.findViewById(R.id.old_password_layout), view.findViewById(R.id.old_password));
        InputElement newPassword = new InputElement(view.findViewById(R.id.new_password_layout), view.findViewById(R.id.new_password));
        InputElement newPasswordConfirm = new InputElement(view.findViewById(R.id.new_password_confirm_layout), view.findViewById(R.id.new_password_confirm));
        Button updatePassword = view.findViewById(R.id.update_password);

        // Dialog for logout from all devices query
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    updatePassword(currentPassword, newPassword, newPasswordConfirm, true, true);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    updatePassword(currentPassword, newPassword, newPasswordConfirm, false, true);
                    break;
            }
        };

        updatePassword.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getResources().getString(R.string.LOGOUT_FROM_ALL_DIALOG))
                    .setPositiveButton(getResources().getString(R.string.YES), dialogClickListener)
                    .setNegativeButton(getResources().getString(R.string.NO), dialogClickListener)
                    .show();
        });

        return view;
    }

    private void updatePassword(InputElement currentPassword, InputElement newPassword, InputElement newPasswordConfirm, boolean logoutRequired, boolean refreshIfNeeded) {
        for (InputElement ie : new InputElement[]{currentPassword, newPassword, newPasswordConfirm}) {
            if (ie.getInputLayout() == null || ie.getTextInput() == null) {
                return;
            }
        }

        if (!validateData(currentPassword, newPassword, newPasswordConfirm))
            return;

        // TODO: Update password
        // Hashing variable password and storing it to passwordHashed
        String passwordHash = BCrypt.withDefaults().hashToString(12, Objects.requireNonNull(newPassword.getTextInput().getText()).toString().toCharArray());

        UpdatePasswordReq updatePasswordReq = new UpdatePasswordReq(Objects.requireNonNull(currentPassword.getTextInput().getText()).toString(), passwordHash, String.valueOf(false));

        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.changePassword(requireContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(requireContext()),
                updatePasswordReq);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/changePassword", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 403) {
                        // Access token or current password invalid

                        // First we'll refresh the tokens (if refreshIfNeeded is true)

                        if (refreshIfNeeded) {
                            // Toast.makeText(getContext(), "Refreshing tokens...", Toast.LENGTH_SHORT).show();

                            // Try to refresh tokens using refresh tokens and re-run updateUserData() if refreshing is successful
                            Tokens.refreshTokensOnServer(requireActivity(), requireContext(), () -> {
                                updatePassword(currentPassword, newPassword, newPasswordConfirm, logoutRequired, false);
                                return null;
                            });
                        } else {
                            // We already refreshed the tokens so the credentials are invalid
                            if (getContext() != null)
                                Toast.makeText(getContext(), getResources().getString(R.string.FEEDBACK_CREDS_INVALID), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                Feedback feedback = response.body();

                if (response.code() == 201) {   // User password updated
                    if (getContext() != null)
                        Toast.makeText(getContext(), (feedback != null) ? feedback.getFeedback() : "User password updated.", Toast.LENGTH_LONG).show();

                    if (logoutRequired) {
                        // Logout was required
                        Tokens.loginRequiredProcedure(requireContext(), requireActivity());
                    } else {
                        // Logout wasn't required
                        requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new MyAccountFragment()).addToBackStack(null).commit();
                    }
                } else {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/changePassword", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    private boolean validateData(InputElement currentPassword, InputElement newPassword, InputElement newPasswordConfirm) {
        // Checking for nulls
        for (InputElement ie : new InputElement[]{currentPassword, newPassword, newPasswordConfirm}) {
            if (ie.getInputLayout() == null || ie.getTextInput() == null || ie.getTextInput().getText() == null) {
                return false;
            }

            // If the previous validation set the error to true (maybe the error isn't present anymore)
            ie.getInputLayout().setErrorEnabled(false);
        }

        final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[\\W])(?=.*[0-9])(?=.*[a-z]).{8,64}$");
        boolean isValid = true;

        if (Objects.requireNonNull(currentPassword.getTextInput().getText()).toString().isEmpty()) {
            isValid = false;
            currentPassword.getInputLayout().setError(requireContext().getResources().getString(R.string.INPUT_ERROR_PASSWORD_MISSING));
        }

        if (!PASSWORD_PATTERN.matcher(Objects.requireNonNull(newPassword.getTextInput().getText()).toString()).matches()) {
            isValid = false;
            newPassword.getInputLayout().setError(getResources().getString(R.string.INPUT_ERROR_PASSWORD));
        }

        if (!Objects.requireNonNull(newPasswordConfirm.getTextInput().getText()).toString().equals(Objects.requireNonNull(newPassword.getTextInput().getText()).toString())) {
            isValid = false;
            newPasswordConfirm.getInputLayout().setError(getResources().getString(R.string.INPUT_ERROR_PASSWORD_CONFIRM));
        }

        return isValid;
    }
}