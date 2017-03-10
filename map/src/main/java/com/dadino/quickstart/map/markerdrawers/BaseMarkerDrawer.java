package com.dadino.quickstart.map.markerdrawers;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dadino.quickstart.core.interfaces.INext;
import com.dadino.quickstart.core.utils.Logs;
import com.dadino.quickstart.map.listeners.OnInfoWindowClickedListener;
import com.dadino.quickstart.map.listeners.OnMarkerClickedListener;
import com.dadino.quickstart.map.listeners.OnMarkerExitAnimationFinishedListener;
import com.dadino.quickstart.map.listeners.OnSearchFromMapListener;
import com.dadino.quickstart.map.markerformatters.IMarkerFormatter;
import com.dadino.quickstart.map.misc.Equal;
import com.dadino.quickstart.map.wrappeditems.MarkedItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class BaseMarkerDrawer<KEY, ITEM> implements INext<List<ITEM>> {

	private static final int MAXIMUM_TOTAL_TIME_FOR_ANIMATION = 1000;
	private static final int MAXIMUM_DELAY                    = 50;
	private final IMarkerFormatter<ITEM>            formatter;
	private       OnSearchFromMapListener           searchListener;
	private       OnMarkerClickedListener<ITEM>     markerClickListener;
	private       OnInfoWindowClickedListener<ITEM> infoWindowClickListener;

	//Maps
	private Map<KEY, MarkedItem<ITEM>> mapItemMarker = new HashMap<>();

	//Varius
	private boolean    mInterceptMarkerClicks;
	private boolean    mInterceptInfoWindowClicks;
	private GoogleMap  map;
	private List<ITEM> items;

	public BaseMarkerDrawer(IMarkerFormatter<ITEM> formatter) {
		this.formatter = formatter;
	}

	public void setSearchListener(OnSearchFromMapListener searchListener) {
		this.searchListener = searchListener;
	}

	public void setMarkerClickedListener(OnMarkerClickedListener<ITEM> clickListener) {
		this.markerClickListener = clickListener;
	}

	public void setInfoWindowClickedListener(OnInfoWindowClickedListener<ITEM> clickListener) {
		this.infoWindowClickListener = clickListener;
	}

	public void setMap(GoogleMap map) {
		this.map = map;
		formatter.onMapReady();
		if (items != null) onItemNext(items);
	}

	@Override
	public void onItemNext(List<ITEM> items) {
		Logs.ui(className() + " loaded: " + items.size());
		if (map == null) {
			this.items = items;
		} else drawMarkers(items);
	}

	private void drawMarkers(@NonNull List<ITEM> items) {
		if (map == null) return;

		Logs.ui("Drawing " + items.size() + " " + className());
		this.items = null;

		if (items.isEmpty()) {

			long removeDelay = 0;
			final long removeDelayStep = mapItemMarker.entrySet()
			                                          .size() > 0 ? Math.min(
					MAXIMUM_TOTAL_TIME_FOR_ANIMATION / mapItemMarker.entrySet()
					                                                .size(), MAXIMUM_DELAY) : 0;
			for (Map.Entry<KEY, MarkedItem<ITEM>> entry : mapItemMarker.entrySet()) {
				removeMarkerAnimated(removeDelay, entry.getValue());
				removeDelay += removeDelayStep;
			}
			mapItemMarker.clear();
			Logs.ui("Removed all " + className());

			return;
		}

		List<ITEM> itemsToRemove = new ArrayList<>();
		List<ITEM> itemsToChange = new ArrayList<>();
		List<ITEM> itemsToAdd = new ArrayList<>();

		//Remove unneeded items
		for (Map.Entry<KEY, MarkedItem<ITEM>> entry : mapItemMarker.entrySet()) {
			boolean keep = false;
			final ITEM key = entry.getValue()
			                      .getItem();
			for (ITEM item : items) {
				if (Equal.equals(getId(key), getId(item))) {
					keep = true;
					break;
				}
			}
			if (!keep) itemsToRemove.add(key);
		}

		long removeDelay = 0;
		final long removeDelayStep = itemsToRemove.size() > 0 ? Math.min(
				MAXIMUM_TOTAL_TIME_FOR_ANIMATION / itemsToRemove.size(), MAXIMUM_DELAY) : 0;
		for (ITEM item : itemsToRemove) {
			removeMarkerAnimated(removeDelay, mapItemMarker.get(getId(item)));
			mapItemMarker.remove(getId(item));
			removeDelay += removeDelayStep;
		}

		//Edit changed items
		for (Map.Entry<KEY, MarkedItem<ITEM>> entry : mapItemMarker.entrySet()) {
			final ITEM key = entry.getValue()
			                      .getItem();
			for (ITEM item : items) {
				if (Equal.equals(getId(key), getId(item))) {
					if (needEdit(key, item)) {
						itemsToChange.add(item);
						break;
					}
				}
			}
		}
		for (ITEM item : itemsToChange) {
			final Marker marker = mapItemMarker.get(getId(item))
			                                   .getMarker();
			mapItemMarker.put(getId(item), new MarkedItem<>(item, marker));
			formatter.editMarker(marker, item);
		}

		//Add new items
		for (ITEM item : items) {
			boolean found = false;

			for (Map.Entry<KEY, MarkedItem<ITEM>> entry : mapItemMarker.entrySet()) {
				if (Equal.equals(getId(item), getId(entry.getValue()
				                                         .getItem()))) {
					found = true;
					break;
				}
			}
			if (!found) itemsToAdd.add(item);
		}
		long delay = 0;
		final long delayStep = itemsToAdd.size() > 0 ? Math.min(
				MAXIMUM_TOTAL_TIME_FOR_ANIMATION / itemsToAdd.size(), MAXIMUM_DELAY) : 0;
		for (ITEM item : itemsToAdd) {
			Marker marker = map.addMarker(formatter.newMarker(item));
			final MarkedItem<ITEM> markedItem = new MarkedItem<>(item, marker);
			mapItemMarker.put(getId(item), markedItem);
			formatter.animateMarkerEnter(markedItem, delay);
			delay += delayStep;
		}
		Logs.ui(className() + " -> markers to remove: " + itemsToRemove.size());
		Logs.ui(className() + " -> markers to change: " + itemsToChange.size());
		Logs.ui(className() + " -> markers to add: " + itemsToAdd.size());
	}

	private void removeMarkerAnimated(long removeDelay, MarkedItem<ITEM> markedItem) {
		formatter.animateMarkerExit(markedItem, removeDelay,
				new OnMarkerExitAnimationFinishedListener() {

					@Override
					public void onMarkerExitAnimationFinished(Marker marker) {
						marker.remove();
					}
				});
	}


	public boolean onMarkerClicked(Marker marker) {
		if (mInterceptMarkerClicks) {
			ITEM item = itemFromMarker(marker);
			if (item != null && markerClickListener != null) {
				markerClickListener.onItemMarkerClicked(item);
				return true;
			}
		}
		return false;
	}

	public boolean onInfoWindowClicked(Marker marker) {
		if (mInterceptInfoWindowClicks) {
			ITEM item = itemFromMarker(marker);
			if (item != null && infoWindowClickListener != null) {
				infoWindowClickListener.onItemInfoWindowClicked(item);
				return true;
			}
		}

		return false;
	}


	public void setInterceptMarkerClicks(boolean interceptItemClicks) {
		this.mInterceptMarkerClicks = interceptItemClicks;
	}


	public void setInterceptInfoWindowClicks(boolean interceptItemClicks) {
		this.mInterceptInfoWindowClicks = interceptItemClicks;
	}

	public void setAnimateMarkerEnter(boolean animateMarkerEnter) {
		formatter.setAnimateMarkerEnter(animateMarkerEnter);
	}

	public void setAnimateMarkerExit(boolean animateMarkerExit) {
		formatter.setAnimateMarkerExit(animateMarkerExit);
	}

	public void onMarkerLoadRequested(float zoom) {
		if (zoom >= getMinumumZoomToSearch()) {
			Logs.ui("Loading " + className() + " for zoom " + zoom + ": true");
			if (searchListener != null) searchListener.onSearchRequested();
		} else {
			Logs.ui("Loading  " + className() + " for zoom " + zoom + ": false");
			if (searchListener != null) searchListener.onTooFarToSee();
		}
	}

	protected abstract float getMinumumZoomToSearch();


	private ITEM itemFromMarker(Marker marker) {
		for (Map.Entry<KEY, MarkedItem<ITEM>> entry : mapItemMarker.entrySet()) {
			if (entry.getValue()
			         .getMarker()
			         .equals(marker)) return entry.getValue()
			                                      .getItem();
		}
		return null;
	}

	protected abstract KEY getId(ITEM item);
	protected abstract boolean needEdit(ITEM oldItem, ITEM newItem);
	protected abstract String className();

	@Nullable
	public LatLngBounds getBounds() {
		LatLngBounds.Builder builder = LatLngBounds.builder();
		for (Map.Entry<KEY, MarkedItem<ITEM>> entry : mapItemMarker.entrySet()) {
			builder.include(entry.getValue()
			                     .getMarker()
			                     .getPosition());
		}
		try {
			return builder.build();
		} catch (IllegalStateException ex) {
			return null;
		}
	}

	public IMarkerFormatter<ITEM> getFormatter() {
		return formatter;
	}
}
