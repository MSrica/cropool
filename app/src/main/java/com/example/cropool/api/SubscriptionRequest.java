package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class SubscriptionRequest {
    @SerializedName("idcheckpoint")
    private final String checkpointID;

    @SerializedName("first_name")
    private final String firstName;

    @SerializedName("last_name")
    private final String lastName;

    @SerializedName("profile_picture")
    private final String profilePicture;

    @SerializedName("pickup_latlng")
    private final String startLatLng;

    @SerializedName("dropoff_latlng")
    private final String finishLatLng;

    public SubscriptionRequest(String checkpointID, String firstName, String lastName, String profilePicture, String startLatLng, String finishLatLng) {
        this.checkpointID = checkpointID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePicture = profilePicture;
        this.startLatLng = startLatLng;
        this.finishLatLng = finishLatLng;
    }

    public String getCheckpointID() {
        return checkpointID;
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

    public String getStartLatLng() {
        return startLatLng;
    }

    public String getFinishLatLng() {
        return finishLatLng;
    }

    @Override
    public String toString() {
        return "SubscriptionRequest{" +
                "checkpointID='" + checkpointID + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", startLatLng='" + startLatLng + '\'' +
                ", finishLatLng='" + finishLatLng + '\'' +
                '}';
    }
}
