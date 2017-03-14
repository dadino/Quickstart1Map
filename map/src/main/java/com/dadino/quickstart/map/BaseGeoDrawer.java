package com.dadino.quickstart.map;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dadino.quickstart.core.interfaces.INext;
import com.dadino.quickstart.core.utils.Logs;
import com.dadino.quickstart.map.listeners.OnGeoClickedListener;
import com.dadino.quickstart.map.listeners.OnGeoExitAnimationFinishedListener;
import com.dadino.quickstart.map.listeners.OnInfoWindowClickedListener;
import com.dadino.quickstart.map.listeners.OnSearchFromMapListener;
import com.dadino.quickstart.map.misc.Equal;
import com.dadino.quickstart.map.misc.GeoItemMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class BaseGeoDrawer<KEY, GEO, ITEM> implements INext<List<ITEM>> {

	private static final int MAXIMUM_TOTAL_TIME_FOR_ANIMATION = 1000;
	private static final int MAXIMUM_DELAY                    = 50;
	private final IGeoFormatter<GEO, ITEM> formatter;
	private final Class geoType = ((GEO) new Object()).getClass();
	private OnSearchFromMapListener           searchListener;
	private OnGeoClickedListener<GEO, ITEM>   geoClickListener;
	private OnInfoWindowClickedListener<ITEM> infoWindowClickListener;
	//Maps
	private Map<KEY, GeoItem<GEO, ITEM>> mapItemGeo = new GeoItemMap<>();

	//Varius
	private boolean            mInterceptGeoClicks;
	private boolean            mInterceptInfoWindowClicks;
	private GoogleMap          map;
	private List<ITEM>         items;
	private boolean            mConsumeGeoClicks;
	private GeoItem<GEO, ITEM> mSelectedItem;
	private LatLngBounds       mMapBounds;

	public BaseGeoDrawer(IGeoFormatter<GEO, ITEM> formatter) {
		this.formatter = formatter;
	}

	public void setSearchListener(OnSearchFromMapListener searchListener) {
		this.searchListener = searchListener;
	}

	public void setGeoClickedListener(OnGeoClickedListener<GEO, ITEM> clickListener) {
		this.geoClickListener = clickListener;
	}

	public void setInfoWindowClickedListener(OnInfoWindowClickedListener<ITEM> clickListener) {
		this.infoWindowClickListener = clickListener;
	}

	@Override
	public void onItemNext(List<ITEM> items) {
		Logs.ui(itemClassName() + " loaded: " + items.size());
		if (map == null) {
			this.items = items;
		} else drawGeos(items);
	}

	private void drawGeos(@NonNull List<ITEM> items) {
		if (map == null) return;

		Logs.ui("Drawing " + items.size() + " " + geoClassName() + " for " + itemClassName());
		this.items = null;

		if (items.isEmpty()) {

			long removeDelay = 0;
			final long removeDelayStep = mapItemGeo.entrySet()
			                                       .size() > 0 ? Math.min(
					MAXIMUM_TOTAL_TIME_FOR_ANIMATION / mapItemGeo.entrySet()
					                                             .size(), MAXIMUM_DELAY) : 0;
			for (Map.Entry<KEY, GeoItem<GEO, ITEM>> entry : mapItemGeo.entrySet()) {
				removeGeoAnimated(removeDelay, entry.getValue());
				removeDelay += removeDelayStep;
			}
			mapItemGeo.clear();
			Logs.ui("Removed all " + geoClassName() + " for " + itemClassName());

			return;
		}

		List<ITEM> itemsToRemove = new ArrayList<>();
		List<ITEM> itemsToChange = new ArrayList<>();
		List<ITEM> itemsToAdd = new ArrayList<>();

		//Remove unneeded items
		for (Map.Entry<KEY, GeoItem<GEO, ITEM>> entry : mapItemGeo.entrySet()) {
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
			removeGeoAnimated(removeDelay, mapItemGeo.get(getId(item)));
			mapItemGeo.remove(getId(item));
			removeDelay += removeDelayStep;
		}

		//Edit changed items
		for (Map.Entry<KEY, GeoItem<GEO, ITEM>> entry : mapItemGeo.entrySet()) {
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
			final GeoItem<GEO, ITEM> oldGeoItem = mapItemGeo.get(getId(item));
			final GeoItem<GEO, ITEM> newGeoItem = formatter.editGeo(oldGeoItem, item);
			;
			mapItemGeo.put(getId(item), newGeoItem);
		}

		//Add new items
		for (ITEM item : items) {
			boolean found = false;

			for (Map.Entry<KEY, GeoItem<GEO, ITEM>> entry : mapItemGeo.entrySet()) {
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
			final GeoItem<GEO, ITEM> markedItem = formatter.newGeo(map, item);
			mapItemGeo.put(getId(item), markedItem);
			formatter.animateGeoEnter(markedItem, delay);
			delay += delayStep;
		}
		Logs.ui(itemClassName() + " -> " + geoClassName() + " to remove: " + itemsToRemove.size());
		Logs.ui(itemClassName() + " -> " + geoClassName() + " to change: " + itemsToChange.size());
		Logs.ui(itemClassName() + " -> " + geoClassName() + " to add: " + itemsToAdd.size());

		updateMapBounds();
	}

	public void updateMapBounds() {
		if (getMap() != null) {
			this.mMapBounds = getMap().getProjection()
			                          .getVisibleRegion().latLngBounds;
			onMapBoundsUpdated();
		}
	}

	protected abstract void onMapBoundsUpdated();

	private void removeGeoAnimated(long removeDelay, GeoItem<GEO, ITEM> markedItem) {
		formatter.animateGeoExit(markedItem, removeDelay,
				new OnGeoExitAnimationFinishedListener<GEO>() {

					@Override
					public void onGeoExitAnimationFinished(GEO geo) {
						removeGeo(geo);
					}
				});
	}

	public boolean onGeoClicked(GEO geo) {
		if (!geoType.isInstance(geo)) return false;
		if (mInterceptGeoClicks) {
			ITEM item = itemFromGeo(geo);
			if (item != null && geoClickListener != null) {
				geoClickListener.onGeoClicked(geo, item);
				setSelectedItem(itemFromGeo(geo));
				return mConsumeGeoClicks;
			}
		}
		return false;
	}

	public boolean onInfoWindowClicked(GEO geo) {
		if (!geoType.isInstance(geo)) return false;
		if (mInterceptInfoWindowClicks) {
			ITEM item = itemFromGeo(geo);
			if (item != null && infoWindowClickListener != null) {
				infoWindowClickListener.onItemInfoWindowClicked(item);
				return true;
			}
		}

		return false;
	}

	public void setInterceptGeoClicks(boolean interceptGeoClicks) {
		this.mInterceptGeoClicks = interceptGeoClicks;
	}

	public void setConsumeGeoClicks(boolean consumeGeoClicks) {
		this.mConsumeGeoClicks = consumeGeoClicks;
	}

	public void setInterceptInfoWindowClicks(boolean interceptGeoClicks) {
		this.mInterceptInfoWindowClicks = interceptGeoClicks;
	}

	public void setAnimateGeoEnter(boolean animateGeoEnter) {
		formatter.setAnimateGeoEnter(animateGeoEnter);
	}

	public void setAnimateGeoExit(boolean animateGeoExit) {
		formatter.setAnimateGeoExit(animateGeoExit);
	}

	public void onGeoLoadRequested(float zoom) {
		if (zoom >= getMinumumZoomToSearch()) {
			Logs.ui("Loading " + geoClassName() + " for " + itemClassName() + " at zoom " + zoom +
			        ": true");
			if (searchListener != null) searchListener.onSearchRequested();
		} else {
			Logs.ui("Loading " + geoClassName() + " for " + itemClassName() + " at zoom " + zoom +
			        ": false");
			if (searchListener != null) searchListener.onTooFarToSee();
		}

		updateMapBounds();
	}

	protected abstract float getMinumumZoomToSearch();

	private ITEM itemFromGeo(GEO geo) {
		for (Map.Entry<KEY, GeoItem<GEO, ITEM>> entry : mapItemGeo.entrySet()) {
			if (entry.getValue()
			         .getGeometry()
			         .equals(geo)) return entry.getValue()
			                                   .getItem();
		}
		return null;
	}

	@Nullable
	public LatLngBounds getBounds() {
		LatLngBounds.Builder builder = LatLngBounds.builder();
		for (Map.Entry<KEY, GeoItem<GEO, ITEM>> entry : mapItemGeo.entrySet()) {
			populateBounds(builder, entry.getValue());
		}
		try {
			return builder.build();
		} catch (IllegalStateException ex) {
			return null;
		}
	}

	public IGeoFormatter<GEO, ITEM> getFormatter() {
		return formatter;
	}

	public void setSelectedItem(ITEM item) {
		if (mSelectedItem != null && mSelectedItem.isManaged()) {
			unhighlightGeo(mSelectedItem);
			hideInfoWindow(mSelectedItem);
		}

		this.mSelectedItem = mapItemGeo.get(getId(item));

		if (mSelectedItem != null && mSelectedItem.isManaged()) {
			highlightGeo(mSelectedItem);
			showInfoWindow(mSelectedItem);
			map.animateCamera(CameraUpdateFactory.newLatLng(getPosition(mSelectedItem)));
		}
	}

	private void highlightGeo(GeoItem<GEO, ITEM> markedItem) {
		formatter.highlightGeo(markedItem);
	}

	private void unhighlightGeo(GeoItem<GEO, ITEM> markedItem) {
		formatter.unhighlightGeo(markedItem);
	}

	protected GoogleMap getMap() {
		return map;
	}

	public void setMap(GoogleMap map) {
		this.map = map;
		formatter.onMapReady();
		if (items != null) onItemNext(items);
	}

	protected Map<KEY, GeoItem<GEO, ITEM>> getItemMap() {
		return mapItemGeo;
	}

	protected abstract LatLng getPosition(GeoItem<GEO, ITEM> value);
	protected abstract void populateBounds(LatLngBounds.Builder builder, GeoItem<GEO, ITEM> value);
	protected abstract void showInfoWindow(GeoItem<GEO, ITEM> value);
	protected abstract void hideInfoWindow(GeoItem<GEO, ITEM> value);
	protected abstract void removeGeo(GEO geo);
	protected abstract KEY getId(ITEM item);
	protected abstract boolean needEdit(ITEM oldItem, ITEM newItem);
	protected abstract String itemClassName();
	protected abstract String geoClassName();

	public LatLngBounds getMapBounds() {
		return mMapBounds;
	}
}
