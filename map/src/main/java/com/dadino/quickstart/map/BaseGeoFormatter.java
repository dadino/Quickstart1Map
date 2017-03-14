package com.dadino.quickstart.map;


import android.content.Context;

import com.dadino.quickstart.map.listeners.OnGeoExitAnimationFinishedListener;

public abstract class BaseGeoFormatter<GEO, ITEM> implements IGeoFormatter<GEO, ITEM> {

	protected final Context mAppContext;

	private boolean mAnimateGeoEnter;
	private boolean mAnimateGeoExit;

	public BaseGeoFormatter(Context context) {
		mAppContext = context.getApplicationContext();
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

	protected abstract void actuallyAnimateGeoExit(GeoItem<GEO, ITEM> geoItem, long delay,
	                                               OnGeoExitAnimationFinishedListener listener);

	protected void onGeoExitAnimationFinished(GeoItem<GEO, ITEM> geoItem,
	                                          OnGeoExitAnimationFinishedListener listener) {
		if (listener != null) listener.onGeoExitAnimationFinished(geoItem.getGeometry());
	}

	protected abstract void actuallyAnimateGeoEnter(GeoItem<GEO, ITEM> geoItem, long delay);

	@Override
	public void setAnimateGeoEnter(boolean animateGeoEnter) {
		this.mAnimateGeoEnter = animateGeoEnter;
	}

	@Override
	public void setAnimateGeoExit(boolean animateMarkerExit) {
		this.mAnimateGeoExit = animateMarkerExit;
	}

	@Override
	public boolean getAnimateGeoExit() {
		return mAnimateGeoExit;
	}

	@Override
	public boolean getAnimateGeoEnter() {
		return mAnimateGeoEnter;
	}
}
