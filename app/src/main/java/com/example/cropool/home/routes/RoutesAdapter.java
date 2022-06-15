package com.example.cropool.home.routes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cropool.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.MyViewHolder> {
    private final List<Route> routes;
    private final Context context;
    private final int inflateResourceID;

    public RoutesAdapter(List<Route> routes, Context context, int inflateResourceID) {
        this.routes = routes;
        this.context = context;
        this.inflateResourceID = inflateResourceID;
    }


    @NonNull
    @Override
    public RoutesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Routes adapter will be used in different containers
        // hence we inflate a custom resource ID set through the constructor
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(inflateResourceID, null));
    }

    @Override
    public void onBindViewHolder(@NonNull RoutesAdapter.MyViewHolder holder, int position) {
        Route route = routes.get(position);

        // TODO: use holder.ELEMENT to edit the route's CardView
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;

        // TODO: change to MapView and show the map
        private final ImageView map;

        private final TextView routeName, routeCreatedOn, routeSchedule, routeDriver, routePassengers, routePricePerKm;
        private final MaterialButton rotueAction1, routeAction2;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.card);
            map = itemView.findViewById(R.id.map);
            routeName = itemView.findViewById(R.id.route_name);
            routeCreatedOn = itemView.findViewById(R.id.route_created_on);
            routeSchedule = itemView.findViewById(R.id.route_schedule);
            routeDriver = itemView.findViewById(R.id.route_driver);
            routePassengers = itemView.findViewById(R.id.route_passengers);
            routePricePerKm = itemView.findViewById(R.id.route_price_per_km);
            rotueAction1 = itemView.findViewById(R.id.route_action1);
            routeAction2 = itemView.findViewById(R.id.route_action2);
        }
    }
}
