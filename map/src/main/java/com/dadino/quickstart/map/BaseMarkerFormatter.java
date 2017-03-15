package com.dadino.quickstart.map;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Property;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Marker;

public abstract class BaseMarkerFormatter<ITEM> extends BaseGeoFormatter<Marker, ITEM> {

	protected Property<Marker, Float>                markerAlphaProperty =
			new Property<Marker, Float>(Float.class, "alpha") {
				@Override
				public void set(Marker object, Float value) {
					object.setAlpha(value);
				}

				@Override
				public Float get(Marker object) {
					return object.getAlpha();
				}
			};
	protected Property<GeoItem<Marker, ITEM>, Float> markerScaleProperty =
			new Property<GeoItem<Marker, ITEM>, Float>(Float.class, "scale") {
				@Override
				public void set(GeoItem<Marker, ITEM> object, Float value) {
					try {
						if (object != null && object.getGeometry() != null) {
							object.setScale(value);
							object.getGeometry()
							      .setIcon(getScaledBitmapDescriptor(object, value));
						}
					} catch (Exception ignored) {
					}
				}

				@Override
				public Float get(GeoItem<Marker, ITEM> object) {
					return object.getScale();
				}
			};

	public BaseMarkerFormatter(Context context) {
		super(context);
	}


	@Override
	public GeoItem<Marker, ITEM> editGeo(GeoItem<Marker, ITEM> oldGeoItem, ITEM newItem) {
		GeoItem<Marker, ITEM> newGeoItem = new GeoItem<>(oldGeoItem, newItem);

		updateMarker(newGeoItem);

		return newGeoItem;
	}

	protected abstract void updateMarker(GeoItem<Marker, ITEM> geoItem);

	@NonNull
	protected abstract BitmapDescriptor getScaledBitmapDescriptor(GeoItem<Marker, ITEM> object,
	                                                              Float value);
}
