package com.android.oobe.location;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.android.oobe.R;

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