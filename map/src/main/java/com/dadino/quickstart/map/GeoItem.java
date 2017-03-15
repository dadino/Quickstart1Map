package com.dadino.quickstart.map;


public class GeoItem<GEO, ITEM> {

	private final ITEM    item;
	private final GEO     geometry;
	private       float   scale;
	private       boolean managed;

	public GeoItem(GEO geometry, ITEM item) {
		this.item = item;
		this.geometry = geometry;
	}

	public GeoItem(GEO geometry, ITEM item, float scale) {
		this.item = item;
		this.geometry = geometry;
		this.scale = scale;
	}

	public GeoItem(GeoItem<GEO, ITEM> source, ITEM newItem) {
		this.item = newItem;
		this.geometry = source.getGeometry();
		this.scale = source.getScale();
		this.managed = source.isManaged();
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
