package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class FindRouteRes {
    @SerializedName("result")
    private ArrayList<Route> resultingRoutes;

    @SerializedName("feedback")
    private String feedback;

    public FindRouteRes(ArrayList<Route> resultingRoutes, String feedback) {
        this.resultingRoutes = resultingRoutes;
        this.feedback = feedback;
    }

    public ArrayList<Route> getResultingRoutes() {
        return resultingRoutes;
    }

    public String getFeedback() {
        return feedback;
    }

    @Override
    public String toString() {
        return "FindRouteRes{" +
                "resultingRoutes=" + resultingRoutes +
                ", feedback=" + feedback +
                '}';
    }
}
