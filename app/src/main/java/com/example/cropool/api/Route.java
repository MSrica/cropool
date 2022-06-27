package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Route {
    @SerializedName("idroute")
    private final String id;

    @SerializedName("idowner")
    private final String ownerID;

    @SerializedName("owner_first_name")
    private final String ownerFirstName;

    @SerializedName("owner_last_name")
    private final String ownerLastName;

    @SerializedName("owner_profile_picture")
    private final String ownerProfilePicture;

    @SerializedName("start_latlng")
    private final String startLatLng;

    @SerializedName("finish_latlng")
    private final String finishLatLng;

    @SerializedName("name")
    private final String name;

    @SerializedName("created_at")
    private final Long createdAt;

    @SerializedName("custom_repetition")
    private final Boolean customRepetition;

    @SerializedName("repetition_mode")
    private final Integer repetitionMode;

    @SerializedName("start_month")
    private final Integer startMonth;

    @SerializedName("start_day_of_month")
    private final Integer startDayOfMonth;

    @SerializedName("start_day_of_week")
    private final Integer startDayOfWeek;

    @SerializedName("start_hour_of_day")
    private final Integer startHourOfDay;

    @SerializedName("start_minute_of_hour")
    private final Integer startMinuteOfHour;

    @SerializedName("note")
    private final String note;

    @SerializedName("price_per_km")
    private final Double pricePerKm;

    @SerializedName("passengers")
    private final ArrayList<Passenger> passengers;

    @SerializedName("devPercentage")
    private final Double devPercentage;

    @SerializedName("directions")
    private final String directions;

    @SerializedName("idcheckpoint")
    private final String subscriptionCheckpointID;

    public Route(String id, String ownerID, String ownerFirstName, String ownerLastName, String ownerProfilePicture, String startLatLng, String finishLatLng, String name, Long createdAt, Boolean customRepetition, Integer repetitionMode, Integer startMonth, Integer startDayOfMonth, Integer startDayOfWeek, Integer startHourOfDay, Integer startMinuteOfHour, String note, Double pricePerKm, ArrayList<Passenger> passengers, Double devPercentage, String directions, String subscriptionCheckpointID) {
        this.id = id;
        this.ownerID = ownerID;
        this.ownerFirstName = ownerFirstName;
        this.ownerLastName = ownerLastName;
        this.ownerProfilePicture = ownerProfilePicture;
        this.startLatLng = startLatLng;
        this.finishLatLng = finishLatLng;
        this.name = name;
        this.createdAt = createdAt;
        this.customRepetition = customRepetition;
        this.repetitionMode = repetitionMode;
        this.startMonth = startMonth;
        this.startDayOfMonth = startDayOfMonth;
        this.startDayOfWeek = startDayOfWeek;
        this.startHourOfDay = startHourOfDay;
        this.startMinuteOfHour = startMinuteOfHour;
        this.note = note;
        this.pricePerKm = pricePerKm;
        this.passengers = passengers;
        this.devPercentage = devPercentage;
        this.directions = directions;
        this.subscriptionCheckpointID = subscriptionCheckpointID;
    }

    public String getId() {
        return id;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getOwnerFirstName() {
        return ownerFirstName;
    }

    public String getOwnerLastName() {
        return ownerLastName;
    }

    public String getOwnerProfilePicture() {
        return ownerProfilePicture;
    }

    public String getStartLatLng() {
        return startLatLng;
    }

    public String getFinishLatLng() {
        return finishLatLng;
    }

    public String getName() {
        return name;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Boolean getCustomRepetition() {
        return customRepetition;
    }

    public Integer getRepetitionMode() {
        return repetitionMode;
    }

    public Integer getStartMonth() {
        return startMonth;
    }

    public Integer getStartDayOfMonth() {
        return startDayOfMonth;
    }

    public Integer getStartDayOfWeek() {
        return startDayOfWeek;
    }

    public Integer getStartHourOfDay() {
        return startHourOfDay;
    }

    public Integer getStartMinuteOfHour() {
        return startMinuteOfHour;
    }

    public String getNote() {
        return note;
    }

    public Double getPricePerKm() {
        return pricePerKm;
    }

    public ArrayList<Passenger> getPassengers() {
        return passengers;
    }

    public Double getDevPercentage() {
        return devPercentage;
    }

    public String getDirections() {
        return directions;
    }

    public String getSubscriptionCheckpointID() {
        return subscriptionCheckpointID;
    }

    @Override
    public String toString() {
        return "Route{" +
                "id='" + id + '\'' +
                ", ownerID='" + ownerID + '\'' +
                ", ownerFirstName='" + ownerFirstName + '\'' +
                ", ownerLastName='" + ownerLastName + '\'' +
                ", ownerProfilePicture='" + ownerProfilePicture + '\'' +
                ", startLatLng='" + startLatLng + '\'' +
                ", finishLatLng='" + finishLatLng + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", customRepetition=" + customRepetition +
                ", repetitionMode=" + repetitionMode +
                ", startMonth=" + startMonth +
                ", startDayOfMonth=" + startDayOfMonth +
                ", startDayOfWeek=" + startDayOfWeek +
                ", startHourOfDay=" + startHourOfDay +
                ", startMinuteOfHour=" + startMinuteOfHour +
                ", note='" + note + '\'' +
                ", pricePerKm=" + pricePerKm +
                ", passengers=" + passengers +
                ", devPercentage=" + devPercentage +
                ", directions='" + directions + '\'' +
                ", subscriptionCheckpointID='" + subscriptionCheckpointID + '\'' +
                '}';
    }
}
