package com.example.cropool.home.routes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cropool.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RouteListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RouteListFragment extends Fragment {

    // The fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ROUTES_LIST = "ROUTES";
    private static final String ARG_FRAGMENT_TITLE = "TITLE";
    private static final String ARG_START_LATLNG = "STARTLATLNG";
    private static final String ARG_FINISH_LATLNG = "FINISHLATLNG";

    // Parameters - routes being visualized and fragment title
    private RouteListParcelable routeListParcelable;
    private String fragmentTitle, startLatLng, finishLatLng;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param downloadedRouteList Routes list wrapped to RouteListParcelable object.
     * @param title               Fragment title.
     * @param startLatLng         Start location for FINDROUTE, null otherwise.
     * @param finishLatLng        Finish location for FINDROUTE, null otherwise.
     * @return A new instance of fragment RouteListFragment.
     */
    public static RouteListFragment newInstance(RouteListParcelable downloadedRouteList, String title, String startLatLng, String finishLatLng) {
        RouteListFragment fragment = new RouteListFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ROUTES_LIST, downloadedRouteList);
        args.putString(ARG_FRAGMENT_TITLE, title);
        args.putString(ARG_START_LATLNG, startLatLng);
        args.putString(ARG_FINISH_LATLNG, finishLatLng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            routeListParcelable = getArguments().getParcelable(ARG_ROUTES_LIST);
            fragmentTitle = getArguments().getString(ARG_FRAGMENT_TITLE);
            startLatLng = getArguments().getString(ARG_START_LATLNG);
            finishLatLng = getArguments().getString(ARG_FINISH_LATLNG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_route_list, container, false);

        TextView title = view.findViewById(R.id.route_list_title);
        title.setText(fragmentTitle);

        if (routeListParcelable == null) {
            return view;
        }

        RecyclerView routeListRecyclerView = view.findViewById(R.id.route_list_recycler_view);
        routeListRecyclerView.setHasFixedSize(false);
        routeListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (routeListParcelable.getRoutesType().equals(RouteType.FOUND)) {
            routeListRecyclerView.setAdapter(new RoutesAdapter(routeListParcelable.getRoutes(), requireContext(), requireActivity(), routeListParcelable.getRoutesType(), startLatLng, finishLatLng));
        } else {
            routeListRecyclerView.setAdapter(new RoutesAdapter(routeListParcelable.getRoutes(), requireContext(), requireActivity(), routeListParcelable.getRoutesType()));
        }

        return view;
    }
}