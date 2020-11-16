package com.dadino.quickstart.map.listeners;


import com.google.android.gms.maps.model.LatLngBounds;

public interface OnSearchFromMapListener {

    void onTooFarToSee(float zoom, LatLngBounds bounds);

    void onSearchRequested(float zoom, LatLngBounds bounds);
}
