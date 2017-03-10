package com.dadino.quickstart.map.polylineformatters;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public interface IPolylineFormatter<ITEM> {

	void onMapReady();
	PolylineOptions newPolyline(ITEM item, List<LatLng> points);
	void editPolyline(Polyline marker, ITEM item, List<LatLng> points);
}
