package com.example.cropool.api;

import com.google.gson.annotations.SerializedName;

public class AddRouteReq {
    @SerializedName("id_owner")
    private String routeOwnerID;

    @SerializedName("name")
    private String routeName;

    @SerializedName("max_num_passengers")
    private int maxNumberPassengers;

    @SerializedName("start_latlng")
    private String startLatLng;

    @SerializedName("finish_latlng")
    private String finishLatLng;

    @SerializedName("repetition_mode")
    private int repetitionMode;

    @SerializedName("price_per_km")
    private Double pricePerKm;

    @SerializedName("custom_repetition")
    private boolean customRepetition;

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

    public AddRouteReq(String routeOwnerID, String routeName, int maxNumberPassengers, String startLatLng, String finishLatLng, int repetitionMode, Double pricePerKm, boolean customRepetition, Integer startMonth, Integer startDayOfMonth, Integer startDayOfWeek, Integer startHourOfDay, Integer startMinuteOfHour, String note) {
        this.routeOwnerID = routeOwnerID;
        this.routeName = routeName;
        this.maxNumberPassengers = maxNumberPassengers;
        this.startLatLng = startLatLng;
        this.finishLatLng = finishLatLng;
        this.repetitionMode = repetitionMode;
        this.pricePerKm = pricePerKm;
        this.customRepetition = customRepetition;
        this.startMonth = (startMonth == null) ? 1 : startMonth;
        this.startDayOfMonth = (startDayOfMonth == null) ? 1 : startDayOfMonth;
        this.startDayOfWeek = (startDayOfWeek == null) ? 1 : startDayOfWeek;
        this.startHourOfDay = startHourOfDay;
        this.startMinuteOfHour = startMinuteOfHour;
        this.note = note;
    }

    public String getRouteOwnerID() {
        return routeOwnerID;
    }

    public String getRouteName() {
        return routeName;
    }

    public int getMaxNumberPassengers() {
        return maxNumberPassengers;
    }

    public String getStartLatLng() {
        return startLatLng;
    }

    public String getFinishLatLng() {
        return finishLatLng;
    }

    public int getRepetitionMode() {
        return repetitionMode;
    }

    public Double getPricePerKm() {
        return pricePerKm;
    }

    public boolean getCustomRepetition() {
        return customRepetition;
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

    @Override
    public String toString() {
        return "AddRouteReq{" +
                "routeOwnerID='" + routeOwnerID + '\'' +
                ", routeName='" + routeName + '\'' +
                ", maxNumberPassengers=" + maxNumberPassengers +
                ", startLatLng='" + startLatLng + '\'' +
                ", finishLatLng='" + finishLatLng + '\'' +
                ", repetitionMode=" + repetitionMode +
                ", pricePerKm=" + pricePerKm +
                ", customRepetition='" + customRepetition + '\'' +
                ", startMonth=" + startMonth +
                ", startDayOfMonth=" + startDayOfMonth +
                ", startDayOfWeek=" + startDayOfWeek +
                ", startHourOfDay=" + startHourOfDay +
                ", startMinuteOfHour=" + startMinuteOfHour +
                ", note='" + note + '\'' +
                '}';
    }
}
