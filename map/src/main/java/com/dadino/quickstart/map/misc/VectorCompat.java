package com.dadino.quickstart.map.misc;


import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.core.content.ContextCompat;


public class VectorCompat {

	public static Drawable getDrawable(Context context, @DrawableRes int drawableRes) {
		try {
			return ContextCompat.getDrawable(context, drawableRes);
		} catch (Exception e) {
			return VectorDrawableCompat.create(context.getResources(), drawableRes, null);
		}
	}
}
