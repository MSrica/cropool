package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class CheckpointReq {
    @SerializedName("id_route")
    private String routeID;

    @SerializedName("pickup_latlng")
    private String pickupLatLng;

    @SerializedName("dropoff_latlng")
    private String dropoffLatLng;

    public CheckpointReq(String routeID, String pickupLatLng, String dropoffLatLng) {
        this.routeID = routeID;
        this.pickupLatLng = pickupLatLng;
        this.dropoffLatLng = dropoffLatLng;
    }

    public String getRouteID() {
        return routeID;
    }

    public String getPickupLatLng() {
        return pickupLatLng;
    }

    public String getDropoffLatLng() {
        return dropoffLatLng;
    }

    @Override
    public String toString() {
        return "CheckpointReq{" +
                "routeID='" + routeID + '\'' +
                ", pickupLatLng='" + pickupLatLng + '\'' +
                ", dropoffLatLng='" + dropoffLatLng + '\'' +
                '}';
    }
}
