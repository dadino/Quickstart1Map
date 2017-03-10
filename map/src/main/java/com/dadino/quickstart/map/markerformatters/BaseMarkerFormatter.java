package com.dadino.quickstart.map.markerformatters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Property;

import com.dadino.quickstart.map.listeners.OnMarkerExitAnimationFinishedListener;
import com.dadino.quickstart.map.wrappeditems.MarkedItem;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Marker;

public abstract class BaseMarkerFormatter<ITEM> implements IMarkerFormatter<ITEM> {

	protected final Context mAppContext;
	protected Property<Marker, Float>           markerAlphaProperty = new Property<Marker, Float>(
			Float.class, "alpha") {
		@Override
		public void set(Marker object, Float value) {
			object.setAlpha(value);
		}

		@Override
		public Float get(Marker object) {
			return object.getAlpha();
		}
	};
	protected Property<MarkedItem<ITEM>, Float> markerScaleProperty =
			new Property<MarkedItem<ITEM>, Float>(Float.class, "scale") {
				@Override
				public void set(MarkedItem<ITEM> object, Float value) {
					try {
						if (object != null && object.getMarker() != null) {
							object.setScale(value);
							object.getMarker()
							      .setIcon(getScaledBitmapDescriptor(object, value));
						}
					} catch (Exception ignored) {
					}
				}

				@Override
				public Float get(MarkedItem<ITEM> object) {
					return object.getScale();
				}
			};
	private boolean mAnimateMarkerEnter;
	private boolean mAnimateMarkerExit;

	public BaseMarkerFormatter(Context context) {
		mAppContext = context.getApplicationContext();
	}

	@Override
	public void animateMarkerEnter(MarkedItem<ITEM> markedItem, long delay) {
		if (mAnimateMarkerEnter) {
			actuallyAnimateMarkerEnter(markedItem, delay);
		}
	}

	protected abstract void actuallyAnimateMarkerExit(MarkedItem<ITEM> markedItem, long delay,
	                                                  OnMarkerExitAnimationFinishedListener
			                                                  listener);

	protected void onMarkerExitAnimationFinished(MarkedItem<ITEM> markedItem,
	                                             OnMarkerExitAnimationFinishedListener listener) {
		if (listener != null) listener.onMarkerExitAnimationFinished(markedItem.getMarker());
	}

	protected abstract void actuallyAnimateMarkerEnter(MarkedItem<ITEM> markedItem, long delay);

	@NonNull
	protected abstract BitmapDescriptor getScaledBitmapDescriptor(MarkedItem<ITEM> object,
	                                                              Float value);

	public boolean getAnimateMarkerEnter() {
		return mAnimateMarkerEnter;
	}

	@Override
	public void setAnimateMarkerEnter(boolean animateMarkerEnter) {
		this.mAnimateMarkerEnter = animateMarkerEnter;
	}

	@Override
	public void animateMarkerExit(MarkedItem<ITEM> markedItem, long delay,
	                              OnMarkerExitAnimationFinishedListener listener) {
		if (mAnimateMarkerExit) {
			actuallyAnimateMarkerExit(markedItem, delay, listener);
		} else {
			onMarkerExitAnimationFinished(markedItem, listener);
		}
	}

	public boolean getAnimateMarkerExit() {
		return mAnimateMarkerExit;
	}

	@Override
	public void setAnimateMarkerExit(boolean animateMarkerExit) {
		this.mAnimateMarkerExit = animateMarkerExit;
	}
}
