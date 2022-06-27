package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class RouteIDReq {
    @SerializedName("route_id")
    private String routeID;

    public RouteIDReq(String routeID) {
        this.routeID = routeID;
    }

    public String getRouteID() {
        return routeID;
    }

    @Override
    public String toString() {
        return "RouteIDReq{" +
                "routeID='" + routeID + '\'' +
                '}';
    }
}
