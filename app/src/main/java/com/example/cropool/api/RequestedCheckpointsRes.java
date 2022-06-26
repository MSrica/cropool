package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RequestedCheckpointsRes {
    @SerializedName("requested_checkpoints")
    ArrayList<SubscriptionRequest> requestedCheckpoints;

    public RequestedCheckpointsRes(ArrayList<SubscriptionRequest> requestedCheckpoints) {
        this.requestedCheckpoints = requestedCheckpoints;
    }

    public ArrayList<SubscriptionRequest> getRequestedCheckpoints() {
        return requestedCheckpoints;
    }

    @Override
    public String toString() {
        return "RequestedCheckpointsRes{" +
                "requestedCheckpoints=" + requestedCheckpoints +
                '}';
    }
}
