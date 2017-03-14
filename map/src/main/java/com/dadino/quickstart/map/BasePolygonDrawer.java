package com.dadino.quickstart.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;

import java.util.Map;


public abstract class BasePolygonDrawer<KEY, ITEM> extends BaseGeoDrawer<KEY, Polygon, ITEM> {


	public BasePolygonDrawer(BasePolygonFormatter<ITEM> formatter) {
		super(formatter);
	}

	protected void onMapBoundsUpdated() {
		for (Map.Entry<KEY, GeoItem<Polygon, ITEM>> entry : getItemMap().entrySet()) {
			final Polygon polygon = entry.getValue()
			                             .getGeometry();
			polygon.setVisible(isVisible(polygon));
		}
	}

	@Override
	protected LatLng getPosition(GeoItem<Polygon, ITEM> value) {
		final LatLngBounds.Builder builder = LatLngBounds.builder();
		for (LatLng latLng : value.getGeometry()
		                          .getPoints()) {
			builder.include(latLng);
		}
		return builder.build()
		              .getCenter();
	}

	@Override
	protected void populateBounds(LatLngBounds.Builder builder, GeoItem<Polygon, ITEM> value) {
		for (LatLng latLng : value.getGeometry()
		                          .getPoints()) {
			builder.include(latLng);
		}
	}

	@Override
	protected void showInfoWindow(GeoItem<Polygon, ITEM> value) {
		//Not used
	}

	@Override
	protected void hideInfoWindow(GeoItem<Polygon, ITEM> value) {
		//Not used
	}

	@Override
	protected void removeGeo(Polygon polygons) {
		polygons.remove();
	}

	@Override
	protected String geoClassName() {
		return Polygon.class.getSimpleName();
	}

	private boolean isVisible(Polygon polygon) {
		for (LatLng point : polygon.getPoints()) {
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
