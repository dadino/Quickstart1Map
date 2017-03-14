package com.dadino.quickstart.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public abstract class BaseMarkerDrawer<KEY, ITEM> extends BaseGeoDrawer<KEY, Marker, ITEM> {

	public BaseMarkerDrawer(BaseMarkerFormatter<ITEM> formatter) {
		super(formatter);
	}

	@Override
	protected LatLng getPosition(GeoItem<Marker, ITEM> value) {
		return value.getGeometry()
		            .getPosition();
	}

	@Override
	protected void populateBounds(LatLngBounds.Builder builder, GeoItem<Marker, ITEM> value) {
		builder.include(value.getGeometry()
		                     .getPosition());
	}

	@Override
	protected void showInfoWindow(GeoItem<Marker, ITEM> value) {
		value.getGeometry()
		     .showInfoWindow();
	}

	@Override
	protected void hideInfoWindow(GeoItem<Marker, ITEM> value) {
		value.getGeometry()
		     .hideInfoWindow();
	}

	@Override
	protected void removeGeo(Marker marker) {
		marker.remove();
	}

	@Override
	protected String geoClassName() {
		return Marker.class.getSimpleName();
	}
}