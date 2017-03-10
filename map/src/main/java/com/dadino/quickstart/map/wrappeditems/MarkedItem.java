package com.dadino.quickstart.map.wrappeditems;

import com.google.android.gms.maps.model.Marker;


public class MarkedItem<ITEM> {

	private final ITEM   item;
	private final Marker marker;
	private       float  scale;

	public MarkedItem(ITEM item, Marker marker) {
		this.item = item;
		this.marker = marker;
	}

	public ITEM getItem() {
		return item;
	}

	public Marker getMarker() {
		return marker;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}
}
