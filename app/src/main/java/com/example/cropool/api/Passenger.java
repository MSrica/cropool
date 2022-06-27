package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class Passenger {
    @SerializedName("idpassenger")
    private String id;

    @SerializedName("idcheckpoint")
    private String checkpointID;

    @SerializedName("pickup_latlng")
    private String pickupLatLng;

    @SerializedName("dropoff_latlng")
    private String dropoffLatLng;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("profile_picture")
    private String profilePicture;

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
