package com.android.provision;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Layout;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settingslib.datetime.ZoneGetter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.TreeSet;

public class TimeFragment extends Fragment {

    private static final String HOURS_24 = "24";
    private static final String HOURS_12 = "12";
    private static final String TAG = "TimeFragment";
    private LanguageActivity.LanguageListener languageListener;
    private Context mContext;
    private LinearLayout timeZone;
    private LinearLayout timeFormat;
    private Switch is24HourSwitch;
    private TextView currentTime;
    private TextView currentTimeZone;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_TICK)) {
                currentTime.setText(getCurrentTime());
                Log.w(TAG, "onReceive");
            }
        }
    };

    public TimeFragment() {
    }

    @SuppressLint("ValidFragment")
    public TimeFragment(LanguageActivity.LanguageListener languageListener) {
        this.languageListener = languageListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_time, null);
        mContext = getContext();
        DateFormat.is24HourFormat(mContext);
        timeZone = view.findViewById(R.id.time_zone);
        timeFormat = view.findViewById(R.id.time_format);
        is24HourSwitch = view.findViewById(R.id.is24HourSwitch);
        currentTime = view.findViewById(R.id.current_time);
        currentTimeZone = view.findViewById(R.id.current_time_zone);
        currentTime.setText(getCurrentTime());
        set24Hour(getContext(), is24HourSwitch.isChecked());
        currentTimeZone.setText(getTimeZoneOffsetAndName());
        is24HourSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean is24Hour) {
                set24Hour(getContext(), is24Hour);
                currentTime.setText(getCurrentTime());
            }
        });
        TreeSet<BaseTimeZoneAdapter.BaseTimeZoneItem> baseTimeZoneItems = new TreeSet<>();
        timeFormat.setOnClickListener(v -> is24HourSwitch.setChecked(!is24HourSwitch.isChecked()));
        timeZone.setOnClickListener(v -> languageListener.setTimeZone());


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        getContext().registerReceiver(mBroadcastReceiver, intentFilter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unregisterReceiver(mBroadcastReceiver);
    }

    static void set24Hour(Context context, Boolean is24Hour) {
        String value = is24Hour == null ? null :
                is24Hour ? HOURS_24 : HOURS_12;
        Settings.System.putString(context.getContentResolver(),
                Settings.System.TIME_12_24, value);
    }

    private String getCurrentTime() {
        final Calendar now = Calendar.getInstance();
        String currentTime = DateFormat.getTimeFormat(mContext).format(now.getTime());
        Log.w(TAG, "currentTime = " + currentTime);
        return currentTime;
    }

    private CharSequence getTimeZoneOffsetAndName() {
        final Calendar now = Calendar.getInstance();
        CharSequence timeZoneOffsetAndName = ZoneGetter.getTimeZoneOffsetAndName(mContext,
                now.getTimeZone(), now.getTime());
//        Log.w(TAG, "timeZoneOffsetAndName = " + timeZoneOffsetAndName);
        return timeZoneOffsetAndName;
    }

}
