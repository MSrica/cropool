package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class Passenger {
    @SerializedName("idpassenger")
    private final String id;

    @SerializedName("idcheckpoint")
    private final String checkpointID;

    @SerializedName("pickup_latlng")
    private final String pickupLatLng;

    @SerializedName("dropoff_latlng")
    private final String dropoffLatLng;

    @SerializedName("first_name")
    private final String firstName;

    @SerializedName("last_name")
    private final String lastName;

    @SerializedName("profile_picture")
    private final String profilePicture;

    public Passenger(String id, String checkpointID, String pickupLatLng, String dropoffLatLng, String firstName, String lastName, String profilePicture) {
        this.id = id;
        this.checkpointID = checkpointID;
        this.pickupLatLng = pickupLatLng;
        this.dropoffLatLng = dropoffLatLng;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePicture = profilePicture;
    }

    public String getId() {
        return id;
    }

    public String getCheckpointID() {
        return checkpointID;
    }

    public String getPickupLatLng() {
        return pickupLatLng;
    }

    public String getDropoffLatLng() {
        return dropoffLatLng;
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

    @Override
    public String toString() {
        return "Passenger{" +
                "id='" + id + '\'' +
                ", checkpointID='" + checkpointID + '\'' +
                ", pickupLatLng='" + pickupLatLng + '\'' +
                ", dropoffLatLng='" + dropoffLatLng + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }
}
