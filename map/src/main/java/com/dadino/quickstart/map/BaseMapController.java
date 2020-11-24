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
import io.reactivex.functions.Consumer;


public abstract class BaseMapController {

    protected final Context mAppContext;
    private final int mMapPadding;
    private final LatLng mInitialPosition;
    private final float mInitialZoom;
    private final Disposable cameraMoveDisposable;
    protected GoogleMap mMap;
    private final BehaviorRelay<Float> cameraMoveRelay;
    private boolean mLocationPermissionGranted;
    private int mapPaddingLeft;
    private int mapPaddingRight;
    private int mapPaddingTop;
    private int mapPaddingBottom;

    private final List<BaseGeoDrawer> drawers = new ArrayList<>();
    private LatLngBounds mapBounds;

    public BaseMapController(Context context) {
        this.mAppContext = context.getApplicationContext();
        mMapPadding = context.getResources()
                .getDimensionPixelSize(R.dimen._16dp);
        mInitialPosition = null;
        mInitialZoom = 0;
        cameraMoveRelay = BehaviorRelay.createDefault(mInitialZoom);
        cameraMoveDisposable = cameraMoveRelay.toFlowable(BackpressureStrategy.LATEST)
                .debounce(50, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Float>() {
                    @Override
                    public void accept(Float zoom) {
                        onCameraMoved();
                    }
                });
    }

    public BaseMapController(Context context, LatLng initialPosition, float initialZoom, long cameraDebounceMillis) {
        this.mAppContext = context.getApplicationContext();
        mMapPadding = context.getResources()
                .getDimensionPixelSize(R.dimen._16dp);
        mInitialPosition = initialPosition;
        mInitialZoom = initialZoom;
        cameraMoveRelay = BehaviorRelay.createDefault(mInitialZoom);
        cameraMoveDisposable = cameraMoveRelay.toFlowable(BackpressureStrategy.LATEST)
                .debounce(cameraDebounceMillis, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Float>() {
                    @Override
                    public void accept(Float zoom) {
                        onCameraMoved();
                    }
                });
    }

    public void addGeoDrawer(BaseGeoDrawer drawer) {
        drawers.add(drawer);
    }


    public void onMapReady(GoogleMap googleMap, Bundle savedInstanceState) {
        mMap = googleMap;
        setMapUi();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return onMarkerClicked(marker);
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                onInfoWindowClicked(marker);
            }
        });
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                onPolylineClicked(polyline);
            }
        });
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                cameraMoveRelay.accept(mMap.getCameraPosition().zoom);
            }
        });
        mMap.setPadding(mapPaddingLeft, mapPaddingTop, mapPaddingRight, mapPaddingBottom);

        updateMyLocationButton();
        setInitialPosition(savedInstanceState);
        onCameraMoved();

        for (BaseGeoDrawer drawer : drawers) {
            drawer.setMap(mMap);
        }
    }

    public abstract void setMapUi();

    public abstract float getDefaultZoom();

    private void setInitialPosition(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    mInitialPosition == null ? getDefaultPosition() : mInitialPosition,
                    mInitialPosition == null ? getDefaultZoom() : mInitialZoom);
            mMap.moveCamera(cameraUpdate);
        }
    }

    protected LatLng getDefaultPosition() {
        return new LatLng(43.142351, 13.078629);
    }

    @SuppressWarnings("MissingPermission")
    private void updateMyLocationButton() {
        if (mLocationPermissionGranted && mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings()
                    .setMyLocationButtonEnabled(true);
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
        float zoom = mInitialZoom;
        if (mMap != null) {
            CameraPosition cameraPosition = mMap.getCameraPosition();
            if (cameraPosition != null) zoom = cameraPosition.zoom;

            final Projection projection = mMap.getProjection();
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onCameraMoved();
                }
            }, 32);
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
        mLocationPermissionGranted = true;
        updateMyLocationButton();
    }

    public void setMapBorders(int left, int top, int right, int bottom) {
        this.mapPaddingLeft = left;
        this.mapPaddingRight = right;
        this.mapPaddingTop = top;
        this.mapPaddingBottom = bottom;
        if (mMap != null) mMap.setPadding(left, top, right, bottom);
    }

    public void setMapBorderLeft(int left) {
        this.mapPaddingLeft = left;
        if (mMap != null) mMap.setPadding(left, mapPaddingTop, mapPaddingRight, mapPaddingBottom);
    }

    public void setMapBorderRight(int right) {
        this.mapPaddingRight = right;
        if (mMap != null) mMap.setPadding(mapPaddingLeft, mapPaddingTop, right, mapPaddingBottom);
    }

    public void setMapBorderTop(int top) {
        this.mapPaddingTop = top;
        if (mMap != null) mMap.setPadding(mapPaddingLeft, top, mapPaddingRight, mapPaddingBottom);
    }

    public void setMapBorderBottom(int bottom) {
        this.mapPaddingBottom = bottom;
        if (mMap != null) mMap.setPadding(mapPaddingLeft, mapPaddingTop, mapPaddingRight, bottom);
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), mMapPadding));
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
            mMap.setMyLocationEnabled(false);
        } catch (Exception ignored) {
        }
        mMap = null;
        for (BaseGeoDrawer drawer : drawers) {
            drawer.onDestroy();
        }
        drawers.clear();
        if (cameraMoveDisposable != null && !cameraMoveDisposable.isDisposed())
            cameraMoveDisposable.dispose();
    }
}
