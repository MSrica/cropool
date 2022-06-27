package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class AccountInfo {
    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("profile_picture")
    private String profilePicture;

    @SerializedName("created_at")
    private Long createdAt;

    @SerializedName("number_of_routes")
    private Integer numberOfRoutes;

    public AccountInfo(String firstName, String lastName, String profilePicture, Integer numberOfRoutes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePicture = profilePicture;
        this.numberOfRoutes = numberOfRoutes;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Integer getNumberOfRoutes() {
        return numberOfRoutes;
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", createdAt=" + createdAt +
                ", numberOfRoutes=" + numberOfRoutes +
                '}';
    }
}
