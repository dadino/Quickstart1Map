package com.dadino.quickstart.map.misc;


import com.dadino.quickstart.map.GeoItem;

import java.util.HashMap;

public class GeoItemMap<KEY, GEO, ITEM> extends HashMap<KEY, GeoItem<GEO, ITEM>> {

	@Override
	public GeoItem<GEO, ITEM> put(KEY key, GeoItem<GEO, ITEM> value) {
		value.setManaged(true);
		return super.put(key, value);
	}

	@Override
	public GeoItem<GEO, ITEM> remove(Object key) {
		if (get(key) != null) get(key).setManaged(false);
		return super.remove(key);
	}
}
