package com.dadino.quickstart.map;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.VisibleRegion;
import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


public abstract class BaseMapController {

    protected final Context appContext;
    private final int mapPadding;
    private final LatLng initialPosition;
    private final float initialZoom;
    private final Disposable cameraMoveDisposable;
    public boolean disableMyLocation = false;
    private final BehaviorRelay<Float> cameraMoveRelay;
    protected GoogleMap map;
    private int mapPaddingLeft;
    private int mapPaddingRight;
    private int mapPaddingTop;
    private int mapPaddingBottom;
    private boolean locationPermissionGranted;

    private final List<BaseGeoDrawer> drawers = new ArrayList<>();
    private LatLngBounds mapBounds;

    public BaseMapController(Context context, boolean hasAllPermissions) {
        this.appContext = context.getApplicationContext();
        mapPadding = context.getResources()
                .getDimensionPixelSize(R.dimen._16dp);
        locationPermissionGranted = hasAllPermissions;
        initialPosition = null;
        initialZoom = 0;
        cameraMoveRelay = BehaviorRelay.createDefault(initialZoom);
        cameraMoveDisposable = cameraMoveRelay.toFlowable(BackpressureStrategy.LATEST)
                .debounce(50, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(zoom -> onCameraMoved());
    }

    public BaseMapController(Context context, LatLng initialPosition, float initialZoom, boolean hasAllPermissions, long cameraDebounceMillis) {
        this.appContext = context.getApplicationContext();
        mapPadding = context.getResources()
                .getDimensionPixelSize(R.dimen._16dp);
        locationPermissionGranted = hasAllPermissions;
        this.initialPosition = initialPosition;
        this.initialZoom = initialZoom;
        cameraMoveRelay = BehaviorRelay.createDefault(this.initialZoom);
        cameraMoveDisposable = cameraMoveRelay.toFlowable(BackpressureStrategy.LATEST)
                .debounce(cameraDebounceMillis, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(zoom -> onCameraMoved());
    }

    public void addGeoDrawer(BaseGeoDrawer drawer) {
        drawers.add(drawer);
    }

    public void onMapReady(GoogleMap googleMap, Bundle savedInstanceState) {
        map = googleMap;
        setMapUi();

        map.setOnMarkerClickListener(this::onMarkerClicked);
        map.setOnInfoWindowClickListener(this::onInfoWindowClicked);
        map.setOnPolylineClickListener(this::onPolylineClicked);
        map.setOnCameraMoveListener(() -> cameraMoveRelay.accept(map.getCameraPosition().zoom));
        map.setPadding(mapPaddingLeft, mapPaddingTop, mapPaddingRight, mapPaddingBottom);

        updateMyLocationButton();
        setInitialPosition(savedInstanceState);
        onCameraMoved();

        for (BaseGeoDrawer drawer : drawers) {
            drawer.setMap(map);
        }
    }

    public abstract void setMapUi();

    public abstract float getDefaultZoom();

    private void setInitialPosition(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    initialPosition == null ? getDefaultPosition() : initialPosition,
                    initialPosition == null ? getDefaultZoom() : initialZoom);
            map.moveCamera(cameraUpdate);
        }
    }

    protected LatLng getDefaultPosition() {
        return new LatLng(43.142351, 13.078629);
    }

    @SuppressWarnings("MissingPermission")
    private void updateMyLocationButton() {
        if (!disableMyLocation && locationPermissionGranted && map != null) {
            map.setOnMyLocationButtonClickListener(() -> false);
            map.setOnMyLocationClickListener(location -> {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                map.animateCamera(cameraUpdate);
            });
            map.setMyLocationEnabled(true);
        }
    }

    private boolean onMarkerClicked(Marker marker) {
        for (BaseGeoDrawer controller : drawers) {
            if (controller.onGeoClicked(marker)) return true;
        }
        return false;
    }

    private boolean onInfoWindowClicked(Marker marker) {
        for (BaseGeoDrawer controller : drawers) {
            if (controller.onInfoWindowClicked(marker)) return true;
        }
        return false;
    }

    private boolean onPolylineClicked(Polyline polyline) {
        for (BaseGeoDrawer controller : drawers) {
            if (controller.onGeoClicked(polyline)) return true;
        }
        return false;
    }

    private void onCameraMoved() {
        mapBounds = null;
        float zoom = initialZoom;
        if (map != null) {
            CameraPosition cameraPosition = map.getCameraPosition();
            if (cameraPosition != null) zoom = cameraPosition.zoom;

            final Projection projection = map.getProjection();
            if (projection != null) {
                final VisibleRegion region = projection.getVisibleRegion();
                if (region != null) {
                    final LatLngBounds latLngBounds = region.latLngBounds;
                    if (latLngBounds != null) {
                        mapBounds = latLngBounds;
                    }
                }
            }
        }
        if (mapBounds == null ||
                (mapBounds.southwest.latitude == 0
                        && mapBounds.southwest.longitude == 0
                        && mapBounds.northeast.latitude == 0
                        && mapBounds.northeast.longitude == 0)) {
            new Handler().postDelayed(this::onCameraMoved, 32);
        } else {
            onMapProjectionChanged(mapBounds, zoom);
        }
    }

    private void onMapProjectionChanged(LatLngBounds bounds, float zoom) {
        for (BaseGeoDrawer controller : drawers) {
            controller.onMapProjectionBoundsChanged(bounds, zoom);
        }
    }

    @SuppressWarnings("MissingPermission")
    public void onLocationPermissionGranted() {
        locationPermissionGranted = true;
        updateMyLocationButton();
    }

    public void setMapBorders(int left, int top, int right, int bottom) {
        this.mapPaddingLeft = left;
        this.mapPaddingRight = right;
        this.mapPaddingTop = top;
        this.mapPaddingBottom = bottom;
        if (map != null) map.setPadding(left, top, right, bottom);
    }

    public void setMapBorderLeft(int left) {
        this.mapPaddingLeft = left;
        if (map != null) map.setPadding(left, mapPaddingTop, mapPaddingRight, mapPaddingBottom);
    }

    public void setMapBorderRight(int right) {
        this.mapPaddingRight = right;
        if (map != null) map.setPadding(mapPaddingLeft, mapPaddingTop, right, mapPaddingBottom);
    }

    public void setMapBorderTop(int top) {
        this.mapPaddingTop = top;
        if (map != null) map.setPadding(mapPaddingLeft, top, mapPaddingRight, mapPaddingBottom);
    }

    public void setMapBorderBottom(int bottom) {
        this.mapPaddingBottom = bottom;
        if (map != null) map.setPadding(mapPaddingLeft, mapPaddingTop, mapPaddingRight, bottom);
    }

    public void moveToDrawerBounds() {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (BaseGeoDrawer controller : drawers) {
            final LatLngBounds bounds = controller.getBounds();
            if (bounds != null) {
                builder.include(bounds.northeast);
                builder.include(bounds.southwest);
            }
        }
        try {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), mapPadding));
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    public LatLngBounds getMapBounds() {
        return mapBounds;
    }

    public void onDestroy() {
        try {
            //noinspection MissingPermission
            map.setMyLocationEnabled(false);
        } catch (Exception ignored) {
        }
        map = null;
        for (BaseGeoDrawer drawer : drawers) {
            drawer.onDestroy();
        }
        drawers.clear();
        if (cameraMoveDisposable != null && !cameraMoveDisposable.isDisposed())
            cameraMoveDisposable.dispose();
    }
}
