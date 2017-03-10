package com.dadino.quickstart.map.polylineformatters;

import android.content.Context;


public abstract class BasePolylineFormatter<ITEM> implements IPolylineFormatter<ITEM> {

	protected final Context mAppContext;

	public BasePolylineFormatter(Context context) {
		this.mAppContext = context.getApplicationContext();
	}
}
