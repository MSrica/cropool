package com.example.cropool.custom;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class InputElement {
    private final TextInputLayout inputLayout;
    private final TextInputEditText textInput;

    public InputElement(TextInputLayout inputLayout, TextInputEditText textInput) {
        this.inputLayout = inputLayout;
        this.textInput = textInput;
    }

    public TextInputLayout getInputLayout() {
        return inputLayout;
    }

    public TextInputEditText getTextInput() {
        return textInput;
    }
}
