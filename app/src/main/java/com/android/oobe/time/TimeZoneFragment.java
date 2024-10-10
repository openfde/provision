package com.android.oobe.time;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.oobe.LanguageActivity;
import com.android.oobe.R;

public class TimeZoneFragment extends Fragment {
    private LanguageActivity.LanguageListener mLanguageListener;
    private LinearLayout region;
    private LinearLayout regionZone;
    private TextView regionNameTxt;
    private TextView regionZoneTxt;
    private TextView regionZoneNameTxt;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                setView();
            }
        }
    };

    public TimeZoneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        IntentFilter timeZoneIntentFilter = new IntentFilter();
        timeZoneIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(mBroadcastReceiver, timeZoneIntentFilter);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_time_zone, container, false);

        region = view.findViewById(R.id.region);
        regionZone = view.findViewById(R.id.regionZone);
        regionNameTxt = view.findViewById(R.id.regionNameTxt);
        regionZoneTxt = view.findViewById(R.id.regionZoneTxt);
        regionZoneNameTxt = view.findViewById(R.id.regionZoneNameTxt);

        setView();

        region.setOnClickListener(v -> mLanguageListener.addRegion());
        if (TimeZoneProvider.getRegionZoneInfoList(getContext()).size() == 1) {
            regionZoneTxt.setTextColor(getContext().getResources().getColor(R.color.connected_state_color));
            regionZoneNameTxt.setTextColor(getContext().getResources().getColor(R.color.connected_state_color));
        } else {
            regionZone.setOnClickListener(v -> mLanguageListener.addRegionZone(TimeZoneProvider.getRegionId(getContext())));
        }

        return view;
    }

    public void setLanguageListener(LanguageActivity.LanguageListener languageListener) {
        this.mLanguageListener = languageListener;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private void setView() {
        regionNameTxt.setText(TimeZoneProvider.getDisplayRegionTxt(getContext()));
        regionZoneNameTxt.setText(TimeZoneProvider.getDisplayRegionZoneTxt(getContext()));
    }

}