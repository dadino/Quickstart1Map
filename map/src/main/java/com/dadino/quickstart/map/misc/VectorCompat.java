package com.dadino.quickstart.map.misc;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;


public class VectorCompat {

	public static Drawable getDrawable(Context context, @DrawableRes int drawableRes) {
		try {
			return ContextCompat.getDrawable(context, drawableRes);
		} catch (Exception e) {
			return VectorDrawableCompat.create(context.getResources(), drawableRes, null);
		}
	}
}
