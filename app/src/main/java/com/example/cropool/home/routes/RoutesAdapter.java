package com.example.cropool.home.routes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cropool.BuildConfig;
import com.example.cropool.R;
import com.example.cropool.api.CheckpointIDReq;
import com.example.cropool.api.CheckpointReq;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Feedback;
import com.example.cropool.api.Passenger;
import com.example.cropool.api.RequestedCheckpointsRes;
import com.example.cropool.api.Route;
import com.example.cropool.api.RouteIDReq;
import com.example.cropool.api.Tokens;
import com.example.cropool.home.HomeActivity;
import com.example.cropool.home.messages.ChatActivity;
import com.example.cropool.home.messages.Conversation;
import com.example.cropool.home.subscription_requests.SubscriptionRequestListDialogFragment;
import com.example.cropool.home.subscription_requests.SubscriptionRequestListParcelable;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_RTDB_URL);
    private final List<Route> routes;
    private final Context context;
    private final FragmentActivity activity;
    private final RouteType routesType;
    private String startLatLng = "";
    private String finishLatLng = "";

    private String subscriptionIDToRemove = null;
    private View subscriptionViewToRemove = null;
    // Dialog for deleting/declining a user's request to subscribe to current user's route
    private final DialogInterface.OnClickListener removeExistingRequestedSubscriptionListener = (dialog, which) -> {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                // Unsubscribe
                removeSubscription(subscriptionIDToRemove, subscriptionViewToRemove, true);
                subscriptionIDToRemove = null;
                subscriptionViewToRemove = null;
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                // Reset subscriptionIDToRemove and subscriptionViewToRemove
                subscriptionIDToRemove = null;
                subscriptionViewToRemove = null;
                break;
        }
    };

    public RoutesAdapter(List<Route> routes, Context context, FragmentActivity activity, RouteType routesType) {
        this.routes = routes;
        this.context = context;
        this.activity = activity;
        this.routesType = routesType;
    }

    public RoutesAdapter(List<Route> routes, Context context, FragmentActivity activity, RouteType routesType, String startLatLng, String finishLatLng) {
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RoutesAdapter.MyViewHolder holder, int position) {
        Route route = routes.get(position);

        // Dialog for unsubscribing from a route
        DialogInterface.OnClickListener unsubscribeDialogListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Unsubscribe
                    unsubscribeFromRoute(holder.rootLayout, route, true);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // Do nothing
                    break;
            }
        };

        holder.routeName.setText(route.getName());

        String createdOnText = "Route created on " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(route.getCreatedAt());
        holder.routeCreatedOn.setText(createdOnText);

        holder.routeSchedule.setText(getScheduleText(route));

        // Set driver picture, onclick (chat) and onlongclick (show name)
        Picasso.get().load(route.getOwnerProfilePicture()).into(holder.routeDriver);
        holder.routeDriver.setOnClickListener(v -> {
            if (routesType.equals(RouteType.MY)) {
                // Current user is the driver - he shouldn't talk to himself (I guess...)
                return;
            }

            if (HomeActivity.getCurrentFBUser() == null) {
                Toast.makeText(context, "Please sign in again.", Toast.LENGTH_SHORT).show();
                return;
            }

            startChat(route.getOwnerFirstName() + " " + route.getOwnerLastName(), route.getOwnerProfilePicture(), HomeActivity.getCurrentFBUser().getUid(), route.getOwnerID());
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
            holder.routeAction1.setOnClickListener(v -> requestedCheckpoints(route.getId(), true));

            // TODO: Maybe an option to delete the route?
            holder.routeAction2.setVisibility(View.GONE);
        } else if (routesType.equals(RouteType.SUBSCRIBED_TO)) {
            holder.routeAction1.setVisibility(View.GONE);

            String action2Text = "Unsubscribe";
            holder.routeAction2.setText(action2Text);
            holder.routeAction2.setOnClickListener(v -> {
                // Manage unsubscribing via AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getResources().getString(R.string.UNSUBSCRIBE_CONFIRM))
                        .setPositiveButton(context.getResources().getString(R.string.YES), unsubscribeDialogListener)
                        .setNegativeButton(context.getResources().getString(R.string.NO), unsubscribeDialogListener)
                        .show();
            });
        } else {
            // RouteType.FOUND
            holder.routeAction2.setVisibility(View.GONE);

            holder.routeAction1.setTextColor(context.getResources().getColor(R.color.USER_ONLINE_COLOR));
            holder.routeAction1.setText(context.getResources().getString(R.string.subscribe));
            holder.routeAction1.setOnClickListener(v -> subscribeToRoute(route, true));
        }

        holder.map.setOnTouchListener((v, event) -> {
            holder.map.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        Bundle bundle = new Bundle();
        holder.map.onCreate(bundle);
        holder.map.getMapAsync(googleMap -> {
            if (route.getDirections() != null) {
                List<LatLng> points = decodePoly(route.getDirections());
                if (points.size() <= 0) {
                    return;
                }

                holder.mapPlaceholder.setVisibility(View.GONE);
                holder.map.setVisibility(View.VISIBLE);

                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.addAll(points);
                polylineOptions.width(12);
                polylineOptions.color((context != null) ? context.getResources().getColor(R.color.cropool_main) : Color.CYAN);

                googleMap.addPolyline(polylineOptions);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 9f));
            }


            holder.map.onResume();
        });
    }

    // Checking whether the chat between currentUserUID and otherUserUID exists and acting accordingly
    private void startChat(String name, String profilePicture, String currentUserUID, String otherUserUID) {
        if (currentUserUID == null || otherUserUID == null || currentUserUID.equals(otherUserUID)) {
            return;
        }

        databaseReference.child(context.getResources().getString(R.string.FB_RTDB_CHAT_TABLE_NAME)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String conversationID = "";
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Long user1UID = dataSnapshot.hasChild(context.getResources().getString(R.string.FB_RTDB_CHAT_USER_1_KEY)) ? dataSnapshot.child(context.getResources().getString(R.string.FB_RTDB_CHAT_USER_1_KEY)).getValue(Long.class) : null;
                    Long user2UID = dataSnapshot.hasChild(context.getResources().getString(R.string.FB_RTDB_CHAT_USER_2_KEY)) ? dataSnapshot.child(context.getResources().getString(R.string.FB_RTDB_CHAT_USER_2_KEY)).getValue(Long.class) : null;

                    if (user1UID != null && user2UID != null && ((user1UID.toString().equals(currentUserUID) && user2UID.toString().equals(otherUserUID)) || (user1UID.toString().equals(otherUserUID) && user2UID.toString().equals(currentUserUID)))) {
                        // conversationID for selected user combination already exists
                        conversationID = dataSnapshot.getKey();
                        break;
                    }
                }

                Conversation conversation = new Conversation(name, null, profilePicture, false, false, null, conversationID, currentUserUID, otherUserUID);

                Intent goToChat = new Intent(context, ChatActivity.class);
                goToChat.putExtra(context.getResources().getString(R.string.CHAT_ACTIVITY_INTENT_CONVERSATION_NAME), conversation);
                activity.startActivity(goToChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Retrieves requested checkpoints for a route
    private void requestedCheckpoints(String routeID, boolean refreshIfNeeded) {
        if (routeID == null) {
            Toast.makeText(context, "Sorry, there was an error. Try again.", Toast.LENGTH_LONG).show();
        }

        RouteIDReq routeIDReq = new RouteIDReq(routeID);
        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<RequestedCheckpointsRes> call = cropoolAPI.requestedCheckpoints(context.getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(context), routeIDReq);

        call.enqueue(new Callback<RequestedCheckpointsRes>() {
            @Override
            public void onResponse(@NotNull Call<RequestedCheckpointsRes> call, @NotNull Response<RequestedCheckpointsRes> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/requestedCheckpoints", "notSuccessful: Something went wrong. - " + response.code() + response);

                    if (response.code() == 403 || response.code() == 401) {
                        // Access or Firebase tokens invalid

                        // Try to refresh tokens using refresh tokens and re-run addRoute() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if (refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(activity, context, () -> {
                                requestedCheckpoints(routeID, false);
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

                RequestedCheckpointsRes requestedCheckpointsRes = response.body();

                if (response.code() == 200 && requestedCheckpointsRes != null && requestedCheckpointsRes.getRequestedCheckpoints().size() > 0) {   // Retreived the list
                    activity.getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, SubscriptionRequestListDialogFragment.newInstance(new SubscriptionRequestListParcelable(requestedCheckpointsRes.getRequestedCheckpoints()))).addToBackStack(null).commit();
                } else {
                    Toast.makeText(context, "No requests found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<RequestedCheckpointsRes> call, @NotNull Throwable t) {
                Toast.makeText(context, "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/requestedCheckpoints", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    // Removes an existing/requested subscription (checkpoint with checkpointID subscriptionIDToRemove)
    private void removeSubscription(String subscriptionIDToRemove, View viewToRemove, boolean refreshIfNeeded) {
        if (subscriptionIDToRemove == null) {
            Toast.makeText(context, "Sorry, there was an error. Try again.", Toast.LENGTH_LONG).show();
        }

        CheckpointIDReq checkpointIDReq = new CheckpointIDReq(subscriptionIDToRemove);
        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.removeCheckpoint(context.getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(context), checkpointIDReq);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/removeSubscription", "notSuccessful: Something went wrong. - " + response.code() + response);

                    if (response.code() == 403 || response.code() == 401) {
                        // Access or Firebase tokens invalid

                        // Try to refresh tokens using refresh tokens and re-run addRoute() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if (refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(activity, context, () -> {
                                removeSubscription(subscriptionIDToRemove, subscriptionViewToRemove, false);
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

                if (response.code() == 200) {   // Unsubscribed
                    Toast.makeText(context, (feedback != null) ? feedback.getFeedback() : "Successfully removed.", Toast.LENGTH_LONG).show();

                    // Hide the route we unsubscribed from
                    if (viewToRemove != null)
                        viewToRemove.setVisibility(View.GONE);
                } else {
                    Toast.makeText(context, "Sorry, couldn't remove.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                Toast.makeText(context, "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/removeSubscription", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    // Removes subscription to the route
    private void unsubscribeFromRoute(View rootLayout, Route route, boolean refreshIfNeeded) {
        CheckpointIDReq checkpointIDReq = new CheckpointIDReq(route.getSubscriptionCheckpointID());
        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.unsubscribeCheckpoint(context.getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(context), checkpointIDReq);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/unsubscribeFromRoute", "notSuccessful: Something went wrong. - " + response.code() + response);

                    if (response.code() == 403 || response.code() == 401) {
                        // Access or Firebase tokens invalid

                        // Try to refresh tokens using refresh tokens and re-run addRoute() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if (refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(activity, context, () -> {
                                unsubscribeFromRoute(rootLayout, route, false);
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

                if (response.code() == 200) {   // Unsubscribed
                    Toast.makeText(context, (feedback != null) ? feedback.getFeedback() : "Successfully unsubscribed.", Toast.LENGTH_LONG).show();

                    // Hide the route we unsubscribed from
                    rootLayout.setVisibility(View.GONE);
                } else {
                    Toast.makeText(context, "Sorry, couldn't unsubscribe.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                Toast.makeText(context, "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/unsubscribeFromRoute", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    // Sends request to subscribe to the route
    private void subscribeToRoute(Route route, boolean refreshIfNeeded) {
        if (startLatLng == null || finishLatLng == null) {
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
        @SuppressLint("DefaultLocale") String timeText = String.format("%02d", route.getStartHourOfDay()) + ":" + String.format("%02d", route.getStartMinuteOfHour());

        if (route.getRepetitionMode() == null || route.getCustomRepetition()) {
            return "Custom repetition: " + route.getNote();
        } else if (route.getRepetitionMode() == REPETITION_DAILY) {
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
            if (passenger.getProfilePicture() != null && !passenger.getProfilePicture().equals(context.getResources().getString(R.string.FB_RTDB_DEFAULT_PICTURE_VALUE)))
                Picasso.get().load(passenger.getProfilePicture()).into(passengerView);

            // Create parameters and set them to passenger image
            float dpFactor = context.getResources().getDisplayMetrics().density;


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (30 * dpFactor), (int) (30 * dpFactor));
            params.setMarginStart((int) (8 * dpFactor));
            passengerView.setLayoutParams(params);

            passengerView.setOnClickListener(v -> {
                if (HomeActivity.getCurrentFBUser() == null) {
                    Toast.makeText(context, "Please sign in again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                startChat(passenger.getFirstName() + " " + passenger.getLastName(), passenger.getProfilePicture(), HomeActivity.getCurrentFBUser().getUid(), passenger.getId());
            });

            passengerView.setOnLongClickListener(v -> {
                if (routesType.equals(RouteType.SUBSCRIBED_TO) || routesType.equals(RouteType.FOUND)) {
                    Toast.makeText(context, passenger.getFirstName() + " " + passenger.getLastName(), Toast.LENGTH_SHORT).show();
                } else if (routesType.equals(RouteType.MY)) {
                    subscriptionIDToRemove = passenger.getCheckpointID();
                    subscriptionViewToRemove = passengerView;

                    // Manage unsubscribing via AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(context.getResources().getString(R.string.REMOVE_SUBSCRIPTION))
                            .setPositiveButton(context.getResources().getString(R.string.YES), removeExistingRequestedSubscriptionListener)
                            .setNegativeButton(context.getResources().getString(R.string.NO), removeExistingRequestedSubscriptionListener)
                            .show();
                }

                return false;
            });

            holder.routePassengersLayout.addView(passengerView);
        }
    }

    // http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mapPlaceholder;
        private final MapView map;
        private final RelativeLayout rootLayout;
        private final TextView routeName, routeCreatedOn, routeSchedule, routePricePerKm, routePassengersLabel;
        private final LinearLayout routePassengersLayout;
        private final MaterialButton routeAction1, routeAction2;
        private final CircleImageView routeDriver;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            rootLayout = itemView.findViewById(R.id.route_card_root_layout);
            mapPlaceholder = itemView.findViewById(R.id.map_placeholder);
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
