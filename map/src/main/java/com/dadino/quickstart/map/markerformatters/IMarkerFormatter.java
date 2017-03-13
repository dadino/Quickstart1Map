package com.dadino.quickstart.map.markerformatters;

import com.dadino.quickstart.map.listeners.OnMarkerExitAnimationFinishedListener;
import com.dadino.quickstart.map.wrappeditems.MarkedItem;
import com.google.android.gms.maps.model.MarkerOptions;

public interface IMarkerFormatter<ITEM> {

	void onMapReady();
	MarkerOptions newMarker(ITEM item);
	void editMarker(MarkedItem<ITEM> marker);
	void animateMarkerEnter(MarkedItem<ITEM> marker, long delay);
	void setAnimateMarkerEnter(boolean animateMarkerEnter);
	void animateMarkerExit(MarkedItem<ITEM> marker, long delay,
	                       OnMarkerExitAnimationFinishedListener listener);
	void setAnimateMarkerExit(boolean animateMarkerExit);
	void highlightMarker(MarkedItem markedItem);
	void unhighlightMarker(MarkedItem markedItem);
}
