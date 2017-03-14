package com.dadino.quickstart.map;


import android.content.Context;

import com.google.android.gms.maps.model.Polyline;

public abstract class BasePolylineFormatter<ITEM> extends BaseGeoFormatter<Polyline, ITEM> {


	public BasePolylineFormatter(Context context) {
		super(context);
	}
}
