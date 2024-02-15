package com.dadino.quickstart.map.sample;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.dadino.quickstart.core.fragments.BaseFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

public abstract class BaseMapFragment extends BaseFragment implements PermissionRequester {

    public static final String MAP_VIEW_SAVE_STATE = "mapViewSaveState";
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;

    protected abstract MapView getMapView();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        multiplePermissionLauncher = LocationHelper.createContract(this, this::onLocationPermissionGranted);

        MapView mapView = getMapView();
        if (mapView != null) mapView.onCreate(savedInstanceState);
    }

    protected abstract boolean onLocationPermissionGranted();

    @Override
    public ActivityResultLauncher<String[]> getLauncher() {
        return multiplePermissionLauncher;
    }

    public boolean hasAllPermissions() {
        return LocationHelper.checkPermission(requireContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        MapView mapView = getMapView();
        if (mapView != null) mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        MapView mapView = getMapView();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        MapView mapView = getMapView();
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        MapView mapView = getMapView();
        if (mapView != null) mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        MapView mapView = getMapView();
        if (mapView != null) mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        try {
            final Bundle mapViewSaveState = new Bundle(outState);
            getMapView().onSaveInstanceState(mapViewSaveState);
            outState.putBundle(MAP_VIEW_SAVE_STATE, mapViewSaveState);
            super.onSaveInstanceState(outState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        MapView mapView = getMapView();
        if (mapView != null) mapView.onLowMemory();
    }

    protected void initializeMap(Bundle savedInstanceState, OnMapReadyCallback callback) {
        MapView mapView = getMapView();
        if (mapView != null) {
            mapView.onCreate(mapViewSavedInstanceState(savedInstanceState));
            mapView.getMapAsync(callback);
        }
    }

    private Bundle mapViewSavedInstanceState(Bundle savedInstanceState) {
        return savedInstanceState != null ? savedInstanceState.getBundle(MAP_VIEW_SAVE_STATE) :
                null;
    }
}
