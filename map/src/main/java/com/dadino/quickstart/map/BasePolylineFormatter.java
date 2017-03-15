package com.dadino.quickstart.map;


import android.content.Context;

import com.google.android.gms.maps.model.Polyline;

public abstract class BasePolylineFormatter<ITEM> extends BaseGeoFormatter<Polyline, ITEM> {


	public BasePolylineFormatter(Context context) {
		super(context);
	}


	@Override
	public GeoItem<Polyline, ITEM> editGeo(GeoItem<Polyline, ITEM> oldGeoItem, ITEM newItem) {
		GeoItem<Polyline, ITEM> newGeoItem = new GeoItem<>(oldGeoItem, newItem);

		updatePolyline(newGeoItem);

		return newGeoItem;
	}

	protected abstract void updatePolyline(GeoItem<Polyline, ITEM> geoItem);
}
