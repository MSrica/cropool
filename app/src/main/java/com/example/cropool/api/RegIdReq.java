package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class RegIdReq {
    @SerializedName("e_mail")
    private final String email;

    @SerializedName("registration_id")
    private final String registrationId;

    public RegIdReq(String email, String registrationId) {
        this.email = email;
        this.registrationId = registrationId;
    }

    public String getEmail() {
        return email;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    @Override
    public String toString() {
        return "RegIdReq{" +
                "email='" + email + '\'' +
                ", registrationId='" + registrationId + '\'' +
                '}';
    }
}
