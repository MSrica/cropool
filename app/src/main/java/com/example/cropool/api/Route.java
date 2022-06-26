package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Route {
    @SerializedName("idroute")
    private String id;

    @SerializedName("idowner")
    private String ownerID;

    @SerializedName("owner_first_name")
    private String ownerFirstName;

    @SerializedName("owner_last_name")
    private String ownerLastName;

    @SerializedName("owner_profile_picture")
    private String ownerProfilePicture;

    @SerializedName("start_latlng")
    private String startLatLng;

    @SerializedName("finish_latlng")
    private String finishLatLng;

    @SerializedName("name")
    private String name;

    @SerializedName("created_at")
    private Long createdAt;

    @SerializedName("custom_repetition")
    private Boolean customRepetition;

    @SerializedName("repetition_mode")
    private Integer repetitionMode;

    @SerializedName("start_month")
    private Integer startMonth;

    @SerializedName("start_day_of_month")
    private Integer startDayOfMonth;

    @SerializedName("start_day_of_week")
    private Integer startDayOfWeek;

    @SerializedName("start_hour_of_day")
    private Integer startHourOfDay;

    @SerializedName("start_minute_of_hour")
    private Integer startMinuteOfHour;

    @SerializedName("note")
    private String note;

    @SerializedName("price_per_km")
    private Double pricePerKm;

    @SerializedName("passengers")
    private ArrayList<Passenger> passengers;

    @SerializedName("devPercentage")
    private Double devPercentage;

    @SerializedName("directions")
    private String directions;

    public Route(String id, String ownerID, String ownerFirstName, String ownerLastName, String ownerProfilePicture, String startLatLng, String finishLatLng, String name, Long createdAt, Boolean customRepetition, Integer repetitionMode, Integer startMonth, Integer startDayOfMonth, Integer startDayOfWeek, Integer startHourOfDay, Integer startMinuteOfHour, String note, Double pricePerKm, ArrayList<Passenger> passengers, Double devPercentage, String directions) {
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
                '}';
    }
}
