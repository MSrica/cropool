package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class FindRouteReq {
    @SerializedName("pickup_latlng")
    private final String pickupLatLng;

    @SerializedName("dropoff_latlng")
    private final String dropoffLatLng;

    @SerializedName("max_price_per_km")
    private final Double maxPricePerKm;

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

    @SerializedName("pickup_timestamp_tolerance")
    private final Integer pickupSecondsTolerance;

    public FindRouteReq(String pickupLatLng, String dropoffLatLng, Double maxPricePerKm, Boolean customRepetition, Integer repetitionMode, Integer startMonth, Integer startDayOfMonth, Integer startDayOfWeek, Integer startHourOfDay, Integer startMinuteOfHour, Integer pickupSecondsTolerance) {
        this.pickupLatLng = pickupLatLng;
        this.dropoffLatLng = dropoffLatLng;
        this.maxPricePerKm = maxPricePerKm;
        this.customRepetition = customRepetition != null && customRepetition;
        this.repetitionMode = repetitionMode;
        this.startMonth = (startMonth == null) ? 1 : 0;
        this.startDayOfMonth = startDayOfMonth;
        this.startDayOfWeek = (startDayOfWeek == null) ? 1 : 0;
        this.startHourOfDay = startHourOfDay;
        this.startMinuteOfHour = startMinuteOfHour;
        this.pickupSecondsTolerance = pickupSecondsTolerance;
    }

    public String getPickupLatLng() {
        return pickupLatLng;
    }

    public String getDropoffLatLng() {
        return dropoffLatLng;
    }

    public Double getMaxPricePerKm() {
        return maxPricePerKm;
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

    public Integer getPickupSecondsTolerance() {
        return pickupSecondsTolerance;
    }

    @Override
    public String toString() {
        return "FindRouteReq{" +
                "pickupLatLng='" + pickupLatLng + '\'' +
                ", dropoffLatLng='" + dropoffLatLng + '\'' +
                ", maxPricePerKm=" + maxPricePerKm +
                ", customRepetition=" + customRepetition +
                ", repetitionMode=" + repetitionMode +
                ", startMonth=" + startMonth +
                ", startDayOfMonth=" + startDayOfMonth +
                ", startDayOfWeek=" + startDayOfWeek +
                ", startHourOfDay=" + startHourOfDay +
                ", startMinuteOfHour=" + startMinuteOfHour +
                ", pickupSecondsTolerance=" + pickupSecondsTolerance +
                '}';
    }
}
