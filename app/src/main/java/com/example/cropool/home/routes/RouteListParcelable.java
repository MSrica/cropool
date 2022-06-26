package com.example.cropool.home.routes;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.cropool.api.Route;

import java.util.ArrayList;

public class RouteListParcelable implements Parcelable {
    public static final Creator<RouteListParcelable> CREATOR = new Creator<RouteListParcelable>() {
        @Override
        public RouteListParcelable createFromParcel(Parcel in) {
            return new RouteListParcelable(in);
        }

        @Override
        public RouteListParcelable[] newArray(int size) {
            return new RouteListParcelable[size];
        }
    };
    private ArrayList<Route> routes;
    private RouteType routesType;

    public RouteListParcelable(ArrayList<Route> routes, RouteType routesType) {
        this.routes = routes;
        this.routesType = routesType;
    }

    protected RouteListParcelable(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public RouteType getRoutesType() {
        return routesType;
    }

    @Override
    public String toString() {
        return "RouteListParcelable{" +
                "routes=" + routes +
                ", routesType=" + routesType +
                '}';
    }
}
