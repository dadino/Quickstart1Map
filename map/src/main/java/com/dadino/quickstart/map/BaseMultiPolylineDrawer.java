package com.dadino.quickstart.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;
import java.util.Map;


public abstract class BaseMultiPolylineDrawer<KEY, ITEM> extends BaseGeoDrawer<KEY,
		List<Polyline>, ITEM> {


	public BaseMultiPolylineDrawer(BaseMultiPolylineFormatter<ITEM> formatter) {
		super(formatter);
	}

	protected void onMapBoundsUpdated() {
		for (Map.Entry<KEY, GeoItem<List<Polyline>, ITEM>> entry : getItemMap().entrySet()) {
			for (Polyline polyline : entry.getValue()
			                              .getGeometry()) {
				polyline.setVisible(isVisible(polyline));
			}
		}
	}

	@Override
	protected LatLng getPosition(GeoItem<List<Polyline>, ITEM> value) {
		final LatLngBounds.Builder builder = LatLngBounds.builder();
		for (Polyline polyline : value.getGeometry()) {
			for (LatLng latLng : polyline.getPoints()) {
				builder.include(latLng);
			}
		}
		return builder.build()
		              .getCenter();
	}

	@Override
	protected void populateBounds(LatLngBounds.Builder builder,
	                              GeoItem<List<Polyline>, ITEM> value) {
		for (Polyline polyline : value.getGeometry()) {
			for (LatLng latLng : polyline.getPoints()) {
				builder.include(latLng);
			}
		}
	}

	@Override
	protected void showInfoWindow(GeoItem<List<Polyline>, ITEM> value) {
		//Not used
	}

	@Override
	protected void hideInfoWindow(GeoItem<List<Polyline>, ITEM> value) {
		//Not used
	}

	@Override
	protected void removeGeo(List<Polyline> polylines) {
		for (Polyline polyline : polylines) {
			polyline.remove();
		}
	}

	@Override
	protected String geoClassName() {
		return "List<" + Polyline.class.getSimpleName() + ">";
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
