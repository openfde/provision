package com.android.provision;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.Collator;
import android.icu.text.DateFormat;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import libcore.timezone.CountryZonesFinder;
import libcore.timezone.TimeZoneFinder;

public class TimeZoneFragment extends Fragment {
    public static final int STATE_UNKNOWN = -1;
    public static final int STATE_TIME_ZONE = 0;
    public static final int STATE_REGION = 1;
    public static final int STATE_REGION_ZONE = 2;
    public static final int STATE_SUCCESS = 3;
    public static final String REGION_ID = "region_id";
    public static final String REGION_ZONE_ID = "region_zone_id";
    public static final String REGION_ZONE_SIZE = "region_zone_size";
    private static final String TAG = "TimeZoneFragment";
    private final int PADDING = 94;
    private Context mContext;
    private Locale mLocale;
    private Collator mCollator;
    private TimeZoneData mTimeZoneData;
    private RecyclerView mRecyclerView;
    private int mState = -1;
    Set<String> mRegionIds;
    LocaleDisplayNames mLocaleDisplayNames;
    private String mLocaleRegionId, mLocaleRegionZoneId;
    //The three lists represent the time zone home page, the region (country) list, and the region within country list.
    //Three recyclerViews reuse the same layout.
    private LanguageActivity.LanguageListener languageListener;
    private BaseTimeZoneAdapter mBaseTimeZoneAdapter;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.w(TAG, "onReceive");
            if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                setView(STATE_TIME_ZONE, null);
                Log.w(TAG, "onReceive");
            }
        }
    };

    public TimeZoneFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public TimeZoneFragment(LanguageActivity.LanguageListener languageListener) {
        this.languageListener = languageListener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_time_zone, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        init();
//        mRecyclerView.setAdapter(new BaseTimeZoneAdapter(getContext(), mState, getTimeZoneList()));
        setView(STATE_TIME_ZONE, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFragmentPadding(0,0,0, PADDING);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        languageListener.showAndHideReturnButton(View.GONE);
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private void init() {
        mContext = getContext();
        mCollator = Collator.getInstance(mLocale);
        mLocale = this.getResources().getConfiguration().getLocales().get(0);
        //Get the ISO codes for all countries first
        CountryZonesFinder countryZonesFinder = TimeZoneFinder.getInstance().getCountryZonesFinder();
        List<String> iosCodes = countryZonesFinder.lookupAllCountryIsoCodes();
        //Get the RegionId for all countries again.
        mTimeZoneData = new TimeZoneData(countryZonesFinder);
        mRegionIds = mTimeZoneData.getRegionIds();
        //Then to the name of the country in locale language
        mLocaleDisplayNames = LocaleDisplayNames.getInstance(mLocale);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getActivity().registerReceiver(mBroadcastReceiver, filter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mBaseTimeZoneAdapter = new BaseTimeZoneAdapter(mContext, null);
        mRecyclerView.setAdapter(mBaseTimeZoneAdapter);

//        mLocale = this.getResources().getConfiguration().getLocales().get(0);
//        mSelectedRegionZoneId = TimeZone.getDefault().getID();
//        final Set<String> matchedRegions = timeZoneData.lookupCountryCodesForZoneId(mSelectedRegionZoneId);
//        String regionId = matchedRegions.toArray(new String[matchedRegions.size()])[0];
//        Log.w(TAG, "regiondId = " + regionId);
//        String displayName = localeDisplayNames.regionDisplayName(regionId);
//        Log.w(TAG, "displayName = " + displayName);

//        mLocale = this.getResources().getConfiguration().locale;
//        CountryZonesFinder countryZonesFinder = TimeZoneFinder.getInstance().getCountryZonesFinder();
//        List<String> strings = countryZonesFinder.lookupAllCountryIsoCodes();
    }


    // Although it is a list of time zones, there are actually only two items, the sub-table represents Region and RegionZone Item
    private ArrayList<BaseTimeZoneAdapter.BaseTimeZoneItem> getTimeZoneList() {
        ArrayList<BaseTimeZoneAdapter.BaseTimeZoneItem> timeZoneItemArrayList = new ArrayList<>();
        mLocaleRegionZoneId = TimeZone.getDefault().getID();
        final Set<String> matchedRegions = mTimeZoneData.lookupCountryCodesForZoneId(mLocaleRegionZoneId);
        mLocaleRegionId = matchedRegions.toArray(new String[matchedRegions.size()])[0];
        String displaySelectedReginoName = mLocaleDisplayNames.regionDisplayName(mLocaleRegionId);
//        Log.w(TAG, "mSelectedRegionId = " + mLocaleRegionId);
//        Log.w(TAG, "displayReginoName = " + displaySelectedReginoName);
        timeZoneItemArrayList.add(new BaseTimeZoneAdapter.BaseTimeZoneItem(getString(R.string.region), displaySelectedReginoName, true, (state, result) -> setView(state, result)));
        TimeZoneInfo.Formatter mTimeZoneInfoFormatter = new TimeZoneInfo.Formatter(mLocale, new Date());
        TimeZoneInfo timeZoneInfo = mTimeZoneInfoFormatter.format(mLocaleRegionZoneId);
        CharSequence gmtOffset = timeZoneInfo.getGmtOffset();
        String exemplarLocation = timeZoneInfo.getExemplarLocation();
        timeZoneItemArrayList.add(new BaseTimeZoneAdapter.BaseTimeZoneItem(mContext.getString(R.string.region_zone), mContext.getString(R.string.region_zone_information, exemplarLocation, gmtOffset), getRegionTimeZoneListByRegionId(mLocaleRegionId).size() != 1, (state, result) -> setView(state, result)));
//        String genericName = timeZoneInfo.getGenericName();
//        Log.w(TAG, "genericName = " + genericName);
//        Log.w(TAG, "gmtOffset = " + gmtOffset);
//        Log.w(TAG, "daylightName = " + timeZoneInfo.getDaylightName());
//        Log.w(TAG, "standardName = " + timeZoneInfo.getStandardName());
//        Log.w(TAG, "exemplarLocation = " + timeZoneInfo.getExemplarLocation());
//        String location = "New York";
//        String offset = "-05:00";
//        String formattedString = getString(R.string.zone_info_exemplar_location_and_offset, location, offset);
//        Log.w(TAG, "formattedString = " + formattedString);
//        timeZoneItemArrayList.add(new BaseTimeZoneAdapter.TimeZoneItem(mContext.getString(R.string.time_zone), , false));
        return timeZoneItemArrayList;
    }

    private ArrayList<BaseTimeZoneAdapter.BaseTimeZoneItem> getRegionList() {
        final TreeSet<BaseTimeZoneAdapter.BaseTimeZoneItem> regionItemSet = new TreeSet<>(new Comparator<BaseTimeZoneAdapter.BaseTimeZoneItem>() {
            @Override
            public int compare(BaseTimeZoneAdapter.BaseTimeZoneItem o1, BaseTimeZoneAdapter.BaseTimeZoneItem o2) {
                return mCollator.compare(o1.getTitle(), o2.getTitle());
            }
        });
        for (String regionId : mRegionIds) {
            String regionDisplayName = mLocaleDisplayNames.regionDisplayName(regionId);
            regionItemSet.add(new BaseTimeZoneAdapter.BaseTimeZoneItem(regionId, regionDisplayName, (state, bundle) -> setView(state, bundle)));
        }
        return new ArrayList<>(regionItemSet);
    }

    private ArrayList<BaseTimeZoneAdapter.BaseTimeZoneItem> getRegionZoneList(String regionId) {
        Log.w(TAG, "regionId = " + regionId);
        ArrayList<BaseTimeZoneAdapter.BaseTimeZoneItem> regionZoneList = new ArrayList<>();
        List<String> timeZoneIds = getRegionTimeZoneListByRegionId(regionId);
        List<TimeZoneInfo> regionTimeZoneInfo = getRegionTimeZoneInfo(timeZoneIds);
        for (TimeZoneInfo timeZoneInfo : regionTimeZoneInfo) {
            String title = getRegionZoneTitle(timeZoneInfo);
            String summary = getRegionZoneSummary(timeZoneInfo, title);
            String currentTime = getRegionZoneCurrentTime(timeZoneInfo.getTimeZone());
            regionZoneList.add(new BaseTimeZoneAdapter.BaseTimeZoneItem(timeZoneInfo.getId(), title, summary, currentTime, true, (state, bundle) -> setView(state, bundle)));
        }
//            Log.w(TAG, "timeZoneId = " + timeZoneId);
//            Log.w(TAG, "GenericName = " + (timeZoneNames.getDisplayName(canonicalZoneId,
//                    TimeZoneNames.NameType.LONG_GENERIC, now.getTime())));
//            Log.w(TAG, "StandardName = " + timeZoneNames.getDisplayName(canonicalZoneId,
//                    TimeZoneNames.NameType.LONG_STANDARD, now.getTime()));
//            Log.w(TAG, "DaylightName = " + timeZoneNames.getDisplayName(canonicalZoneId,
//                    TimeZoneNames.NameType.LONG_DAYLIGHT, now.getTime()));
//            Log.w(TAG, "ExemplarLocation = " + timeZoneNames.getExemplarLocationName(canonicalZoneId));
//            Log.w(TAG, "GmtOffset = " + gmtOffset);
//            regionZoneList.add(new BaseTimeZoneAdapter.BaseTimeZoneItem(regionId, exemplarLocationName, null, null, (state, result) -> setView(state, result)));
        return regionZoneList;
    }

    private void setView(int state, String result) {
        mState = state;
        Log.w(TAG, "mState = " + mState);
        mBaseTimeZoneAdapter.setState(state);
        ArrayList<BaseTimeZoneAdapter.BaseTimeZoneItem> baseTimeZoneItemArrayList = null;
        if (mState == STATE_TIME_ZONE) {
            if (result != null) saveTimeZone(result);
            baseTimeZoneItemArrayList = getTimeZoneList();
        } else if (mState == STATE_REGION) {
            baseTimeZoneItemArrayList = getRegionList();
        } else if (mState == STATE_REGION_ZONE) {
            baseTimeZoneItemArrayList = getRegionZoneList(result == null ? mLocaleRegionId : result);
            if (baseTimeZoneItemArrayList.size() == 1) {
                saveTimeZone(baseTimeZoneItemArrayList.get(0).getId());
                setView(STATE_TIME_ZONE, null);
                return;
            }
        }
        mBaseTimeZoneAdapter.setItems(baseTimeZoneItemArrayList);
        mBaseTimeZoneAdapter.notifyDataSetChanged();
    }

    private void saveTimeZone(String timeZoneId) {
        if (timeZoneId == null || timeZoneId.isEmpty()) return;
        AlarmManager alarmManager = mContext.getSystemService(AlarmManager.class);
        alarmManager.setTimeZone(timeZoneId);
    }

    private void setFragmentPadding(int left, int top, int right, int bottom) {
        float density = getResources().getDisplayMetrics().density;
        // in pixels
        int paddingLeft = Math.round(left * density);
        int paddingTop = Math.round(top * density);
        int paddingRight = Math.round(right * density);
        int paddingBottom = Math.round(bottom * density);
        getView().setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    private List<String> getRegionTimeZoneListByRegionId(String regionId) {
        FilteredCountryTimeZones filteredCountryTimeZones = mTimeZoneData.lookupCountryTimeZones(regionId);
        return filteredCountryTimeZones.getTimeZoneIds();
    }

    /**
     * Returns a list of {@link TimeZoneInfo} objects. The returned list will be sorted properly for
     * display in the locale.It may be smaller than the input collection, if equivalent IDs are
     * passed in.
     *
     * @param timeZoneIds a list of Olson IDs.
     */
    private List<TimeZoneInfo> getRegionTimeZoneInfo(Collection<String> timeZoneIds) {
        final TimeZoneInfo.Formatter formatter = new TimeZoneInfo.Formatter(mLocale, new Date());
        final TreeSet<TimeZoneInfo> timeZoneInfos = new TreeSet<>(new TimeZoneInfoComparator(Collator.getInstance(mLocale), new Date()));
        for (final String timeZoneId : timeZoneIds) {
            final TimeZone timeZone = TimeZone.getFrozenTimeZone(timeZoneId);
            // Skip time zone ICU isn't aware.
            if (timeZone.getID().equals(TimeZone.UNKNOWN_ZONE_ID)) {
                continue;
            }
            timeZoneInfos.add(formatter.format(timeZone));
        }
        return Collections.unmodifiableList(new ArrayList<>(timeZoneInfos));
    }

    private String getRegionZoneTitle(TimeZoneInfo timeZoneInfo) {
        String name = timeZoneInfo.getExemplarLocation();
        if (name == null) {
            name = timeZoneInfo.getGenericName();
        }
        if (name == null && timeZoneInfo.getTimeZone().inDaylightTime(new Date())) {
            name = timeZoneInfo.getDaylightName();
        }
        if (name == null) {
            name = timeZoneInfo.getStandardName();
        }
        if (name == null) {
            name = String.valueOf(timeZoneInfo.getGmtOffset());
        }
        return name;
    }

    private String getRegionZoneSummary(TimeZoneInfo timeZoneInfo, String title) {
        String name = timeZoneInfo.getGenericName();
        if (name == null) {
            if (timeZoneInfo.getTimeZone().inDaylightTime(new Date())) {
                name = timeZoneInfo.getDaylightName();
            } else {
                name = timeZoneInfo.getStandardName();
            }
        }
        // Ignore name / GMT offset if the title shows the same information
        if (name == null || name.equals(title)) {
            CharSequence gmtOffset = timeZoneInfo.getGmtOffset();
            return gmtOffset == null || gmtOffset.toString().equals(title) ? "" : gmtOffset.toString();
        } else {
            return mContext.getString(R.string.region_zone_information, name, timeZoneInfo.getGmtOffset());
        }
    }

    private String getRegionZoneCurrentTime(TimeZone timeZone) {
        String currentTime = null;
        // Get the regionalized time format by calling the hide method through reflection.
        try {
            Log.w(TAG, "try catch");
            Class<?> dateFormatClass = android.text.format.DateFormat.class;
            Method getTimeFormatStringMethod = dateFormatClass.getDeclaredMethod("getTimeFormatString", Context.class);
            getTimeFormatStringMethod.setAccessible(true);
            String timeFormatString = (String) getTimeFormatStringMethod.invoke(null, mContext);
            DateFormat currentTimeFormat = new SimpleDateFormat(timeFormatString, mLocale);
            currentTime = currentTimeFormat.format(Calendar.getInstance(timeZone));
            Log.w(TAG, "currentTime = " + currentTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTime;
    }

    private String getCanonicalZoneId(TimeZone timeZone) {
        final String id = timeZone.getID();
        final String canonicalId = TimeZone.getCanonicalID(id);
        if (canonicalId != null) {
            return canonicalId;
        }
        return id;
    }

    //Sort RegionZones
    static class TimeZoneInfoComparator implements Comparator<TimeZoneInfo> {
        private final Collator mCollator;
        private final Date mNow;

        TimeZoneInfoComparator(Collator collator, Date now) {
            mCollator = collator;
            mNow = now;
        }

        @Override
        public int compare(TimeZoneInfo tzi1, TimeZoneInfo tzi2) {
            int result = Integer.compare(tzi1.getTimeZone().getOffset(mNow.getTime()), tzi2.getTimeZone().getOffset(mNow.getTime()));
            if (result == 0) {
                result = Integer.compare(tzi1.getTimeZone().getRawOffset(), tzi2.getTimeZone().getRawOffset());
            }
            if (result == 0) {
                result = mCollator.compare(tzi1.getExemplarLocation(), tzi2.getExemplarLocation());
            }
            if (result == 0 && tzi1.getGenericName() != null && tzi2.getGenericName() != null) {
                result = mCollator.compare(tzi1.getGenericName(), tzi2.getGenericName());
            }
            return result;
        }
    }

}