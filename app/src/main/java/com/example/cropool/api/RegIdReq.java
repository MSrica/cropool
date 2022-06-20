package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class RegIdReq {
    @SerializedName("registration_id")
    private final String registrationId;

    public RegIdReq(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    @Override
    public String toString() {
        return "RegIdReq{" +
                "registrationId='" + registrationId + '\'' +
                '}';
    }
}
