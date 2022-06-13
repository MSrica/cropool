package com.example.cropool.my_account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.cropool.R;
import com.example.cropool.custom.InputElement;

import java.util.Objects;
import java.util.regex.Pattern;

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

        updatePassword.setOnClickListener(v -> updatePassword(currentPassword, newPassword, newPasswordConfirm));

        return view;
    }

    private void updatePassword(InputElement currentPassword, InputElement newPassword, InputElement newPasswordConfirm) {
        for (InputElement ie : new InputElement[]{currentPassword, newPassword, newPasswordConfirm}) {
            if (ie.getInputLayout() == null || ie.getTextInput() == null) {
                return;
            }
        }

        if (!validateData(currentPassword, newPassword, newPasswordConfirm))
            return;

        // TODO: Update password
    }

    private boolean validateData(InputElement currentPassword, InputElement newPassword, InputElement newPasswordConfirm) {
        for (InputElement ie : new InputElement[]{currentPassword, newPassword, newPasswordConfirm}) {
            if (ie.getInputLayout() == null || ie.getTextInput() == null || ie.getTextInput().getText() == null) {
                return false;
            }

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