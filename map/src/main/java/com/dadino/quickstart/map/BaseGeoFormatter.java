package com.dadino.quickstart.map;


import android.content.Context;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.dadino.quickstart.map.listeners.OnGeoExitAnimationFinishedListener;
import com.google.android.gms.maps.GoogleMap;

public abstract class BaseGeoFormatter<GEO, ITEM> implements IGeoFormatter<GEO, ITEM> {

	protected final Context   mAppContext;
	protected       GoogleMap map;
	private         boolean   mAnimateGeoEnter;
	private         boolean   mAnimateGeoExit;

	public BaseGeoFormatter(Context context) {
		mAppContext = context.getApplicationContext();
	}

	@Override
	@CallSuper
	public void onMapReady(@NonNull GoogleMap map) {
		this.map = map;
	}

	@Override
	public void animateGeoEnter(GeoItem<GEO, ITEM> markedItem, long delay) {
		if (mAnimateGeoEnter) {
			actuallyAnimateGeoEnter(markedItem, delay);
		}
	}

	@Override
	public void animateGeoExit(GeoItem<GEO, ITEM> markedItem, long delay,
	                           OnGeoExitAnimationFinishedListener listener) {
		if (mAnimateGeoExit) {
			actuallyAnimateGeoExit(markedItem, delay, listener);
		} else {
			onGeoExitAnimationFinished(markedItem, listener);
		}
	}

	@Override
	public boolean getAnimateGeoEnter() {
		return mAnimateGeoEnter;
	}

	@Override
	public void setAnimateGeoEnter(boolean animateGeoEnter) {
		this.mAnimateGeoEnter = animateGeoEnter;
	}

	@Override
	public boolean getAnimateGeoExit() {
		return mAnimateGeoExit;
	}

	@Override
	public void setAnimateGeoExit(boolean animateMarkerExit) {
		this.mAnimateGeoExit = animateMarkerExit;
	}

	protected abstract void actuallyAnimateGeoExit(GeoItem<GEO, ITEM> geoItem, long delay,
	                                               OnGeoExitAnimationFinishedListener listener);

	protected void onGeoExitAnimationFinished(GeoItem<GEO, ITEM> geoItem,
	                                          OnGeoExitAnimationFinishedListener listener) {
		if (listener != null) listener.onGeoExitAnimationFinished(geoItem.getGeometry());
	}

	protected abstract void actuallyAnimateGeoEnter(GeoItem<GEO, ITEM> geoItem, long delay);
}
