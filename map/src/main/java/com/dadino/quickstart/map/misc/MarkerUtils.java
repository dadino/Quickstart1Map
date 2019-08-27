package com.dadino.quickstart.map.misc;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import android.util.Property;

import com.dadino.quickstart.map.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


public class MarkerUtils {

	public static void animateMarkerTo(Marker marker, LatLng finalPosition,
	                                   final LatLngInterpolator latLngInterpolator) {
		TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
			@Override
			public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
				return latLngInterpolator.interpolate(fraction, startValue, endValue);
			}
		};
		Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
		ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator,
				finalPosition);
		animator.setDuration(2000);
		animator.start();
	}

	public static BitmapDescriptor getMarkerIconFromDrawable(Context context,
	                                                         @DrawableRes int drawableId,
	                                                         @ColorRes int colorId) {
		final Drawable drawable = VectorCompat.getDrawable(context, drawableId);
		return getMarkerIconFromDrawable(context, drawable, colorId);
	}

	public static BitmapDescriptor getMarkerIconFromDrawable(Context context, Drawable drawable,
	                                                         @ColorRes int colorId) {
		if (colorId != 0) drawable.setColorFilter(ContextCompat.getColor(context, colorId),
				PorterDuff.Mode.SRC_IN);
		final int size = context.getResources()
		                        .getDimensionPixelSize(R.dimen._20dp);
		Canvas canvas = new Canvas();
		Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
		canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, size, size);
		drawable.draw(canvas);
		return BitmapDescriptorFactory.fromBitmap(bitmap);
	}
}
