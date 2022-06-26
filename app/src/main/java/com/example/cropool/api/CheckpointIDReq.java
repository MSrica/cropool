package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class CheckpointIDReq {
    @SerializedName("id_checkpoint")
    private String checkpointID;

    public CheckpointIDReq(String checkpointID) {
        this.checkpointID = checkpointID;
    }

    public String getCheckpointID() {
        return checkpointID;
    }

    @Override
    public String toString() {
        return "CheckpointIDReq{" +
                "checkpointID='" + checkpointID + '\'' +
                '}';
    }
}
