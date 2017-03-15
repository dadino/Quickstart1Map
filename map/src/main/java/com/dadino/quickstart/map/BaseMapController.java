package com.dadino.quickstart.map;


import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;


public abstract class BaseMapController {

	protected final Context   mAppContext;
	private final   int       mMapPadding;
	private final   LatLng    mInitialPosition;
	private final   float     mInitialZoom;
	//Varius
	protected       GoogleMap map;
	private BehaviorSubject<Float> cameraMoveObservable = BehaviorSubject.create();
	private boolean mLocationPermissionGranted;
	private int     mapPaddingLeft;
	private int     mapPaddingRight;
	private int     mapPaddingTop;
	private int     mapPaddingBottom;

	private List<BaseGeoDrawer> drawers = new ArrayList<>();

	public BaseMapController(Context context) {
		this.mAppContext = context.getApplicationContext();
		mMapPadding = context.getResources()
		                     .getDimensionPixelSize(R.dimen._16dp);
		mInitialPosition = null;
		mInitialZoom = 0;
		cameraMoveObservable.debounce(50, TimeUnit.MILLISECONDS)
		                    .observeOn(AndroidSchedulers.mainThread())
		                    .subscribe(new Action1<Float>() {
			                    @Override
			                    public void call(Float zoom) {
				                    onItemsLoadRequested(zoom);
			                    }
		                    });
	}

	public BaseMapController(Context context, LatLng initialPosition, float initialZoom) {
		this.mAppContext = context.getApplicationContext();
		mMapPadding = context.getResources()
		                     .getDimensionPixelSize(R.dimen._16dp);
		mInitialPosition = initialPosition;
		mInitialZoom = initialZoom;
		cameraMoveObservable.debounce(50, TimeUnit.MILLISECONDS)
		                    .observeOn(AndroidSchedulers.mainThread())
		                    .subscribe(new Action1<Float>() {
			                    @Override
			                    public void call(Float zoom) {
				                    onItemsLoadRequested(zoom);
			                    }
		                    });
	}

	public void addGeoDrawer(BaseGeoDrawer drawer) {
		drawers.add(drawer);
	}


	public void onMapReady(GoogleMap googleMap, Bundle savedInstanceState) {
		map = googleMap;
		setMapUi();

		map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				return onMarkerClicked(marker);
			}
		});
		map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				onInfoWindowClicked(marker);
			}
		});
		map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
			@Override
			public void onPolylineClick(Polyline polyline) {
				onPolylineClicked(polyline);
			}
		});
		map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
			@Override
			public void onCameraMove() {
				onBoundsChanged();
			}
		});
		map.setPadding(mapPaddingLeft, mapPaddingTop, mapPaddingRight, mapPaddingBottom);

		updateMyLocationButton();
		setInitialPosition(savedInstanceState);
		onBoundsChanged();

		for (BaseGeoDrawer drawer : drawers) {
			drawer.setMap(map);
		}
	}

	public abstract void setMapUi();

	public abstract float getDefaultZoom();


	private void setInitialPosition(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
					mInitialPosition == null ? getDefaultPosition() : mInitialPosition,
					mInitialPosition == null ? getDefaultZoom() : mInitialZoom);
			map.moveCamera(cameraUpdate);
		}
	}

	protected LatLng getDefaultPosition() {
		return new LatLng(37.6689742, 13.4464143);
	}


	@SuppressWarnings("MissingPermission")
	private void updateMyLocationButton() {
		if (mLocationPermissionGranted && map != null) {
			map.setMyLocationEnabled(true);
			map.getUiSettings()
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

	private void onBoundsChanged() {
		cameraMoveObservable.onNext(map.getCameraPosition().zoom);
	}

	private void onItemsLoadRequested(float zoom) {
		for (BaseGeoDrawer controller : drawers) {
			controller.onGeoLoadRequested(zoom);
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
		if (map != null) map.setPadding(left, top, right, bottom);
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
			map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), mMapPadding));
		} catch (IllegalStateException ex) {
			ex.printStackTrace();
		}
	}

	public LatLngBounds getMapBounds() {
		return map.getProjection()
		          .getVisibleRegion().latLngBounds;
	}
}
