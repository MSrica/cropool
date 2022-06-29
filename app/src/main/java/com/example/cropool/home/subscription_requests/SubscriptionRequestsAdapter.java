package com.example.cropool.home.subscription_requests;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cropool.R;
import com.example.cropool.api.CheckpointIDReq;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Feedback;
import com.example.cropool.api.SubscriptionRequest;
import com.example.cropool.api.Tokens;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SubscriptionRequestsAdapter extends RecyclerView.Adapter<SubscriptionRequestsAdapter.MyViewHolder> {
    private final List<SubscriptionRequest> subscriptionRequests;
    private final Context context;
    private final Activity activity;

    public SubscriptionRequestsAdapter(List<SubscriptionRequest> subscriptionRequests, Context context, Activity activity) {
        this.subscriptionRequests = subscriptionRequests;
        this.context = context;
        this.activity = activity;
    }

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

    @NonNull
    @Override
    public SubscriptionRequestsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.subscription_request_item_adapter_layout, null));
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionRequestsAdapter.MyViewHolder holder, int position) {
        SubscriptionRequest subscriptionRequest = subscriptionRequests.get(position);

        if (context != null && subscriptionRequest.getProfilePicture() != null && !subscriptionRequest.getProfilePicture().equals(context.getResources().getString(R.string.FB_RTDB_DEFAULT_PICTURE_VALUE)))
            Picasso.get().load(subscriptionRequest.getProfilePicture()).into(holder.profilePicture);

        String nameText = subscriptionRequest.getFirstName() + " " + subscriptionRequest.getLastName();
        holder.name.setText(nameText);

        String fromTo = "From: " + subscriptionRequest.getStartLatLng() + "\nTo: " + subscriptionRequest.getFinishLatLng();
        if (context != null && subscriptionRequest.getStartLatLng() != null && subscriptionRequest.getFinishLatLng() != null) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            String[] pos1 = subscriptionRequest.getStartLatLng().split(",");
            String[] pos2 = subscriptionRequest.getFinishLatLng().split(",");

            if (pos1.length == 2 || pos2.length == 2) {
                try {
                    List<Address> addresses1 = geocoder.getFromLocation(Double.parseDouble(pos1[0]), Double.parseDouble(pos1[1]), 1);
                    List<Address> addresses2 = geocoder.getFromLocation(Double.parseDouble(pos2[0]), Double.parseDouble(pos2[1]), 1);
                    fromTo = "From: " + addresses1.get(0).getAddressLine(0) + "\nTo: " + addresses2.get(0).getAddressLine(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        holder.description.setText(fromTo);

        holder.accept.setOnClickListener(v -> {
            acceptSubscription(subscriptionRequest.getCheckpointID(), holder.rootLayout, true);
        });

        holder.decline.setOnClickListener(v -> {
            subscriptionIDToRemove = subscriptionRequest.getCheckpointID();
            subscriptionViewToRemove = holder.rootLayout;

            // Manage unsubscribing via AlertDialog
            if(context != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getResources().getString(R.string.DECLINE_SUBSCRIPTION))
                        .setPositiveButton(context.getResources().getString(R.string.YES), removeExistingRequestedSubscriptionListener)
                        .setNegativeButton(context.getResources().getString(R.string.NO), removeExistingRequestedSubscriptionListener)
                        .show();
            }
        });
    }

    // Removes an existing/requested subscription (checkpoint with checkpointID subscriptionIDToRemove)
    private void removeSubscription(String subscriptionIDToRemove, View viewToRemove, boolean refreshIfNeeded) {
        if (subscriptionIDToRemove == null) {
            if (context != null)
                Toast.makeText(context, "Sorry, there was an error. Try again.", Toast.LENGTH_LONG).show();
            return;
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
                    Toast.makeText(context, (feedback != null) ? feedback.getFeedback() : "Successfully declined.", Toast.LENGTH_LONG).show();

                    // Hide the route we unsubscribed from
                    if (viewToRemove != null)
                        viewToRemove.setVisibility(View.GONE);
                } else {
                    Toast.makeText(context, "Sorry, couldn't decline.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                Toast.makeText(context, "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/removeSubscription", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    // Accepts the requested subscription (checkpoint with checkpointID subscriptionIDToAccept)
    private void acceptSubscription(String subscriptionIDToRemove, View viewToRemove, boolean refreshIfNeeded) {
        if (subscriptionIDToRemove == null) {
            if (context != null)
                Toast.makeText(context, "Sorry, there was an error. Try again.", Toast.LENGTH_LONG).show();
            return;
        }

        CheckpointIDReq checkpointIDReq = new CheckpointIDReq(subscriptionIDToRemove);
        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.acceptCheckpoint(context.getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(context), checkpointIDReq);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/acceptSubscription", "notSuccessful: Something went wrong. - " + response.code() + response);

                    if (response.code() == 403 || response.code() == 401) {
                        // Access or Firebase tokens invalid

                        // Try to refresh tokens using refresh tokens and re-run addRoute() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if (refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(activity, context, () -> {
                                acceptSubscription(subscriptionIDToRemove, subscriptionViewToRemove, false);
                                return null;
                            });
                        } else {
                            Toast.makeText(context, "Sorry, there was an error. Please check your maximum capacity. " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(context, "Sorry, there was an error. Please check your maximum capacity. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                Feedback feedback = response.body();

                if (response.code() == 200) {   // Accepted
                    Toast.makeText(context, (feedback != null) ? feedback.getFeedback() : "Request accepted.", Toast.LENGTH_LONG).show();

                    // Hide the request we accepted
                    if (viewToRemove != null)
                        viewToRemove.setVisibility(View.GONE);
                } else {
                    Toast.makeText(context, "Sorry, couldn't accept.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                Toast.makeText(context, "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/acceptSubscription", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return subscriptionRequests.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout rootLayout;
        private final CircleImageView profilePicture;
        private final TextView name;
        private final TextView description;
        private final ImageView accept, decline;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            this.rootLayout = itemView.findViewById(R.id.subscription_request_root_layout);
            this.profilePicture = itemView.findViewById(R.id.subscription_request_profile_picture);
            this.name = itemView.findViewById(R.id.subscription_request_name);
            this.description = itemView.findViewById(R.id.subscription_request_description);
            this.accept = itemView.findViewById(R.id.subscription_request_accept);
            this.decline = itemView.findViewById(R.id.subscription_request_decline);
        }
    }
}
