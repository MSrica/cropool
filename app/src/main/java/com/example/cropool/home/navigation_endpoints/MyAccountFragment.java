package com.example.cropool.home.navigation_endpoints;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.cropool.BuildConfig;
import com.example.cropool.R;
import com.example.cropool.api.AccountInfo;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Feedback;
import com.example.cropool.api.Tokens;
import com.example.cropool.home.my_account.MyRoutesFragment;
import com.example.cropool.home.my_account.PersonalDetailsFragment;
import com.example.cropool.home.my_account.RoutesSubscribedToFragment;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyAccountFragment extends Fragment {

    MaterialCardView routesSubscribedTo, myRoutes, personalDetails, helpSupport, signOut;
    private CircleImageView profilePicture, editProfilePicture;
    private TextView name, routesQty, routesQtyLabel, membership, membershipLabel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_account, container, false);

        name = v.findViewById(R.id.account_name);
        profilePicture = v.findViewById(R.id.account_profile_picture);
        editProfilePicture = v.findViewById(R.id.edit);
        routesQty = v.findViewById(R.id.route_qty);
        routesQtyLabel = v.findViewById(R.id.route_label);
        membership = v.findViewById(R.id.membership_time);
        membershipLabel = v.findViewById(R.id.membership_label);
        routesSubscribedTo = v.findViewById(R.id.routes_subscribed);
        myRoutes = v.findViewById(R.id.my_routes);
        personalDetails = v.findViewById(R.id.personal_details);
        helpSupport = v.findViewById(R.id.help_support);
        signOut = v.findViewById(R.id.sign_out);

        // Dialog for logout from all devices query
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Sign out the user from all devices
                    signOut(true);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // Sign out the user from this device only
                    Tokens.logoutProcedure(requireContext(), requireActivity());

                    break;
            }
        };

        updateUserData();

        routesSubscribedTo.setOnClickListener(v1 -> requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new RoutesSubscribedToFragment()).addToBackStack(null).commit());

        myRoutes.setOnClickListener(v1 -> requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new MyRoutesFragment()).addToBackStack(null).commit());

        personalDetails.setOnClickListener(v1 -> requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new PersonalDetailsFragment()).addToBackStack(null).commit());

        helpSupport.setOnClickListener(v1 -> {
            // Open browser using help and support URL
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(BuildConfig.HELP_AND_SUPPORT_URL));
            startActivity(i);
        });

        signOut.setOnClickListener(v1 -> {
            // Manage logging out via AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(getResources().getString(R.string.LOGOUT_FROM_ALL_DIALOG))
                    .setPositiveButton(getResources().getString(R.string.YES), dialogClickListener)
                    .setNegativeButton(getResources().getString(R.string.NO), dialogClickListener)
                    .show();
        });


        return v;
    }

    // Populates account related TextViews and ImageView with content
    private void updateUserData() {
        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<AccountInfo> call = cropoolAPI.accountInfo(requireContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(requireContext()),
                requireContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getFirebaseToken(requireContext()));

        call.enqueue(new Callback<AccountInfo>() {
            @Override
            public void onResponse(@NotNull Call<AccountInfo> call, @NotNull Response<AccountInfo> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/accountInfo", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 403) {
                        // Access or Firebase tokens invalid
                        // Toast.makeText(getContext(), "Refreshing tokens...", Toast.LENGTH_SHORT).show();

                        // Try to refresh tokens using refresh tokens and re-run updateUserData() if refreshing is successful
                        Tokens.refreshTokensOnServer(requireActivity(), requireContext(), () -> {
                            updateUserData();
                            return null;
                        });
                    } else {
                        Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                AccountInfo accountInfo = response.body();

                if (accountInfo == null) {
                    Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.code() == 201) {   // User info downloaded
                    String displayName = accountInfo.getFirstName() + " " + accountInfo.getLastName() + "!";
                    name.setText(displayName);

                    if (!accountInfo.getProfilePicture().equals(getResources().getString(R.string.FB_RTDB_DEFAULT_PICTURE_VALUE)))
                        Picasso.get().load(accountInfo.getProfilePicture()).into(profilePicture);

                    // We don't want it to say '1 year ago' but '1 year'
                    String membershipTime = TimeAgo.using(accountInfo.getCreatedAt() * 1000L).replace(" ago", "");
                    membership.setText(membershipTime);
                } else {
                    Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<AccountInfo> call, @NotNull Throwable t) {
                Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/accountInfo", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    // Signs the user out from all devices
    private void signOut(boolean refreshIfNeeded) {
        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.signOut(requireContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(requireContext()));

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/signOut", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 403) {
                        // Access token invalid

                        // First we'll refresh the tokens (if refreshIfNeeded is true)
                        if (refreshIfNeeded) {
                            Toast.makeText(getContext(), "Refreshing tokens...", Toast.LENGTH_SHORT).show();

                            // Try to refresh tokens using refresh tokens and re-run updateUserData() if refreshing is successful
                            Tokens.refreshTokensOnServer(requireActivity(), requireContext(), () -> {
                                // We don't want to refresh tokens multiple times just because we encountered 403 status code
                                signOut(false);
                                return null;
                            });
                        } else {
                            // We already refreshed the tokens so something else failed.
                            Toast.makeText(getContext(), "Sorry, can't sign out.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                Feedback feedback = response.body();

                if (response.code() == 201) {   // User signed out
                    Toast.makeText(getContext(), (feedback != null) ? feedback.getFeedback() : "User signed out from all devices.", Toast.LENGTH_LONG).show();

                    // Logout procedure
                    Tokens.logoutProcedure(requireContext(), requireActivity());
                } else {
                    Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/signOut", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        updateUserData();
    }
}