package com.dadino.quickstart.map.sample;

import android.location.Location;

import androidx.annotation.Nullable;

public interface OnLocationFoundCallback {
    void onLocationLoading(boolean loading);

    void onLocationNotFound();

    void onLocationFound(@Nullable Location location);

    void onLocationError(Throwable throwable);
}
