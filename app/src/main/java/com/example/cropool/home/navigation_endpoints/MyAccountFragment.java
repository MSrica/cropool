package com.example.cropool.home.navigation_endpoints;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cropool.BuildConfig;
import com.example.cropool.R;
import com.example.cropool.api.AccountInfo;
import com.example.cropool.api.CheckpointReq;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Feedback;
import com.example.cropool.api.FindRouteRes;
import com.example.cropool.api.Tokens;
import com.example.cropool.home.HomeActivity;
import com.example.cropool.home.my_account.MyRoutesFragment;
import com.example.cropool.home.my_account.PersonalDetailsFragment;
import com.example.cropool.home.my_account.RoutesSubscribedToFragment;
import com.example.cropool.home.routes.RouteListFragment;
import com.example.cropool.home.routes.RouteListParcelable;
import com.example.cropool.home.routes.RouteType;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyAccountFragment extends Fragment {

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    MaterialCardView routesSubscribedTo, myRoutes, personalDetails, helpSupport, signOut;
    ProgressBar waitingForUpload;
    StorageReference storageReference = storage.getReferenceFromUrl(BuildConfig.FIREBASE_STORAGE_URL);
    private CircleImageView profilePicture, editProfilePicture;
    private TextView name, routesQty, routesQtyLabel, membership, membershipLabel;
    private ActivityResultLauncher<Intent> chooseImageActivityResultLauncher;

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
        waitingForUpload = v.findViewById(R.id.loading_profile_picture);

        // Callback for picking a profile picture
        chooseImageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageURI = result.getData().getData();
                        Bundle bundle = result.getData().getExtras();

                        if (imageURI != null) {
                            // Toast.makeText(getContext(), "imageuri not null", Toast.LENGTH_LONG).show();
                            uploadProfilePicture(null, imageURI, false);
                        } else if (bundle != null) {
                            // Toast.makeText(getContext(), "bundle not null", Toast.LENGTH_LONG).show();
                            Bitmap bitmap = (Bitmap) bundle.get("data");

                            if (bitmap == null)
                                return;

                            uploadProfilePicture(bitmap, null, true);
                        }
                    } else {
                        Toast.makeText(getContext(), "Profile picture not updated.", Toast.LENGTH_LONG).show();
                    }
                });

        // Dialog for choosing between image gallery or camera for choosing a profile picture
        DialogInterface.OnClickListener profilePictureDialogListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Choose image from existing images
                    chooseProfilePicture(true);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // Take a new picture using camera
                    chooseProfilePicture(false);
                    break;
            }
        };

        // Dialog for logout from all devices query
        DialogInterface.OnClickListener logoutDialogListener = (dialog, which) -> {
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

        updateUserData(true, true);

        editProfilePicture.setOnClickListener(v1 -> {
            // Manage updating profile picture via AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(requireContext().getResources().getString(R.string.PROFILE_PICTURE_RESOURCE))
                    .setPositiveButton(requireContext().getResources().getString(R.string.DEVICE_IMAGES), profilePictureDialogListener)
                    .setNegativeButton(requireContext().getResources().getString(R.string.CAMERA), profilePictureDialogListener)
                    .show();
        });

        routesSubscribedTo.setOnClickListener(v1 -> getSubscribedToRoutes(true));

        myRoutes.setOnClickListener(v1 -> getMyRoutes(true));

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
            builder.setMessage(requireContext().getResources().getString(R.string.LOGOUT_FROM_ALL_DIALOG))
                    .setPositiveButton(requireContext().getResources().getString(R.string.YES), logoutDialogListener)
                    .setNegativeButton(requireContext().getResources().getString(R.string.NO), logoutDialogListener)
                    .show();
        });


        return v;
    }

    // Downloads routes current user is subscribed to and displays them as a list
    private void getSubscribedToRoutes(boolean refreshIfNeeded){
        if (HomeActivity.getCurrentFBUser() == null || getContext() == null || getActivity() == null) {
            Toast.makeText(getContext(), "There was an error, please sign in again.", Toast.LENGTH_LONG).show();
            return;
        }

        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<FindRouteRes> call = cropoolAPI.subscribedToRoutes(getContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(getContext()));

        call.enqueue(new Callback<FindRouteRes>() {
            @Override
            public void onResponse(@NotNull Call<FindRouteRes> call, @NotNull Response<FindRouteRes> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/subscribedToRoutes", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 403 || response.code() == 401) {
                        // Access or Firebase tokens invalid

                        // Try to refresh tokens using refresh tokens and re-run addRoute() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if (refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(getActivity(), requireContext(), () -> {
                                getSubscribedToRoutes(false);
                                return null;
                            });
                        } else {
                            Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                FindRouteRes findRouteRes = response.body();

                if (response.code() == 200 && findRouteRes != null) {   // Routes downloaded
                    // Toast.makeText(getContext(), (feedback != null) ? feedback.getFeedback() : "Routes retrieved.", Toast.LENGTH_LONG).show();

                    if (findRouteRes.getResultingRoutes().size() <= 0){
                        Toast.makeText(getContext(), "No routes.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, RouteListFragment.newInstance(new RouteListParcelable(findRouteRes.getResultingRoutes(), RouteType.SUBSCRIBED_TO), "Routes subscribed to", null, null)).addToBackStack(null).commit();
                } else {
                    Toast.makeText(getContext(), "Sorry, there was a problem when downloading your routes.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<FindRouteRes> call, @NotNull Throwable t) {
                Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/subscribedToRoutes", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    // Downloads current user's routes and displays them as a list
    private void getMyRoutes(boolean refreshIfNeeded){
        if (HomeActivity.getCurrentFBUser() == null || getContext() == null || getActivity() == null) {
            Toast.makeText(getContext(), "There was an error, please sign in again.", Toast.LENGTH_LONG).show();
            return;
        }

        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<FindRouteRes> call = cropoolAPI.myRoutes(getContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(getContext()));

        call.enqueue(new Callback<FindRouteRes>() {
            @Override
            public void onResponse(@NotNull Call<FindRouteRes> call, @NotNull Response<FindRouteRes> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/myRoutes", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 403 || response.code() == 401) {
                        // Access or Firebase tokens invalid

                        // Try to refresh tokens using refresh tokens and re-run addRoute() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if (refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(getActivity(), requireContext(), () -> {
                                getMyRoutes(false);
                                return null;
                            });
                        } else {
                            Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                FindRouteRes findRouteRes = response.body();

                if (response.code() == 200 && findRouteRes != null) {   // Routes downloaded
                    // Toast.makeText(getContext(), (feedback != null) ? feedback.getFeedback() : "Routes retrieved.", Toast.LENGTH_LONG).show();

                    if (findRouteRes.getResultingRoutes().size() <= 0){
                        Toast.makeText(getContext(), "No routes.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.home_activity_fragment_container, RouteListFragment.newInstance(new RouteListParcelable(findRouteRes.getResultingRoutes(), RouteType.MY), "My routes", null, null)).addToBackStack(null).commit();
                } else {
                    Toast.makeText(getContext(), "Sorry, there was a problem when downloading your routes.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<FindRouteRes> call, @NotNull Throwable t) {
                Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/myRoutes", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    // Starts the process of choosing the profile picture
    private void chooseProfilePicture(boolean chooseFromExistingImages) {
        Intent intent;

        if (chooseFromExistingImages) {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }

        chooseImageActivityResultLauncher.launch(intent);
    }

    private void startWaitingUI() {
        profilePicture.setVisibility(View.GONE);
        waitingForUpload.setVisibility(View.VISIBLE);
    }

    private void stopWaitingUI() {
        waitingForUpload.setVisibility(View.GONE);
        profilePicture.setVisibility(View.VISIBLE);
    }

    // Uploads the chosen profile picture depending of its format
    private void uploadProfilePicture(Bitmap bitmap, Uri imageURI, boolean isBitmapFormat) {
        startWaitingUI();

        // Get the current user's UID for upload folder purposes
        String currentUserUID = HomeActivity.getCurrentFBUser().getUid();

        // Choose a location to upload to
        StorageReference uploadReference = storageReference.child(currentUserUID).child(String.valueOf(System.currentTimeMillis()));

        // Create an empty upload task
        UploadTask uploadTask;

        if (isBitmapFormat) {
            // Trying to upload a bitmap picture

            if (bitmap == null) {
                Toast.makeText(getContext(), "Image loading error. Profile picture not updated.", Toast.LENGTH_LONG).show();
                stopWaitingUI();
                return;
            }

            // Get the data from an ImageView as bytes
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();

            // Create an upload task
            uploadTask = uploadReference.putBytes(data);
        } else {
            // Trying to upload a picture in URI form (somewhere on the device)

            if (imageURI == null) {
                Toast.makeText(getContext(), "Image loading error. Profile picture not updated.", Toast.LENGTH_LONG).show();
                stopWaitingUI();
                return;
            }

            // Create an upload task

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permissions error. Profile picture not updated.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                stopWaitingUI();
                return;
            } else {
                uploadTask = uploadReference.putFile(imageURI);
            }
        }

        // Create an URL task to get the URL of the uploaded photo
        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(getContext(), "Upload error. Profile picture not updated.", Toast.LENGTH_LONG).show();
                stopWaitingUI();
            }

            // Continue with upload task to get the download URL
            return uploadReference.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Updated profile picture uploaded
                // Log.i("/uploadProfilePicture", task.getResult().toString());

                // Update the profile picture URL in the database
                // (which will automatically upload the FB RTDB profile picture URL)
                updateProfilePicture(task.getResult().toString(), true);
            } else {
                Toast.makeText(getContext(), "Upload error. Profile picture not updated.", Toast.LENGTH_LONG).show();
                stopWaitingUI();
            }
        });
    }

    // Updates uploads the uploaded profile picture
    private void updateProfilePicture(String pictureURL, boolean refreshIfNeeded) {
        AccountInfo updateInfo = new AccountInfo(null, null, pictureURL, null);

        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.updateInfo(requireContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(requireContext()),
                requireContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getFirebaseToken(requireContext()), updateInfo);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/updateProfilePicture", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 403) {
                        // Access or Firebase tokens invalid
                        // Toast.makeText(getContext(), "Refreshing tokens...", Toast.LENGTH_SHORT).show();

                        // Try to refresh tokens using refresh tokens and re-run updateProfilePicture() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if (refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(requireActivity(), requireContext(), () -> {
                                updateProfilePicture(pictureURL, false);
                                return null;
                            });
                        } else {
                            Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    stopWaitingUI();
                    return;
                }

                Feedback feedback = response.body();

                if (response.code() == 201) {   // Profile picture URL updated
                    Toast.makeText(getContext(), (feedback != null) ? feedback.getFeedback() : "Profile picture updated.", Toast.LENGTH_LONG).show();
                    updateUserData(true, true);
                } else {
                    Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();
                }

                stopWaitingUI();
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();
                stopWaitingUI();

                Log.e("/updateProfilePicture", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    // Populates account related TextViews and ImageView with content
    private void updateUserData(boolean refreshIfNeeded, boolean useWaitingUI) {
        if (useWaitingUI)
            startWaitingUI();

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

                        // Try to refresh tokens (depending on refreshIfNeeded, we don't want to refresh forever if that isn't the reason for 403 code)
                        // using refresh tokens and re-run updateUserData() if refreshing is successful
                        if (refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(requireActivity(), requireContext(), () -> {
                                updateUserData(false, useWaitingUI);
                                return null;
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), "Sorry, there was an error. " + response.code(), Toast.LENGTH_LONG).show();
                    }

                    if (useWaitingUI)
                        stopWaitingUI();
                    return;
                }

                AccountInfo accountInfo = response.body();

                if (accountInfo == null) {
                    Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                    if (useWaitingUI)
                        stopWaitingUI();

                    return;
                }

                if (response.code() == 200) {   // User info downloaded
                    String displayName = accountInfo.getFirstName() + " " + accountInfo.getLastName() + "!";
                    name.setText(displayName);

                    if (!accountInfo.getProfilePicture().equals(requireContext().getResources().getString(R.string.FB_RTDB_DEFAULT_PICTURE_VALUE)))
                        Picasso.get().load(accountInfo.getProfilePicture()).into(profilePicture);

                    String routesQtyText = "" + accountInfo.getNumberOfRoutes();
                    routesQty.setText(routesQtyText);

                    // We don't want it to say '1 year ago' but '1 year'
                    String membershipTime = TimeAgo.using(accountInfo.getCreatedAt() * 1000L).replace(" ago", "");
                    membershipTime = membershipTime.substring(0, 1).toUpperCase() + membershipTime.substring(1);
                    membership.setText(membershipTime);
                } else {
                    Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();
                }

                if (useWaitingUI)
                    stopWaitingUI();
            }

            @Override
            public void onFailure(@NotNull Call<AccountInfo> call, @NotNull Throwable t) {
                Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                if (useWaitingUI)
                    stopWaitingUI();

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
    public void onResume() {
        super.onResume();
        updateUserData(true, false);
    }
}