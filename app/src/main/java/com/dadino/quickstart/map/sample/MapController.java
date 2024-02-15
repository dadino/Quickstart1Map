package com.dadino.quickstart.map.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.dadino.quickstart.core.utils.Logs;
import com.dadino.quickstart.map.BaseMapController;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;

import io.reactivex.disposables.Disposable;

public class MapController extends BaseMapController {

    private static final float DEFAULT_ZOOM = 12.1f;
    private static final String EXTRA_MAP_LAT = "EXTRA_MAP_LAT";
    private static final String EXTRA_MAP_LON = "EXTRA_MAP_LON";
    private static final LatLng DEFAULT_POSITION = new LatLng(43.142380, 13.078593);
    private boolean moveMapToUserLocation = false;
    private Disposable singleLocationUpdateDisposable = null;

    public MapController(Context context, float initialZoom, boolean hasAllPermissions, Bundle savedInstanceState) {
        super(context, initialPosition(savedInstanceState), initialZoom, hasAllPermissions, 50);
        if (initialPosition(savedInstanceState) == null) moveMapToUserLocation = true;
    }

    private static LatLng initialPosition(Bundle state) {
        if (state != null
                && state.containsKey(EXTRA_MAP_LAT)
                && state.containsKey(EXTRA_MAP_LON)) {
            return new LatLng(state.getDouble(EXTRA_MAP_LAT), state.getDouble(EXTRA_MAP_LON));
        } else return null;
    }

    @Override
    public void setMapUi() {
        disableMyLocation = false;
        //map.setLocationSource(new MyLocationSource(appContext));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);
        LatLngBounds bounds = LatLngBounds.builder()
                .include(new LatLng(42, 12))
                .include(new LatLng(44, 14))
                .build();
        map.setLatLngBoundsForCameraTarget(bounds);
        map.setMinZoomPreference(8);
        map.setIndoorEnabled(false);

        map.setMapStyle(getMapStyle());
    }

    @Override
    public float getDefaultZoom() {
        return DEFAULT_ZOOM;
    }

    @Override
    protected LatLng getDefaultPosition() {
        return DEFAULT_POSITION;
    }

    @Override
    public void onLocationPermissionGranted() {
        super.onLocationPermissionGranted();
        startLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        singleLocationUpdateDisposable = LocationHelper.getSingleLocation(appContext, new OnLocationFoundCallback() {
            @Override
            public void onLocationLoading(boolean loading) {
            }

            @Override
            public void onLocationNotFound() {
            }

            @Override
            public void onLocationFound(@Nullable Location location) {
                MapController.this.onLocationFound(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            @Override
            public void onLocationError(Throwable throwable) {
            }
        });
    }

    private MapStyleOptions getMapStyle() {
        return MapStyleOptions.loadRawResourceStyle(appContext, R.raw.map_style);
    }

    public int getMapType() {
        if (map == null) return GoogleMap.MAP_TYPE_NORMAL;
        return map.getMapType();
    }

    public void toggleMapType() {
        if (map == null) return;
        if (map.getMapType() == GoogleMap.MAP_TYPE_HYBRID)
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else if (map.getMapType() == GoogleMap.MAP_TYPE_NORMAL)
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    private void onLocationFound(LatLng latLng) {
        if (map != null && moveMapToUserLocation) {
            Logs.ui("Moving map to user position");
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
            moveMapToUserLocation = false;
        }
    }

    public void onSaveInstanceState(Bundle state) {
        if (map != null) {
            final LatLng position = map.getCameraPosition().target;
            state.putDouble(EXTRA_MAP_LAT, position.latitude);
            state.putDouble(EXTRA_MAP_LON, position.longitude);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (singleLocationUpdateDisposable != null && !singleLocationUpdateDisposable.isDisposed())
            singleLocationUpdateDisposable.dispose();
    }
}
