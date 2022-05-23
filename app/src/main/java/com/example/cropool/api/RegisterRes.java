package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class RegisterRes {
    @SerializedName("feedback")
    private String feedback;

    public String getFeedback() {
        return feedback;
    }

    @Override
    public String toString() {
        return "RegisterRes{" +
                "feedback='" + feedback + '\'' +
                '}';
    }
}
