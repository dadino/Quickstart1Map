package com.dadino.quickstart.map;


import android.content.Context;

import com.google.android.gms.maps.model.Polyline;

import java.util.List;

public abstract class BaseMultiPolylineFormatter<ITEM> extends BaseGeoFormatter<List<Polyline>,
		ITEM> {


	public BaseMultiPolylineFormatter(Context context) {
		super(context);
	}

	@Override
	public GeoItem<List<Polyline>, ITEM> editGeo(GeoItem<List<Polyline>, ITEM> oldGeoItem,
	                                             ITEM newItem) {
		for (Polyline polyline : oldGeoItem.getGeometry()) {
			polyline.remove();
		}

		return newGeo(newItem);
	}
}
