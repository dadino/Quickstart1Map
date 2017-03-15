package com.dadino.quickstart.map;


import android.content.Context;

import com.google.android.gms.maps.model.Polygon;

public abstract class BasePolygonFormatter<ITEM> extends BaseGeoFormatter<Polygon, ITEM> {


	public BasePolygonFormatter(Context context) {
		super(context);
	}


	@Override
	public GeoItem<Polygon, ITEM> editGeo(GeoItem<Polygon, ITEM> oldGeoItem, ITEM newItem) {
		GeoItem<Polygon, ITEM> newGeoItem = new GeoItem<>(oldGeoItem, newItem);

		updatePolygon(newGeoItem);

		return newGeoItem;
	}

	protected abstract void updatePolygon(GeoItem<Polygon, ITEM> geoItem);
}
