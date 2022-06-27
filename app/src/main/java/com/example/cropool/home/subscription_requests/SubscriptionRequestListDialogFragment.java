package com.example.cropool.home.subscription_requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cropool.R;

public class SubscriptionRequestListDialogFragment extends DialogFragment {

    private static final String ARG_REQUESTS = "REQUESTS";

    private SubscriptionRequestListParcelable subscriptionRequestListParcelable;

    public static SubscriptionRequestListDialogFragment newInstance(SubscriptionRequestListParcelable subscriptionRequestListParcelable) {
        SubscriptionRequestListDialogFragment fragment = new SubscriptionRequestListDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_REQUESTS, subscriptionRequestListParcelable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subscriptionRequestListParcelable = getArguments().getParcelable(ARG_REQUESTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_subscription_request_list_dialog, container, false);

        LinearLayout placeholder = view.findViewById(R.id.subscription_request_fragment_root);
        placeholder.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        RecyclerView subscriptionListRecyclerView = view.findViewById(R.id.subscription_request_recycler_view);
        subscriptionListRecyclerView.setHasFixedSize(false);
        subscriptionListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        subscriptionListRecyclerView.setAdapter(new SubscriptionRequestsAdapter(subscriptionRequestListParcelable.getSubscriptionRequests(), requireContext(), getActivity()));

        return view;
    }
}