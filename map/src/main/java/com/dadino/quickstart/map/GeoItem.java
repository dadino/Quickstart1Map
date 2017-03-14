package com.dadino.quickstart.map;


public abstract class GeoItem<GEO, ITEM> {

	private final ITEM    item;
	private final GEO     geometry;
	private       float   scale;
	private       boolean managed;

	protected GeoItem(ITEM item, GEO geometry) {
		this.item = item;
		this.geometry = geometry;
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
