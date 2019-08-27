package com.dadino.quickstart.map;

import android.content.Context;
import androidx.annotation.NonNull;

import com.dadino.quickstart.map.listeners.OnGeoExitAnimationFinishedListener;
import com.dadino.quickstart.map.misc.Equal;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Marker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class GeoDrawerTests {

	private List<TestObject> step0 = new ArrayList<>();
	private List<TestObject> step1 = new ArrayList<>();
	private List<TestObject> step2 = new ArrayList<>();
	private List<TestObject> step3 = new ArrayList<>();

	private BaseGeoDrawer<Long, Marker, TestObject> drawer;


	@Before
	public void populateLists() {
		step1.add(new TestObject(1, "a", 1, 1));
		step1.add(new TestObject(2, "a", 1, 1));
		step1.add(new TestObject(3, "a", 1, 1));
		step1.add(new TestObject(4, "a", 1, 1));
		step1.add(new TestObject(5, "a", 1, 1));
		step1.add(new TestObject(6, "a", 1, 1));
		step1.add(new TestObject(7, "a", 1, 1));
		step1.add(new TestObject(8, "a", 1, 1));
		step1.add(new TestObject(9, "a", 1, 1));
		step1.add(new TestObject(10, "a", 1, 1));
		step1.add(new TestObject(11, "a", 1, 1));
		step1.add(new TestObject(12, "a", 1, 1));
		step1.add(new TestObject(13, "a", 1, 1));
		step1.add(new TestObject(14, "a", 1, 1));
		step1.add(new TestObject(15, "a", 1, 1));
		step1.add(new TestObject(16, "a", 1, 1));
		step1.add(new TestObject(17, "a", 1, 1));

		step2.add(new TestObject(1, "a", 2, 1));
		step2.add(new TestObject(2, "a", 2, 1));
		step2.add(new TestObject(3, "a", 2, 1));
		step2.add(new TestObject(4, "a", 1, 1));
		step2.add(new TestObject(5, "a", 1, 1));
		step2.add(new TestObject(6, "a", 1, 1));
		step2.add(new TestObject(7, "a", 1, 1));
		step2.add(new TestObject(8, "a", 1, 1));
		step2.add(new TestObject(9, "a", 1, 1));
		step2.add(new TestObject(10, "a", 1, 1));
		step2.add(new TestObject(11, "a", 1, 1));
		step2.add(new TestObject(12, "a", 1, 1));
		step2.add(new TestObject(13, "a", 1, 1));
		step2.add(new TestObject(14, "a", 1, 1));
		step2.add(new TestObject(16, "a", 1, 1));
		step2.add(new TestObject(17, "a", 1, 1));
		step2.add(new TestObject(18, "a", 1, 1));

		step3.add(new TestObject(1, "a", 2, 1));
		step3.add(new TestObject(2, "a", 2, 1));
		step3.add(new TestObject(3, "a", 2, 1));
		step3.add(new TestObject(4, "b", 1, 3));
		step3.add(new TestObject(5, "a", 1, 1));
		step3.add(new TestObject(6, "b", 1, 1));
		step3.add(new TestObject(7, "a", 1, 1));
		step3.add(new TestObject(8, "a", 1, 1));
		step3.add(new TestObject(9, "a", 1, 1));
	}

	@Before
	public void createDrawer() {
		Context context = Mockito.mock(Context.class);

		BaseMarkerFormatter<TestObject> formatter = new BaseMarkerFormatter<TestObject>(context) {
			@Override
			protected void updateMarker(GeoItem<Marker, TestObject> item) {}

			@NonNull
			@Override
			protected BitmapDescriptor getScaledBitmapDescriptor(GeoItem<Marker, TestObject>
					                                                     object, Float value) {
				return null;
			}

			@Override
			protected void actuallyAnimateGeoExit(GeoItem<Marker, TestObject> geoItem, long delay,
			                                      OnGeoExitAnimationFinishedListener listener) {}

			@Override
			protected void actuallyAnimateGeoEnter(GeoItem<Marker, TestObject> geoItem,
			                                       long delay) {}


			@Override
			public GeoItem<Marker, TestObject> newGeo(TestObject testObject) {
				return new GeoItem<>((Marker) null, testObject);
			}


			@Override
			public void highlightGeo(GeoItem<Marker, TestObject> geoItem) {}

			@Override
			public void unhighlightGeo(GeoItem<Marker, TestObject> geoItem) {}
		};
		drawer = spy(new BaseMarkerDrawer<Long, TestObject>(formatter) {
			@Override
			protected float getMinumumZoomToSearch() {
				return 0;
			}

			@Override
			protected Long getId(TestObject testObject) {
				return testObject.getId();
			}

			@Override
			protected boolean needEdit(TestObject oldItem, TestObject newItem) {
				return oldItem.getLatitude() != newItem.getLatitude() ||
				       oldItem.getLongitude() != newItem.getLongitude() || !Equal.equals(
						oldItem.getTitle(), newItem.getTitle());
			}

			@Override
			protected String itemClassName() {
				return TestObject.class.getSimpleName();
			}

			@Override
			protected void removeGeo(Marker marker) {

			}
		});
	}

	@Test
	public void cicle() {
		doNothing().when(drawer)
		           .log(anyString());

		advanceToStep(step0, 0, 0, 0, 0);
		advanceToStep(step1, 0, 0, 17, 17);
		advanceToStep(step2, 1, 3, 1, 17);
		advanceToStep(step3, 8, 2, 0, 9);
		advanceToStep(step3, 0, 0, 0, 9);
	}

	private void advanceToStep(List<TestObject> step, int expectedRemove, int expectedEdit,
	                           int expectedAdd, int expectedTotal) {

		drawer.setItems(step);
		final int removeAfterStep0 = drawer.removeItemsInternal()
		                                   .size();
		final int editAfterStep0 = drawer.editItemsInternal()
		                                 .size();
		final int addAfterStep0 = drawer.addItemsInternal()
		                                .size();
		assertEquals(expectedRemove, removeAfterStep0);
		assertEquals(expectedEdit, editAfterStep0);
		assertEquals(expectedAdd, addAfterStep0);
		assertEquals(expectedTotal, drawer.getItemMap()
		                                  .size());
		for (Map.Entry<Long, GeoItem<Marker, TestObject>> entry : drawer.getItemMap()
		                                                                .entrySet()) {
			assertTrue(entry.getValue()
			                .isManaged());
		}
	}

	private class TestObject {

		private final long   id;
		private final double latitude;
		private final double longitude;
		private final String title;

		public TestObject(long id, String title, double latitude, double longitude) {
			this.id = id;
			this.latitude = latitude;
			this.longitude = longitude;
			this.title = title;
		}

		public long getId() {
			return id;
		}

		public double getLatitude() {
			return latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public String getTitle() {
			return title;
		}
	}
}
