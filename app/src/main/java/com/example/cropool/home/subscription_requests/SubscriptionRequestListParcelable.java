package com.example.cropool.home.subscription_requests;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.cropool.api.SubscriptionRequest;

import java.util.ArrayList;

public class SubscriptionRequestListParcelable implements Parcelable {
    public static final Creator<SubscriptionRequestListParcelable> CREATOR = new Creator<SubscriptionRequestListParcelable>() {
        @Override
        public SubscriptionRequestListParcelable createFromParcel(Parcel in) {
            return new SubscriptionRequestListParcelable(in);
        }

        @Override
        public SubscriptionRequestListParcelable[] newArray(int size) {
            return new SubscriptionRequestListParcelable[size];
        }
    };
    private ArrayList<SubscriptionRequest> subscriptionRequests;

    public SubscriptionRequestListParcelable(ArrayList<SubscriptionRequest> subscriptionRequests) {
        this.subscriptionRequests = subscriptionRequests;
    }

    protected SubscriptionRequestListParcelable(Parcel in) {
    }

    public ArrayList<SubscriptionRequest> getSubscriptionRequests() {
        return subscriptionRequests;
    }

    @Override
    public String toString() {
        return "SubscriptionRequestListParcelable{" +
                "subscriptionRequests=" + subscriptionRequests +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
