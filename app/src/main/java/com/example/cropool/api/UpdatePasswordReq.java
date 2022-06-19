package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class UpdatePasswordReq {
    @SerializedName("current_password")
    String currentPassword;

    @SerializedName("new_password")
    String newPassword;

    @SerializedName("logout_required")
    String logoutRequired;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getLogoutRequired() {
        return logoutRequired;
    }

    public UpdatePasswordReq(String currentPassword, String newPassword, String logoutRequired) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.logoutRequired = logoutRequired;
    }

    @Override
    public String toString() {
        return "UpdatePasswordReq{" +
                "currentPassword='" + currentPassword + '\'' +
                ", newPassword='" + newPassword + '\'' +
                ", logoutRequired='" + logoutRequired + '\'' +
                '}';
    }
}
