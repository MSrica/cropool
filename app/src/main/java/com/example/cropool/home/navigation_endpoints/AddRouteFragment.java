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
import com.example.cropool.api.AddRouteReq;
import com.example.cropool.api.CropoolAPI;
import com.example.cropool.api.Feedback;
import com.example.cropool.api.Tokens;
import com.example.cropool.custom.InputElement;
import com.example.cropool.home.HomeActivity;
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
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddRouteFragment extends Fragment {

    private static final int REPETITION_DAILY = 1;
    private static final int REPETITION_MONTHLY = 2;
    private static final String START_LOCATION_VALUE = "START_LOCATION";
    private static final String FINISH_LOCATION_VALUE = "FINISH_LOCATION";
    private String TARGET = "";
    private LatLng startLatLng = null, finishLatLng = null;
    private InputElement startLocation, finishLocation, name, note;
    private Slider passengerNum, price, dayOfMonth;
    private ChipGroup repetitionModeChipGroup, startDayChipGroup;
    private TextView startDayLabel, noteLabel, startTimeLabel, dayOfMonthLabel;
    private TimePicker startTime;
    private ActivityResultLauncher<Intent> placesAutocomplete;
    private Button addRouteButton;

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
        View view = inflater.inflate(R.layout.fragment_add_route, container, false);

        startLatLng = finishLatLng = null;
        startLocation = new InputElement(view.findViewById(R.id.start_location_layout), view.findViewById(R.id.start_location));
        finishLocation = new InputElement(view.findViewById(R.id.finish_location_layout), view.findViewById(R.id.finish_location));
        name = new InputElement(view.findViewById(R.id.add_route_name_layout), view.findViewById(R.id.add_route_name));
        passengerNum = view.findViewById(R.id.add_route_max_passenger_number);
        price = view.findViewById(R.id.add_route_price_per_km);
        repetitionModeChipGroup = view.findViewById(R.id.repetition_mode_chip_group);
        startDayLabel = view.findViewById(R.id.add_route_start_day_label);
        startDayChipGroup = view.findViewById(R.id.start_day_chip_group);
        dayOfMonthLabel = view.findViewById(R.id.add_route_start_day_of_month_label);
        dayOfMonth = view.findViewById(R.id.add_route_start_day_of_month);
        noteLabel = view.findViewById(R.id.add_route_note_label);
        note = new InputElement(view.findViewById(R.id.add_route_note_layout), view.findViewById(R.id.add_route_note));
        startTimeLabel = view.findViewById(R.id.start_time_label);
        startTime = view.findViewById(R.id.start_time_picker);
        addRouteButton = view.findViewById(R.id.add_route_button);

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

            if (checkedIds.contains(R.id.daily_repetition)) {
                repetitionMode = REPETITION_DAILY;
                viewsToHide.addAll(Arrays.asList(dayOfMonthLabel, dayOfMonth, startDayLabel, startDayChipGroup, noteLabel, note.getInputLayout()));
                viewsToShow.addAll(Arrays.asList(startTimeLabel, startTime));
            } else if (checkedIds.contains(R.id.weekly_repetition)) {
                // Removing checked day(s) and enabling single selections
                startDayChipGroup.clearCheck();
                startDayChipGroup.setSingleSelection(true);

                viewsToHide.addAll(Arrays.asList(dayOfMonthLabel, dayOfMonth, noteLabel, note.getInputLayout()));
                viewsToShow.addAll(Arrays.asList(startDayLabel, startDayChipGroup, startTimeLabel, startTime));
            } else if (checkedIds.contains(R.id.some_days_repetition)) {
                // Removing checked day(s) and removing single selection
                startDayChipGroup.clearCheck();
                startDayChipGroup.setSingleSelection(false);

                viewsToHide.addAll(Arrays.asList(dayOfMonthLabel, dayOfMonth, noteLabel, note.getInputLayout()));
                viewsToShow.addAll(Arrays.asList(startDayLabel, startDayChipGroup, startTimeLabel, startTime));
            } else if (checkedIds.contains(R.id.monthly_repetition)) {
                repetitionMode = REPETITION_MONTHLY;

                viewsToHide.addAll(Arrays.asList(startDayLabel, startDayChipGroup, noteLabel, note.getInputLayout()));
                viewsToShow.addAll(Arrays.asList(dayOfMonthLabel, dayOfMonth, startTimeLabel, startTime));
            } else {
                // Custom repetition - R.id.custom_repetition
                customRepetition = true;

                viewsToHide.addAll(Arrays.asList(dayOfMonthLabel, dayOfMonth, startDayLabel, startDayChipGroup, startTimeLabel, startTime));
                viewsToShow.addAll(Arrays.asList(noteLabel, note.getInputLayout()));
            }

            // Hiding all unnecessary views
            for (View v : viewsToHide) v.setVisibility(View.GONE);

            // Showing all necessary views
            for (View v : viewsToShow) v.setVisibility(View.VISIBLE);
        });

        // Updating startDayOfWeek/someDaysCode when something changes
        startDayChipGroup.setOnCheckedStateChangeListener(((group, checkedIds) -> {
            if (checkedIds.isEmpty()){
                repetitionMode = null;
                return;
            }

            // Updating repetition mode - days are coded as ints (same for weekly or some days)
            int code = 0;

            if (checkedIds.contains(R.id.monday)) code += 1000000;
            if (checkedIds.contains(R.id.tuesday)) code += 200000;
            if (checkedIds.contains(R.id.wednesday)) code += 30000;
            if (checkedIds.contains(R.id.thursday)) code += 4000;
            if (checkedIds.contains(R.id.friday)) code += 500;
            if (checkedIds.contains(R.id.saturday)) code += 60;
            if (checkedIds.contains(R.id.sunday)) code += 7;

            repetitionMode = code;
        }));

        addRouteButton.setOnClickListener(v -> addRoute(true));

        return view;
    }

    private void addRoute(boolean refreshIfNeeded) {
        if (!validateData() || HomeActivity.getCurrentFBUser() == null) {
            Toast.makeText(getContext(), "Route couldn't be added. Please check your input.", Toast.LENGTH_LONG).show();
            return;
        }

        AddRouteReq addRouteReq = new AddRouteReq(HomeActivity.getCurrentFBUser().getUid(), Objects.requireNonNull(name.getTextInput().getText()).toString(), (int) passengerNum.getValue(), startLatLng.latitude + "," + startLatLng.longitude, finishLatLng.latitude + "," + finishLatLng.longitude, repetitionMode, (double) price.getValue(), customRepetition != null && customRepetition, null, (int) dayOfMonth.getValue(), null, startTime.getCurrentHour(), startTime.getCurrentMinute(), Objects.requireNonNull(note.getTextInput().getText()).toString());

        Retrofit retrofit = CropoolAPI.getRetrofit();
        CropoolAPI cropoolAPI = retrofit.create(CropoolAPI.class);

        Call<Feedback> call = cropoolAPI.addRoute(requireContext().getResources().getString(R.string.TOKEN_HEADER_PREFIX) + Tokens.getAccessToken(requireContext()), addRouteReq);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(@NotNull Call<Feedback> call, @NotNull Response<Feedback> response) {
                if (!response.isSuccessful()) {
                    // Not OK
                    Log.e("/addRoute", "notSuccessful: Something went wrong. - " + response.code());

                    if (response.code() == 403) {
                        // Access or Firebase tokens invalid
                        // Toast.makeText(getContext(), "Refreshing tokens...", Toast.LENGTH_SHORT).show();

                        // Try to refresh tokens using refresh tokens and re-run addRoute() if refreshing is successful
                        // Set refreshIfNeeded to false - we don't want to refresh tokens infinitely if that's not the problem
                        if(refreshIfNeeded) {
                            Tokens.refreshTokensOnServer(requireActivity(), requireContext(), () -> {
                                addRoute(false);
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

                Feedback feedback = response.body();

                if (response.code() == 201) {   // Route inserted
                    Toast.makeText(getContext(), (feedback != null) ? feedback.getFeedback() : "Route created.", Toast.LENGTH_LONG).show();

                    // Go back to find route fragment (can't replace fragment because of navigation bar)
                    requireActivity().startActivity(new Intent(getContext(), HomeActivity.class));
                    requireActivity().finish();
                } else {
                    Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Feedback> call, @NotNull Throwable t) {
                Toast.makeText(getContext(), "Sorry, there was an error.", Toast.LENGTH_LONG).show();

                Log.e("/addRoute", "onFailure: Something went wrong. " + t.getMessage());
            }
        });
    }

    private boolean validateData() {
        boolean isValid = true;

        // Removing past errors
        for (InputElement ie : new InputElement[]{startLocation, finishLocation, name, note})
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

        if (name == null || name.getTextInput() == null || name.getTextInput().getText() == null || name.getTextInput().getText().toString().isEmpty()) {
            // Name is not set
            isValid = false;
            if (name != null && name.getInputLayout() != null)
                name.getInputLayout().setError("Enter route name.");
        }

        if (passengerNum == null || passengerNum.getValue() <= 0) {
            // Number of passengers is not set
            isValid = false;
            Toast.makeText(getContext(), "Please set a valid number of passengers.", Toast.LENGTH_LONG).show();
        }

        if (price == null || price.getValue() < 0) {
            // Price is not set
            isValid = false;
            Toast.makeText(getContext(), "Please set a valid price.", Toast.LENGTH_LONG).show();
        }

        if (customRepetition != null && customRepetition) {
            // Repetition is custom therefore only note has to be set

            if (note == null || note.getTextInput() == null || note.getTextInput().getText() == null) {
                // Note is not set
                isValid = false;
                if (note != null && note.getInputLayout() != null)
                    note.getInputLayout().setError("Enter custom repetition note.");
            }

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
        // and the only requirement left is starting time
        if (startTime == null) {
            // Starting time is not set
            isValid = false;
            Toast.makeText(getContext(), "Please set a valid starting time.", Toast.LENGTH_LONG).show();
        }

        return isValid;
    }
}