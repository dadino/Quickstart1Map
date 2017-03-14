package com.dadino.quickstart.map;


import android.support.annotation.NonNull;

import com.dadino.quickstart.map.listeners.OnGeoExitAnimationFinishedListener;
import com.google.android.gms.maps.GoogleMap;

public interface IGeoFormatter<GEO, ITEM> {

	void onMapReady();

	GeoItem<GEO, ITEM> newGeo(@NonNull GoogleMap map, ITEM item);
	GeoItem<GEO, ITEM> editGeo(GeoItem<GEO, ITEM> oldGeoItem, ITEM newItem);

	void animateGeoEnter(GeoItem<GEO, ITEM> geo, long delay);
	void animateGeoExit(GeoItem<GEO, ITEM> geo, long delay,
	                    OnGeoExitAnimationFinishedListener listener);
	boolean getAnimateGeoEnter();
	void setAnimateGeoEnter(boolean animateGeoEnter);
	boolean getAnimateGeoExit();
	void setAnimateGeoExit(boolean animateGeoExit);
	void highlightGeo(GeoItem<GEO, ITEM> geoItem);
	void unhighlightGeo(GeoItem<GEO, ITEM> geoItem);
}
