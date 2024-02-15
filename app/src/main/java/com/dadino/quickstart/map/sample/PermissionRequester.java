package com.dadino.quickstart.map.sample;

import android.app.Activity;
import android.content.Context;

import androidx.activity.result.ActivityResultLauncher;

public interface PermissionRequester {
    Activity getActivity();

    Context getContext();

    ActivityResultLauncher<String[]> getLauncher();
}
