package com.dadino.quickstart.map;


import android.content.Context;

import com.google.android.gms.maps.model.Polygon;

public abstract class BasePolygonFormatter<ITEM> extends BaseGeoFormatter<Polygon, ITEM> {


	public BasePolygonFormatter(Context context) {
		super(context);
	}
}
