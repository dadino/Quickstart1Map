package com.dadino.quickstart.map.wrappeditems;

import com.google.android.gms.maps.model.Polyline;

import java.util.List;


public class PolylinedItem<ITEM> {

	private final ITEM           item;
	private final List<Polyline> polylines;

	public PolylinedItem(ITEM item, List<Polyline> polylines) {
		this.item = item;
		this.polylines = polylines;
	}

	public ITEM getItem() {
		return item;
	}

	public List<Polyline> getPolylines() {
		return polylines;
	}
}
