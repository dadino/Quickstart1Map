package com.dadino.quickstart.map.polylinedrawers;


import android.support.annotation.Nullable;

import com.dadino.quickstart.core.interfaces.INext;
import com.dadino.quickstart.core.utils.Logs;
import com.dadino.quickstart.map.listeners.OnPolylineClickedListener;
import com.dadino.quickstart.map.listeners.OnSearchFromMapListener;
import com.dadino.quickstart.map.misc.Equal;
import com.dadino.quickstart.map.polylineformatters.IPolylineFormatter;
import com.dadino.quickstart.map.wrappeditems.PolylinedItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePolylineDrawer<ITEM> implements INext<List<ITEM>> {

	private final IPolylineFormatter<ITEM>        formatter;
	private       OnSearchFromMapListener         searchListener;
	private       OnPolylineClickedListener<ITEM> polylineClickListener;

	//Maps
	private Map<Long, PolylinedItem<ITEM>> mapItemPolyline = new HashMap<>();

	//Varius
	private boolean      mInterceptItemClicks;
	private GoogleMap    map;
	private List<ITEM>   items;
	private LatLngBounds mMapBounds;

	public BasePolylineDrawer(IPolylineFormatter<ITEM> formatter) {
		this.formatter = formatter;
	}

	public void setSearchListener(OnSearchFromMapListener searchListener) {
		this.searchListener = searchListener;
	}

	public void setPolylineClickedListener(OnPolylineClickedListener<ITEM> clickListener) {
		this.polylineClickListener = clickListener;
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
		} else drawPolylines(items);
	}

	private void drawPolylines(List<ITEM> items) {
		if (map == null) return;

		Logs.ui("Drawing " + items.size() + " " + className());
		this.items = null;

		if (items.isEmpty()) {
			for (Map.Entry<Long, PolylinedItem<ITEM>> entry : mapItemPolyline.entrySet()) {
				for (Polyline polyline : entry.getValue()
				                              .getPolylines()) {
					polyline.remove();
				}
			}
			mapItemPolyline.clear();
			Logs.ui("Removed all polylines for " + className());

			return;
		}

		List<ITEM> itemsToRemove = new ArrayList<>();
		List<ITEM> itemsToChange = new ArrayList<>();
		List<ITEM> itemsToAdd = new ArrayList<>();

		//Remove unneeded items
		for (Map.Entry<Long, PolylinedItem<ITEM>> entry : mapItemPolyline.entrySet()) {
			boolean keep = false;
			final ITEM key = entry.getValue()
			                      .getItem();
			for (ITEM item : items) {
				if (getId(key) == getId(item)) {
					keep = true;
					break;
				}
			}
			if (!keep) itemsToRemove.add(key);
		}
		for (ITEM item : itemsToRemove) {
			for (Polyline polyline : mapItemPolyline.get(getId(item))
			                                        .getPolylines()) {
				polyline.remove();
			}
			mapItemPolyline.remove(getId(item));
		}

		//Edit changed items
		for (Map.Entry<Long, PolylinedItem<ITEM>> entry : mapItemPolyline.entrySet()) {
			final ITEM key = entry.getValue()
			                      .getItem();
			for (ITEM item : items) {
				if (getId(key) == getId(item)) {
					if (needEdit(key, item)) {
						itemsToChange.add(item);
						break;
					}
				}
			}
		}
		for (ITEM item : itemsToChange) {
			List<Polyline> polylines = new ArrayList<>();
			for (Polyline polyline : mapItemPolyline.get(getId(item))
			                                        .getPolylines()) {
				polyline.remove();
			}
			for (List<LatLng> points : getListOfPolylines(item)) {
				Polyline polyline = map.addPolyline(formatter.newPolyline(item, points));
				polylines.add(polyline);
			}
			mapItemPolyline.put(getId(item), new PolylinedItem<>(item, polylines));
		}

		//Add new items
		for (ITEM item : items) {
			boolean found = false;

			for (Map.Entry<Long, PolylinedItem<ITEM>> entry : mapItemPolyline.entrySet()) {
				if (getId(item) == getId(entry.getValue()
				                              .getItem())) {
					found = true;
					break;
				}
			}
			if (!found) itemsToAdd.add(item);
		}
		for (ITEM item : itemsToAdd) {
			List<Polyline> polylines = new ArrayList<>();
			for (List<LatLng> listOfPoints : getListOfPolylines(item)) {
				Polyline polyline = map.addPolyline(formatter.newPolyline(item, listOfPoints));
				polylines.add(polyline);
			}
			mapItemPolyline.put(getId(item), new PolylinedItem<>(item, polylines));
		}
		Logs.ui(className() + "-> polylines to remove: " + itemsToRemove.size());
		Logs.ui(className() + "-> polylines to change: " + itemsToChange.size());
		Logs.ui(className() + "-> polylines to add: " + itemsToAdd.size());

		updateMapBounds();
	}


	public boolean onPolylineClicked(Polyline polyline) {
		if (mInterceptItemClicks) {
			ITEM item = itemFromPolyline(polyline);
			if (item != null && polylineClickListener != null) {
				polylineClickListener.onItemPolylineClicked(item);
				return true;
			}
		}
		return false;
	}

	public void setInterceptPolylineClicks(boolean interceptPolylineClicks) {
		this.mInterceptItemClicks = interceptPolylineClicks;
	}

	public void updateMapBounds() {
		if (map != null) {
			this.mMapBounds = map.getProjection()
			                     .getVisibleRegion().latLngBounds;
			updatePolylinesVisibility();
		}
	}

	private void updatePolylinesVisibility() {
		for (Map.Entry<Long, PolylinedItem<ITEM>> entry : mapItemPolyline.entrySet()) {
			for (Polyline polyline : entry.getValue()
			                              .getPolylines()) {
				polyline.setVisible(isVisible(polyline));
			}
		}
	}

	private boolean isVisible(Polyline polyline) {
		for (LatLng point : polyline.getPoints()) {
			if (point.latitude >= mMapBounds.southwest.latitude &&
			    point.longitude >= mMapBounds.southwest.longitude &&
			    point.latitude <= mMapBounds.northeast.latitude &&
			    point.longitude <= mMapBounds.northeast.longitude) {
				return true;
			}
		}
		return false;
	}

	public void onPolylineLoadRequested(float zoom) {
		if (zoom >= getMinimumZoomToSearch()) {
			Logs.ui("Loading " + className() + " for zoom " + zoom + ": true");
			if (searchListener != null) searchListener.onSearchRequested();
		} else {
			Logs.ui("Loading " + className() + " for zoom " + zoom + ": false");
			if (searchListener != null) searchListener.onTooFarToSee();
		}

		updateMapBounds();
	}

	protected abstract float getMinimumZoomToSearch();


	private ITEM itemFromPolyline(Polyline polyline) {
		for (Map.Entry<Long, PolylinedItem<ITEM>> entry : mapItemPolyline.entrySet()) {
			for (Polyline p : entry.getValue()
			                       .getPolylines()) {
				if (Equal.equals(p, polyline)) return entry.getValue()
				                                           .getItem();
			}
		}
		return null;
	}

	protected abstract long getId(ITEM item);
	protected abstract boolean needEdit(ITEM oldItem, ITEM newItem);
	protected abstract List<List<LatLng>> getListOfPolylines(ITEM item);
	protected abstract String className();

	@Nullable
	public LatLngBounds getBounds() {
		LatLngBounds.Builder builder = LatLngBounds.builder();
		for (Map.Entry<Long, PolylinedItem<ITEM>> entry : mapItemPolyline.entrySet()) {
			for (Polyline polyline : entry.getValue()
			                              .getPolylines()) {
				for (LatLng latLng : polyline.getPoints()) {
					builder.include(latLng);
				}
			}
		}
		try {
			return builder.build();
		} catch (IllegalStateException ex) {
			return null;
		}
	}
}
