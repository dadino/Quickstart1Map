package com.dadino.quickstart.map.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;

import com.dadino.quickstart.core.BaseActivity;

import dev.chrisbanes.insetter.Insetter;

public class NavigationActivity extends BaseActivity implements PermissionRequester {

    private ActivityResultLauncher<String[]> notificationPermissionLauncher;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);

        Insetter.setEdgeToEdgeSystemUiFlags(getWindow().getDecorView(), true);
    }

    @Override
    public void initPresenters() {

    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public ActivityResultLauncher<String[]> getLauncher() {
        return notificationPermissionLauncher;
    }
}