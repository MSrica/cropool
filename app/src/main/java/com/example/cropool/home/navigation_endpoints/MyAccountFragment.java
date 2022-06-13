package com.example.cropool.home.navigation_endpoints;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.cropool.R;
import com.example.cropool.api.AccountInfo;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Tokens;
import com.example.cropool.my_account.MyRoutesFragment;
import com.example.cropool.my_account.PersonalDetailsFragment;
import com.example.cropool.my_account.RoutesSubscribedToFragment;
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

        updateUserData();

        routesSubscribedTo.setOnClickListener(v1 -> {
            requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new RoutesSubscribedToFragment()).addToBackStack(null).commit();
        });

        myRoutes.setOnClickListener(v1 -> {
            requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new MyRoutesFragment()).addToBackStack(null).commit();
        });

        personalDetails.setOnClickListener(v1 -> {
            requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, new PersonalDetailsFragment()).addToBackStack(null).commit();
        });

        helpSupport.setOnClickListener(v1 -> {

        });

        signOut.setOnClickListener(v1 -> {

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

    @Override
    public void onResume(){
        super.onResume();
        updateUserData();
    }
}