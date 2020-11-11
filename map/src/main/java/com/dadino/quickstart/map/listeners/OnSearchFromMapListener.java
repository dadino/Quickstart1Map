package com.dadino.quickstart.map.listeners;


import com.google.android.gms.maps.model.LatLngBounds;

public interface OnSearchFromMapListener {

	void onTooFarToSee();
	void onSearchRequested(LatLngBounds bounds);
}
