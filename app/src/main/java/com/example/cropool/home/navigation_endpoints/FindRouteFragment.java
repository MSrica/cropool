package com.example.cropool.home.navigation_endpoints;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.cropool.BuildConfig;
import com.example.cropool.R;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.FindRouteReq;
import com.example.cropool.api.FindRouteRes;
import com.example.cropool.api.Tokens;
import com.example.cropool.custom.InputElement;
import com.example.cropool.home.HomeActivity;
import com.example.cropool.home.routes.RouteListFragment;
import com.example.cropool.home.routes.RouteListParcelable;
import com.example.cropool.home.routes.RouteType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FindRouteFragment extends Fragment {

    private static final int REPETITION_DAILY = 1;
    private static final int REPETITION_MONTHLY = 2;
    private static final String START_LOCATION_VALUE = "START_LOCATION";
    private static final String FINISH_LOCATION_VALUE = "FINISH_LOCATION";
    private String TARGET = "";
    private LatLng startLatLng = null, finishLatLng = null;
    private InputElement startLocation, finishLocation;
    private Slider price, dayOfMonth, tolerance;
    private ChipGroup repetitionModeChipGroup, startDayChipGroup;
    private TextView startDayLabel, startTimeLabel, dayOfMonthLabel, toleranceLabel;
    private TimePicker startTime;
    private ActivityResultLauncher<Intent> placesAutocomplete;
    private Button findRouteButton;

    // Repetition mode that'll be sent to API
    private Integer repetitionMode = null;
    private Boolean customRepetition = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_find_route, container, false);

        startLatLng = finishLatLng = null;
        startLocation = new InputElement(view.findViewById(R.id.find_a_route_start_location_layout), view.findViewById(R.id.find_a_route_start_location));
        finishLocation = new InputElement(view.findViewById(R.id.find_a_route_finish_location_layout), view.findViewById(R.id.find_a_route_finish_location));
        price = view.findViewById(R.id.find_a_route_max_price_per_km);
        repetitionModeChipGroup = view.findViewById(R.id.find_a_route_repetition_mode_chip_group);
        startDayLabel = view.findViewById(R.id.find_a_route_start_day_label);
        startDayChipGroup = view.findViewById(R.id.find_a_route_start_day_chip_group);
        dayOfMonthLabel = view.findViewById(R.id.find_a_route_start_day_of_month_label);
        dayOfMonth = view.findViewById(R.id.find_a_route_start_day_of_month);
        startTimeLabel = view.findViewById(R.id.find_a_route_start_time_label);
        startTime = view.findViewById(R.id.find_a_route_start_time_picker);
        toleranceLabel = view.findViewById(R.id.find_a_route_time_tolerance_label);
        tolerance = view.findViewById(R.id.find_a_route_time_tolerance);
        findRouteButton = view.findViewById(R.id.find_a_route_button);

        startTime.setIs24HourView(true);

        // Callback for autocomplete
        placesAutocomplete = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Success, initialize place
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());

                        String placeAddress = place.getAddress();

                        if (TARGET.equals(START_LOCATION_VALUE)) {
                            startLocation.getTextInput().setText(placeAddress);
                            startLatLng = place.getLatLng();
                            startLocation.getInputLayout().setEnabled(true);
                        } else {
                            finishLocation.getTextInput().setText(placeAddress);
                            finishLatLng = place.getLatLng();
                            finishLocation.getInputLayout().setEnabled(true);
                        }
                    } else if (result.getResultCode() != Activity.RESULT_CANCELED) {
                        Toast.makeText(getContext(), "Place decoding error.", Toast.LENGTH_LONG).show();
                    }

                    // Re-enable location inputs
                    startLocation.getInputLayout().setEnabled(true);
                    finishLocation.getInputLayout().setEnabled(true);
                });

        // Initialize Places (for getting latLng objects)
        Places.initialize(requireContext(), BuildConfig.PLACES_API_KEY);

        // Set autocomplete triggers
        for (InputElement ie : new InputElement[]{startLocation, finishLocation}) {
            ie.getTextInput().setFocusable(false);

            ie.getTextInput().setOnClickListener(v -> {
                // If we click until PlacesAPI is not yet shown, we trigger multiple intents
                ie.getInputLayout().setEnabled(false);

                // Initialize place field list
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);

                // Create autocomplete intent
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(requireContext());
                TARGET = (ie == startLocation) ? START_LOCATION_VALUE : FINISH_LOCATION_VALUE;

                // Start activity result
                placesAutocomplete.launch(intent);
            });
        }

        // Hiding/showing necessary views relative to repetition mode
        repetitionModeChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Resetting parameters
            repetitionMode = null;
            customRepetition = null;

            List<View> viewsToHide = new ArrayList<>();
            List<View> viewsToShow = new ArrayList<>();

            if (checkedIds.contains(R.id.find_a_route_daily_repetition)) {
                repetitionMode = REPETITION_DAILY;
                viewsToHide.addAll(Arrays.asList(dayOfMonthLabel, dayOfMonth, startDayLabel, startDayChipGroup));
                viewsToShow.addAll(Arrays.asList(startTimeLabel, startTime, toleranceLabel, tolerance));
            } else if (checkedIds.contains(R.id.find_a_route_weekly_repetition)) {
                // Removing checked day(s) and enabling single selections
                startDayChipGroup.clearCheck();
                startDayChipGroup.setSingleSelection(true);

                viewsToHide.addAll(Arrays.asList(dayOfMonthLabel, dayOfMonth));
                viewsToShow.addAll(Arrays.asList(startDayLabel, startDayChipGroup, startTimeLabel, startTime, toleranceLabel, tolerance));
            } else if (checkedIds.contains(R.id.find_a_route_some_days_repetition)) {
                // Removing checked day(s) and removing single selection
                startDayChipGroup.clearCheck();
                startDayChipGroup.setSingleSelection(false);

                viewsToHide.addAll(Arrays.asList(dayOfMonthLabel, dayOfMonth));
                viewsToShow.addAll(Arrays.asList(startDayLabel, startDayChipGroup, startTimeLabel, startTime, toleranceLabel, tolerance));
            } else if (checkedIds.contains(R.id.find_a_route_monthly_repetition)) {
                repetitionMode = REPETITION_MONTHLY;

                viewsToHide.addAll(Arrays.asList(startDayLabel, startDayChipGroup));
                viewsToShow.addAll(Arrays.asList(dayOfMonthLabel, dayOfMonth, startTimeLabel, startTime, toleranceLabel, tolerance));
            } else {
                // Custom repetition - R.id.find_a_route_custom_repetition
                customRepetition = true;

                viewsToHide.addAll(Arrays.asList(dayOfMonthLabel, dayOfMonth, startDayLabel, startDayChipGroup, startTimeLabel, startTime, toleranceLabel, tolerance));
                viewsToShow.addAll(Collections.emptyList());
            }

            // Hiding all unnecessary views
            for (View v : viewsToHide) v.setVisibility(View.GONE);

            // Showing all necessary views
            for (View v : viewsToShow) v.setVisibility(View.VISIBLE);
        });

        // Updating startDayOfWeek/someDaysCode when something changes
        startDayChipGroup.setOnCheckedStateChangeListener(((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                repetitionMode = null;
                return;
            }

            // Updating repetition mode - days are coded as ints (same for weekly or some days)
            int code = 0;

            if (checkedIds.contains(R.id.find_a_route_monday)) code += 1000000;
            if (checkedIds.contains(R.id.find_a_route_tuesday)) code += 200000;
            if (checkedIds.contains(R.id.find_a_route_wednesday)) code += 30000;
            if (checkedIds.contains(R.id.find_a_route_thursday)) code += 4000;
            if (checkedIds.contains(R.id.find_a_route_friday)) code += 500;
            if (checkedIds.contains(R.id.find_a_route_saturday)) code += 60;
            if (checkedIds.contains(R.id.find_a_route_sunday)) code += 7;

            repetitionMode = code;
        }));

        findRouteButton.setOnClickListener(v -> findRoute(true));

        return view;
    }

    private void findRoute(boolean refreshIfNeeded) {
        if (!validateData()) {
            return;
        }

        if (HomeActivity.getCurrentFBUser() == null) {
            Toast.makeText(getContext(), "There was an error, please sign in again.", Toast.LENGTH_LONG).show();
            return;
        }


        FindRouteReq findRouteReq = new FindRouteReq(startLatLng.latitude + "," + startLatLng.longitude, finishLatLng.latitude + "," + finishLatLng.longitude, (double) price.getValue(), customRepetition, repetitionMode, null, (int) dayOfMonth.getValue(), null, (int) startTime.getCurrentHour(), (int) startTime.getCurrentMinute(), (int) tolerance.getValue());
        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<FindRouteRes> call = cropoolAPI.findRoute(requireContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(requireContext()), findRouteReq);

        call.enqueue(new Callback<FindRouteRes>() {
            @Override
            public void onResponse(@NotNull Call<FindRouteRes> call, @NotNull Response<FindRouteRes> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/findRoute", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 403) {
                        // Access or Firebase tokens invalid
                        // Toast.makeText(getContext(), "Refreshing tokens...", Toast.LENGTH_SHORT).show();

                        // Try to refresh tokens using refresh tokens and re-run addRoute() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if (refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(requireActivity(), requireContext(), () -> {
                                findRoute(false);
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

                if (response.code() == 200 && findRouteRes != null && findRouteRes.getResultingRoutes().size() > 0) {   // Filtered routeItems received
                    RouteListParcelable routeListParcelable = new RouteListParcelable(findRouteRes.getResultingRoutes(), RouteType.FOUND);
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_fragment_container, RouteListFragment.newInstance(routeListParcelable, "Found routes", startLatLng.latitude + "," + startLatLng.longitude, finishLatLng.latitude + "," + finishLatLng.longitude)).commit();
                } else {
                    Toast.makeText(getContext(), "Sorry, no appropriate routeItems were found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<FindRouteRes> call, @NotNull Throwable t) {
                Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/findRoute", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    private boolean validateData() {
        boolean isValid = true;

        // Removing past errors
        for (InputElement ie : new InputElement[]{startLocation, finishLocation})
            ie.getInputLayout().setErrorEnabled(false);

        if (startLatLng == null) {
            // Starting location is not set
            isValid = false;

            if (startLocation != null && startLocation.getInputLayout() != null)
                startLocation.getInputLayout().setError("Enter starting location.");
        }

        if (finishLatLng == null) {
            // Finishing location is not set
            isValid = false;

            if (finishLocation != null && finishLocation.getInputLayout() != null)
                finishLocation.getInputLayout().setError("Enter finishing location.");
        }

        if (price == null || price.getValue() < 0) {
            // Price is not set
            isValid = false;
            Toast.makeText(getContext(), "Please set a valid maximum price.", Toast.LENGTH_LONG).show();
        }

        if (customRepetition != null && customRepetition) {
            // Repetition is custom - no additional parameters need to be set
            return isValid;
        }

        // Repetition isn't custom - otherwise the method would already have returned isValid
        if (repetitionMode == null || repetitionMode <= 0) {
            // Repetition mode is not set
            isValid = false;
            Toast.makeText(getContext(), "Please choose a valid repetition mode.", Toast.LENGTH_LONG).show();
        } else if (repetitionMode == REPETITION_MONTHLY) {
            // Day of month has to be set

            if (dayOfMonth == null || dayOfMonth.getValue() <= 0 || dayOfMonth.getValue() >= 32) {
                // Day of month is not set or not correctly set
                isValid = false;
                Toast.makeText(getContext(), "Please set a valid starting day of month.", Toast.LENGTH_LONG).show();
            }
        }

        // Repetition is daily, monthly, weekly or some days
        // For all of the above possibilities, repetitionMode is already set (we checked for null)
        // and the only requirements left are starting time and tolerance
        if (startTime == null) {
            // Starting time is not set
            isValid = false;
            Toast.makeText(getContext(), "Please set a valid starting time.", Toast.LENGTH_LONG).show();
        }

        if (tolerance == null) {
            // Tolerance time is not set
            isValid = false;
            Toast.makeText(getContext(), "Please set a valid starting time tolerance.", Toast.LENGTH_LONG).show();
        }

        return isValid;
    }
}