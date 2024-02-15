package com.dadino.quickstart.map.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.MapView;
import com.google.android.material.button.MaterialButton;

import dev.chrisbanes.insetter.Insetter;

public class RealTimeMapFragment extends BaseMapFragment implements PermissionRequester {
    private MaterialButton askForPermission;
    private MapView mapView;
    private MapController mapController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (mapController != null) mapController.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void initPresenters() {

    }

    @Override
    public void onResume() {
        super.onResume();

        checkLocationPermission();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapController != null) mapController.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_real_time_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        askForPermission = view.findViewById(R.id.realtime_ask_for_permission_action);
        mapView = view.findViewById(R.id.realtime_map);

        askForPermission.setOnClickListener(v -> LocationHelper.requestPermission(this));

        Insetter.builder()
                .setOnApplyInsetsListener((view1, insets, initialState) -> {
                    mapController.setMapBorderLeft(insets.getSystemGestureInsets().left);
                    mapController.setMapBorderRight(insets.getSystemGestureInsets().right);
                    mapController.setMapBorderTop(insets.getSystemGestureInsets().top);
                    mapController.setMapBorderBottom(insets.getSystemGestureInsets().bottom);
                })
                .applyToView(mapView);

        initMapController(savedInstanceState);
        initializeMap(savedInstanceState, googleMap -> mapController.onMapReady(googleMap, savedInstanceState));
    }

    @Override
    public void onDestroyView() {
        if (mapController != null) mapController.onDestroy();
        super.onDestroyView();
    }

    @Override
    protected boolean onLocationPermissionGranted() {
        mapController.onLocationPermissionGranted();
        return true;
    }


    private void initMapController(Bundle savedInstanceState) {
        mapController = new MapController(getActivity(), 5, hasAllPermissions(), savedInstanceState);
    }


    @Override
    protected MapView getMapView() {
        return mapView;
    }

    private void checkLocationPermission() {
        if (LocationHelper.checkPermission(getContext())) {
            mapController.onLocationPermissionGranted();
            askForPermission.setVisibility(View.GONE);
        } else {
            askForPermission.setVisibility(View.VISIBLE);
        }
    }
}
