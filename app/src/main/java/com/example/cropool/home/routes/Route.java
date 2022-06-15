package com.example.cropool.home.routes;

public class Route {
    private final String name, createdOn, schedule, driver, passengers, pricePerKm;
    // TODO: Additional data for creating a MapView

    public Route(String name, String createdOn, String schedule, String driver, String passengers, String pricePerKm) {
        this.name = name;
        this.createdOn = createdOn;
        this.schedule = schedule;
        this.driver = driver;
        this.passengers = passengers;
        this.pricePerKm = pricePerKm;
    }

    public String getName() {
        return name;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public String getSchedule() {
        return schedule;
    }

    public String getDriver() {
        return driver;
    }

    public String getPassengers() {
        return passengers;
    }

    public String getPricePerKm() {
        return pricePerKm;
    }

    @Override
    public String toString() {
        return "Route{" +
                "name='" + name + '\'' +
                ", createdOn='" + createdOn + '\'' +
                ", schedule='" + schedule + '\'' +
                ", driver='" + driver + '\'' +
                ", passengers='" + passengers + '\'' +
                ", pricePerKm='" + pricePerKm + '\'' +
                '}';
    }
}
