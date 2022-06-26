package com.example.cropool.home.routes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cropool.R;
import com.example.cropool.api.CheckpointReq;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Feedback;
import com.example.cropool.api.Passenger;
import com.example.cropool.api.Route;
import com.example.cropool.api.Tokens;
import com.example.cropool.home.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.MyViewHolder> {

    private static final int REPETITION_DAILY = 1;
    private static final int REPETITION_MONTHLY = 2;

    private final List<Route> routes;
    private final Context context;
    private final Activity activity;
    private final RouteType routesType;
    private String startLatLng = "";
    private String finishLatLng = "";

    public RoutesAdapter(List<Route> routes, Context context, Activity activity, RouteType routesType) {
        this.routes = routes;
        this.context = context;
        this.activity = activity;
        this.routesType = routesType;
    }

    public RoutesAdapter(List<Route> routes, Context context, Activity activity, RouteType routesType, String startLatLng, String finishLatLng) {
        this.routes = routes;
        this.context = context;
        this.activity = activity;
        this.routesType = routesType;
        this.startLatLng = startLatLng;
        this.finishLatLng = finishLatLng;
    }


    @NonNull
    @Override
    public RoutesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Routes adapter will be used in different containers
        // hence we inflate a custom resource ID set through the constructor
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.route_item_adapter_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull RoutesAdapter.MyViewHolder holder, int position) {
        Route route = routes.get(position);

        holder.routeName.setText(route.getName());

        String createdOnText = "Route created on " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(route.getCreatedAt());
        holder.routeCreatedOn.setText(createdOnText);

        holder.routeSchedule.setText(getScheduleText(route));

        // Set driver picture, onclick (chat) and onlongclick (show name)
        Picasso.get().load(route.getOwnerProfilePicture()).into(holder.routeDriver);
        holder.routeDriver.setOnClickListener(v -> {
            Toast.makeText(context, "Start chat to " + route.getOwnerID(), Toast.LENGTH_LONG).show();
        });
        holder.routeDriver.setOnLongClickListener(v -> {
            Toast.makeText(context, route.getOwnerFirstName() + " " + route.getOwnerLastName(), Toast.LENGTH_LONG).show();
            return false;
        });

        String priceText = route.getPricePerKm() + "€";
        holder.routePricePerKm.setText(priceText);

        showRoutePassengers(holder, route.getPassengers());

        if (routesType.equals(RouteType.MY)) {
            String action1Text = "See requests";
            holder.routeAction1.setText(action1Text);
            holder.routeAction1.setTextColor(context.getResources().getColor(R.color.USER_ONLINE_COLOR));
            holder.routeAction1.setOnClickListener(v -> {
                Toast.makeText(context, "Popup recycler view confirm/decline with passenger name, start, finish place", Toast.LENGTH_LONG).show();
            });

            // TODO: Maybe an option to delete the route?
            holder.routeAction2.setVisibility(View.GONE);
        } else if (routesType.equals(RouteType.SUBSCRIBED_TO)) {

        } else {
            // RouteType.FOUND
            holder.routeAction2.setVisibility(View.GONE);

            holder.routeAction1.setTextColor(context.getResources().getColor(R.color.USER_ONLINE_COLOR));
            holder.routeAction1.setText(context.getResources().getString(R.string.subscribe));
            holder.routeAction1.setOnClickListener(v -> subscribeToRoute(route, true));
        }
    }

    private void subscribeToRoute(Route route, boolean refreshIfNeeded) {
        if (HomeActivity.getCurrentFBUser() == null || startLatLng == null || finishLatLng == null) {
            Toast.makeText(context, "There was an error, please sign in again.", Toast.LENGTH_LONG).show();
            return;
        }

        CheckpointReq checkpointReq = new CheckpointReq(route.getId(), startLatLng, finishLatLng);
        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.requestCheckpoint(context.getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(context), checkpointReq);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/subscribeToRoute", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 403) {
                        // Access or Firebase tokens invalid

                        // Try to refresh tokens using refresh tokens and re-run addRoute() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if (refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(activity, context, () -> {
                                subscribeToRoute(route, false);
                                return null;
                            });
                        } else {
                            Toast.makeText(context, "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(context, "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                Feedback feedback = response.body();

                if (response.code() == 201) {   // Checkpoint requested
                    Toast.makeText(context, (feedback != null) ? feedback.getFeedback() : "Subscription request sent.", Toast.LENGTH_LONG).show();

                    // Go back to find route fragment (can't replace fragment because of navigation bar)
                    activity.startActivity(new Intent(context, HomeActivity.class));
                    activity.finish();
                } else {
                    Toast.makeText(context, "Sorry, subscription couldn't be requested.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                Toast.makeText(context, "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/subscribeToRoute", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    // Generates route schedule text information relative to repetition mode etc.
    private String getScheduleText(Route route) {
        String timeText = String.format("%02d", route.getStartHourOfDay()) + ":" + String.format("%02d", route.getStartMinuteOfHour());

        if (route.getRepetitionMode() == REPETITION_DAILY) {
            return "Daily at " + timeText + ".";
        } else if (route.getRepetitionMode() == REPETITION_MONTHLY) {
            String suffix = "th";

            switch (route.getStartDayOfMonth()) {
                case 1:
                    suffix = "st";
                    break;
                case 2:
                    suffix = "nd";
                    break;
                case 3:
                    suffix = "rd";
                    break;
            }

            return "Monthly on " + route.getStartDayOfMonth() + suffix + " at " + timeText + ".";
        } else if (route.getCustomRepetition()) {
            return "Custom repetition: " + route.getNote();
        } else {
            return getDaysFromCode(route.getRepetitionMode()) + " at " + timeText + ".";
        }
    }

    // Helper function to getScheduleText
    private String getDaysFromCode(int code) {
        ArrayList<String> days = new ArrayList<>();

        if (code >= 1000000) {
            days.add("Monday");
            code -= 1000000;
        }

        if (code >= 200000) {
            days.add("Tuesday");
            code -= 200000;
        }

        if (code >= 30000) {
            days.add("Wednesday");
            code -= 30000;
        }

        if (code >= 4000) {
            days.add("Thursday");
            code -= 4000;
        }

        if (code >= 500) {
            days.add("Friday");
            code -= 500;
        }

        if (code >= 60) {
            days.add("Saturday");
            code -= 1000000;
        }

        if (code >= 60) {
            days.add("Sunday");
            code -= 60;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.size(); ++i) {
            sb.append(days.get(i));

            if (i == (days.size() - 2)) {
                // Second to last day added, final day is next
                sb.append(" and ");
            } else if (days.size() > 1) {
                // We add ", " after days before second to last,
                // unless there is just one day in the code
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    // Creates passenger visualizations relative to downloaded passenger information
    private void showRoutePassengers(@NonNull RoutesAdapter.MyViewHolder holder, ArrayList<Passenger> passengers) {
        if (passengers == null || passengers.size() <= 0) {
            holder.routePassengersLayout.setVisibility(View.GONE);
            holder.routePassengersLabel.setVisibility(View.GONE);
            return;
        }

        for (Passenger passenger : passengers) {
            CircleImageView passengerView = new CircleImageView(context);

            // Set the picture
            Picasso.get().load(passenger.getProfilePicture()).into(passengerView);

            // Create parameters and set them to passenger image
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(35, 35);
            params.setMarginStart(4);
            passengerView.setLayoutParams(params);

            passengerView.setOnClickListener(v -> {
                Toast.makeText(context, "Start chat to " + passenger.getId(), Toast.LENGTH_SHORT).show();
            });

            passengerView.setOnLongClickListener(v -> {
                Toast.makeText(context, passenger.getFirstName() + " " + passenger.getLastName() + "/REMOVE", Toast.LENGTH_SHORT).show();
                return false;
            });

            holder.routePassengersLayout.addView(passengerView);
        }
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;

        // TODO: change to MapView and show the map
        private final ImageView map;

        private final TextView routeName, routeCreatedOn, routeSchedule, routePricePerKm, routePassengersLabel;
        private final LinearLayout routePassengersLayout;
        private final MaterialButton routeAction1, routeAction2;
        private final CircleImageView routeDriver;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.card);
            map = itemView.findViewById(R.id.map);
            routeName = itemView.findViewById(R.id.route_name);
            routeCreatedOn = itemView.findViewById(R.id.route_created_on);
            routeSchedule = itemView.findViewById(R.id.route_schedule);
            routeDriver = itemView.findViewById(R.id.driver_profile_picture);
            routePassengersLayout = itemView.findViewById(R.id.route_passengers_layout);
            routePassengersLabel = itemView.findViewById(R.id.route_passengers_label);
            routePricePerKm = itemView.findViewById(R.id.route_price_per_km);
            routeAction1 = itemView.findViewById(R.id.route_action1);
            routeAction2 = itemView.findViewById(R.id.route_action2);
        }
    }
}
