package com.android.oobe.location;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.android.oobe.R;

/**
 * GpsSetFragment is an Android Fragment responsible for configuring GPS settings within an application.
 * It initializes a GpsSetController to manage GPS functionality and bind it to the provided view.
 * When the fragment stops, it calls setGps() on GpsSetController to apply the GPS settings.
 **/
public class GpsSetFragment extends Fragment {
    private static final String TAG = "GpsSetFragment";
    GpsSetController gpsSetController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_gps_set, container, false);
        final Activity activity = getActivity();
        gpsSetController = new GpsSetController(activity, view);
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        gpsSetController.setGps();
    }
}