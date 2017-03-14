package com.dadino.quickstart.map;


public class GeoItem<GEO, ITEM> {

	private final ITEM    item;
	private final GEO     geometry;
	private       float   scale;
	private       boolean managed;

	public GeoItem(ITEM item, GEO geometry) {
		this.item = item;
		this.geometry = geometry;
	}

	public GeoItem(ITEM item, GEO geometry, float scale) {
		this.item = item;
		this.geometry = geometry;
		this.scale = scale;
	}

	public ITEM getItem() {
		return item;
	}

	public GEO getGeometry() {
		return geometry;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public boolean isManaged() {
		return managed;
	}

	public void setManaged(boolean managed) {
		this.managed = managed;
	}
}
