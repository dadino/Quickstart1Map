package com.dadino.quickstart.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;

import java.util.Map;


public abstract class BasePolylineDrawer<KEY, ITEM> extends BaseGeoDrawer<KEY, Polyline, ITEM> {


	public BasePolylineDrawer(BasePolylineFormatter<ITEM> formatter) {
		super(formatter);
	}

	protected void onMapBoundsUpdated() {
		for (Map.Entry<KEY, GeoItem<Polyline, ITEM>> entry : getItemMap().entrySet()) {
			final Polyline polyline = entry.getValue()
			                               .getGeometry();
			polyline.setVisible(isVisible(polyline));
		}
	}

	@Override
	protected LatLng getPosition(GeoItem<Polyline, ITEM> value) {
		final LatLngBounds.Builder builder = LatLngBounds.builder();
		for (LatLng latLng : value.getGeometry()
		                          .getPoints()) {
			builder.include(latLng);
		}
		return builder.build()
		              .getCenter();
	}

	@Override
	protected void populateBounds(LatLngBounds.Builder builder, GeoItem<Polyline, ITEM> value) {
		for (LatLng latLng : value.getGeometry()
		                          .getPoints()) {
			builder.include(latLng);
		}
	}

	@Override
	protected void showInfoWindow(GeoItem<Polyline, ITEM> value) {
		//Not used
	}

	@Override
	protected void hideInfoWindow(GeoItem<Polyline, ITEM> value) {
		//Not used
	}

	@Override
	protected void removeGeo(Polyline polylines) {
		polylines.remove();
	}

	@Override
	protected String geoClassName() {
		return Polyline.class.getSimpleName();
	}

	private boolean isVisible(Polyline polyline) {
		for (LatLng point : polyline.getPoints()) {
			if (point.latitude >= getMapBounds().southwest.latitude &&
			    point.longitude >= getMapBounds().southwest.longitude &&
			    point.latitude <= getMapBounds().northeast.latitude &&
			    point.longitude <= getMapBounds().northeast.longitude) {
				return true;
			}
		}
		return false;
	}
}
