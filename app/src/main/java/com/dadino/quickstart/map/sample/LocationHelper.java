package com.dadino.quickstart.map.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.util.TimeUtils;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

public class LocationHelper {

    public static final long MAX_AGE_IN_MILLIS = 5 * DateUtils.MINUTE_IN_MILLIS;
    public static final long MAX_ACCURACY_IN_METERS = 500L;
    public static final long TIMEOUT_IN_MILLIS = 10 * DateUtils.SECOND_IN_MILLIS;
    public static final long FAST_INTERVAL = 10 * DateUtils.SECOND_IN_MILLIS;

    public static boolean checkPermission(Context context) {
        final String[] missingPermissions = getMissingPermissions(context);
        return missingPermissions.length == 0;
    }

    private static String[] getMissingPermissions(Context context) {
        final boolean coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        final boolean fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (coarseGranted || fineGranted) return new String[]{};
            else
                return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        } else {
            if (fineGranted) return new String[]{};
            else
                return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        }
    }

    public static ActivityResultLauncher<String[]> createContract(Fragment fragment, Callable<Boolean> doOnPermissionGranted) {
        return fragment.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), grantMap -> {
            try {
                if (!grantMap.containsValue(false))
                    doOnPermissionGranted.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void requestPermission(PermissionRequester fragment) {
        final String[] missingPermissions = getMissingPermissions(fragment.getContext());
        if (missingPermissions.length > 0) {
            fragment.getLauncher().launch(missingPermissions);
        }
    }

    private static String[] getNeededPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        else return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    }

    @SuppressLint("MissingPermission")
    public static Disposable getSingleLocation(Context context, OnLocationFoundCallback callback) {
        return getSingleLocation(context, MAX_AGE_IN_MILLIS, MAX_ACCURACY_IN_METERS, TIMEOUT_IN_MILLIS, callback);
    }

    public static Disposable getSingleLocation(Context context, long maxAgeInMillis, long maxAccuracyInMeters, long timeoutInMillis, OnLocationFoundCallback callback) {
        final ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);

        return getLastKnownLocation(locationProvider, maxAgeInMillis, maxAccuracyInMeters)
                .switchIfEmpty(getUpdatedLocation(locationProvider, maxAgeInMillis, maxAccuracyInMeters))
                .take(1)
                .switchIfEmpty(getNullLocation())
                .timeout(timeoutInMillis, TimeUnit.MILLISECONDS)
                .doOnSubscribe(disposable -> callback.onLocationLoading(true))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        callback::onLocationFound,
                        throwable -> {
                            callback.onLocationLoading(false);

                            if (throwable instanceof TimeoutException)
                                callback.onLocationNotFound();
                            else
                                callback.onLocationError(throwable);
                        },
                        () -> callback.onLocationLoading(false));
    }

    @SuppressLint("MissingPermission")
    public static Disposable getMultipleLocations(Context context, OnLocationFoundCallback callback) {
        return getMultipleLocations(context, 3 * DateUtils.MINUTE_IN_MILLIS, callback)

                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(location -> {
                        },
                        throwable -> {
                        },
                        () -> {
                        });
    }

    @SuppressLint("MissingPermission")
    public static Observable<Location> getMultipleLocations(Context context, long interval, OnLocationFoundCallback callback) {
        final ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);

        Log.d("LocationHelper", "Getting multiple locations");
        final LocationRequest.Builder locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval);

        return locationProvider.getUpdatedLocation(locationRequest.build())
                .doOnSubscribe(disposable -> callback.onLocationLoading(true))
                .doOnComplete(() -> callback.onLocationLoading(false))
                .doOnNext(location -> {
                    Log.d("LocationHelper", "Received new location: " + location);

                    callback.onLocationLoading(false);
                    callback.onLocationFound(location);
                })
                .doOnError(throwable -> {
                    callback.onLocationLoading(false);
                    callback.onLocationError(throwable);
                });
    }

    @SuppressLint("MissingPermission")
    public static Observable<Location> getMultipleLocations(Context context, long interval) {
        final ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);

        Log.d("LocationHelper", "Getting multiple locations");
        final LocationRequest.Builder locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval);

        return locationProvider.getUpdatedLocation(locationRequest.build());
    }

    @SuppressLint("MissingPermission")
    private static Observable<Location> getUpdatedLocation(ReactiveLocationProvider locationProvider, long maxAgeInMillis, long maxAccuracyInMeters) {
        return Observable.defer(() -> {
            Log.d("LocationHelper", "Getting updated location");
            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, DateUtils.SECOND_IN_MILLIS).build();

            return locationProvider.getUpdatedLocation(locationRequest)
                    .filter(location -> LocationHelper.isLocationValid(location, maxAgeInMillis, maxAccuracyInMeters));
        });
    }

    @SuppressLint("MissingPermission")
    public static Observable<Location> getLastKnownLocation(Context context) {
        final ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
        return locationProvider.getLastKnownLocation();
    }

    @SuppressLint("MissingPermission")
    private static Observable<Location> getLastKnownLocation(ReactiveLocationProvider locationProvider, long maxAgeInMillis, long maxAccuracyInMeters) {
        return Observable.defer(() -> {
            Log.d("LocationHelper", "Getting last known location");
            return locationProvider.getLastKnownLocation()
                    .filter(location -> LocationHelper.isLocationValid(location, maxAgeInMillis, maxAccuracyInMeters));
        });
    }

    private static Observable<Location> getNullLocation() {
        return Observable.defer(() -> {
            Log.d("LocationHelper", "Passing null location");
            return Observable.just(null);
        });
    }

    @SuppressLint("RestrictedApi")
    private static Boolean isLocationValid(Location location, long maxAgeInMillis, long maxAccuracyInMeters) {
        final long ageInMillis = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / 1000000;
        final boolean tooOld = ageInMillis > (maxAgeInMillis);
        final boolean tooInaccurate = location.getAccuracy() > maxAccuracyInMeters;
        if (BuildConfig.DEBUG) {
            final StringBuilder ageSB = new StringBuilder();
            TimeUtils.formatDuration((ageInMillis), ageSB);
            final String age = ageSB.toString();

            Log.d("LocationHelper", "Checking location: " + location);
            Log.d("LocationHelper", "Is too old? (" + age + ") " + tooOld + "; Is too inaccurate? (" + location.getAccuracy() + " meters)" + tooInaccurate);
        }
        return !tooOld && !tooInaccurate;
    }
}
