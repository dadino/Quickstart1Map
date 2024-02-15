package com.dadino.quickstart.map.sample;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.maps.MapsInitializer;

public class SampleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LEGACY, renderer -> {
            switch (renderer) {
                case LATEST -> Log.d("Maps", "The latest version of the renderer is used.");
                case LEGACY -> Log.d("Maps", "The legacy version of the renderer is used.");
            }
        });
    }
}
