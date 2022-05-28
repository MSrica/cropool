package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class LoginReq {
    @SerializedName("e_mail")
    private final String email;

    @SerializedName("password")
    private final String password;

    public LoginReq(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "LoginReq{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
