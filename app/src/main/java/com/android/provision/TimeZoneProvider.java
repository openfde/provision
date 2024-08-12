package com.android.provision;


import android.app.AlarmManager;
import android.content.Context;
import android.icu.text.Collator;
import android.icu.text.DateFormat;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;

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

public class TimeZoneProvider {
    private String TAG = "TimeZoneProvider";
    private static Context mContext;
    private static Locale mLocale;
    private static Collator mCollator;
    private static TimeZoneData mTimeZoneData;
    //    private String mLocaleRegionZoneId;
    private static LocaleDisplayNames mLocaleDisplayNames;

    private static void init(Context context) {
        mContext = context;
        mLocale = mContext.getResources().getConfiguration().getLocales().get(0);
        mCollator = Collator.getInstance(mLocale);
        CountryZonesFinder countryZonesFinder = TimeZoneFinder.getInstance().getCountryZonesFinder();
        mTimeZoneData = new TimeZoneData(countryZonesFinder);
        mLocaleDisplayNames = LocaleDisplayNames.getInstance(mLocale);
    }

    public static String getRegionId() {
        final Set<String> matchedRegions = mTimeZoneData.lookupCountryCodesForZoneId(getRegionZoneId());
        return matchedRegions.toArray(new String[matchedRegions.size()])[0];
    }

    public static String getRegionZoneId() {
        return TimeZone.getDefault().getID();
    }

    public static void saveTimeZone(Context context, String timeZoneId) {
        init(context);
        if (timeZoneId == null || timeZoneId.isEmpty()) return;
        AlarmManager alarmManager = mContext.getSystemService(AlarmManager.class);
        alarmManager.setTimeZone(timeZoneId);
    }

    public static String getDisplayRegionTxt(Context context) {
        init(context);
        return mLocaleDisplayNames.regionDisplayName(getRegionId());
    }

    public static String getDisplayRegionZoneTxt(Context context) {
        init(context);
        TimeZoneInfo.Formatter formatter = new TimeZoneInfo.Formatter(mLocale, new Date());
        TimeZoneInfo timeZoneInfo = formatter.format(getRegionZoneId());
        return mContext.getString(R.string.region_zone_information, timeZoneInfo.getExemplarLocation(), timeZoneInfo.getGmtOffset());
    }

    public static List<RegionInfo> getRegionInfoList(Context context) {
        init(context);
        final TreeSet<RegionInfo> regionInfoTreeSet = new TreeSet<>(new Comparator<RegionInfo>() {
            @Override
            public int compare(RegionInfo o1, RegionInfo o2) {
                return mCollator.compare(o1.getName(), o2.getName());
            }
        });
        Set<String> regionIds = mTimeZoneData.getRegionIds();
        for (String regionId : regionIds) {
            String regionDisplayName = mLocaleDisplayNames.regionDisplayName(regionId);
            regionInfoTreeSet.add(new RegionInfo(regionId, regionDisplayName));
        }
        return new ArrayList<>(regionInfoTreeSet);
    }

/*
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
* */

    public static List<RegionZoneInfo> getRegionZoneInfoList(Context context) {
        init(context);
        return getRegionZoneInfoList(context, getRegionId());
    }

    public static List<RegionZoneInfo> getRegionZoneInfoList(Context context, String regionId) {
        init(context);
        List<RegionZoneInfo> regionZoneInfoList = new ArrayList<>();
        List<String> regionZoneIdList = getRegionZoneIdListByRegionId(regionId);
        List<TimeZoneInfo> timeZoneInfoList = getRegionTimeZoneInfo(regionZoneIdList);
        for (TimeZoneInfo timeZoneInfo : timeZoneInfoList) {
            String id = timeZoneInfo.getId();
            String title = getRegionZoneTitle(timeZoneInfo);
            String summary = getRegionZoneSummary(timeZoneInfo, title);
            String currentTime = getRegionZoneCurrentTime(timeZoneInfo.getTimeZone());
            regionZoneInfoList.add(new RegionZoneInfo(id, title, summary, currentTime));
        }
        return regionZoneInfoList;
    }

    private static List<String> getRegionZoneIdListByRegionId(String regionId) {
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
    private static List<TimeZoneInfo> getRegionTimeZoneInfo(Collection<String> timeZoneIds) {
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

    private static String getRegionZoneTitle(TimeZoneInfo timeZoneInfo) {
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

    private static String getRegionZoneSummary(TimeZoneInfo timeZoneInfo, String title) {
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

    private static String getRegionZoneCurrentTime(TimeZone timeZone) {
        String currentTime = null;
        // Get the regionalized time format by calling the hide method through reflection.
        try {
//            Log.w(TAG, "try catch");
            Class<?> dateFormatClass = android.text.format.DateFormat.class;
            Method getTimeFormatStringMethod = dateFormatClass.getDeclaredMethod("getTimeFormatString", Context.class);
            getTimeFormatStringMethod.setAccessible(true);
            String timeFormatString = (String) getTimeFormatStringMethod.invoke(null, mContext);
            DateFormat currentTimeFormat = new SimpleDateFormat(timeFormatString, mLocale);
            currentTime = currentTimeFormat.format(Calendar.getInstance(timeZone));
//            Log.w(TAG, "currentTime = " + currentTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTime;
    }


//    private static ArrayList<BaseTimeZoneAdapter.BaseTimeZoneItem> getRegionList() {
//        final TreeSet<BaseTimeZoneAdapter.BaseTimeZoneItem> regionItemSet = new TreeSet<>(new Comparator<BaseTimeZoneAdapter.BaseTimeZoneItem>() {
//            @Override
//            public int compare(BaseTimeZoneAdapter.BaseTimeZoneItem o1, BaseTimeZoneAdapter.BaseTimeZoneItem o2) {
//                return mCollator.compare(o1.getTitle(), o2.getTitle());
//            }
//        });
//        for (String regionId : mRegionIds) {
//            String regionDisplayName = mLocaleDisplayNames.regionDisplayName(regionId);
//            regionItemSet.add(new BaseTimeZoneAdapter.BaseTimeZoneItem(regionId, regionDisplayName, (state, bundle) -> setView(state, bundle)));
//        }
//        return new ArrayList<>(regionItemSet);
//    }
//
//    private static ArrayList<BaseTimeZoneAdapter.BaseTimeZoneItem> getRegionZoneList(String regionId) {
//        Log.w(TAG, "regionId = " + regionId);
//        ArrayList<BaseTimeZoneAdapter.BaseTimeZoneItem> regionZoneList = new ArrayList<>();
//        List<String> timeZoneIds = getRegionTimeZoneListByRegionId(regionId);
//        List<TimeZoneInfo> regionTimeZoneInfo = getRegionTimeZoneInfo(timeZoneIds);
//        for (TimeZoneInfo timeZoneInfo : regionTimeZoneInfo) {
//            String title = getRegionZoneTitle(timeZoneInfo);
//            String summary = getRegionZoneSummary(timeZoneInfo, title);
//            String currentTime = getRegionZoneCurrentTime(timeZoneInfo.getTimeZone());
//            regionZoneList.add(new BaseTimeZoneAdapter.BaseTimeZoneItem(timeZoneInfo.getId(), title, summary, currentTime, true, (state, bundle) -> setView(state, bundle)));
//        }
////            Log.w(TAG, "timeZoneId = " + timeZoneId);
////            Log.w(TAG, "GenericName = " + (timeZoneNames.getDisplayName(canonicalZoneId,
////                    TimeZoneNames.NameType.LONG_GENERIC, now.getTime())));
////            Log.w(TAG, "StandardName = " + timeZoneNames.getDisplayName(canonicalZoneId,
////                    TimeZoneNames.NameType.LONG_STANDARD, now.getTime()));
////            Log.w(TAG, "DaylightName = " + timeZoneNames.getDisplayName(canonicalZoneId,
////                    TimeZoneNames.NameType.LONG_DAYLIGHT, now.getTime()));
////            Log.w(TAG, "ExemplarLocation = " + timeZoneNames.getExemplarLocationName(canonicalZoneId));
////            Log.w(TAG, "GmtOffset = " + gmtOffset);
////            regionZoneList.add(new BaseTimeZoneAdapter.BaseTimeZoneItem(regionId, exemplarLocationName, null, null, (state, result) -> setView(state, result)));
//        return regionZoneList;
//    }
//
//    private String getRegionZoneTitle(TimeZoneInfo timeZoneInfo) {
//        String name = timeZoneInfo.getExemplarLocation();
//        if (name == null) {
//            name = timeZoneInfo.getGenericName();
//        }
//        if (name == null && timeZoneInfo.getTimeZone().inDaylightTime(new Date())) {
//            name = timeZoneInfo.getDaylightName();
//        }
//        if (name == null) {
//            name = timeZoneInfo.getStandardName();
//        }
//        if (name == null) {
//            name = String.valueOf(timeZoneInfo.getGmtOffset());
//        }
//        return name;
//    }
//
//    private String getRegionZoneSummary(TimeZoneInfo timeZoneInfo, String title) {
//        String name = timeZoneInfo.getGenericName();
//        if (name == null) {
//            if (timeZoneInfo.getTimeZone().inDaylightTime(new Date())) {
//                name = timeZoneInfo.getDaylightName();
//            } else {
//                name = timeZoneInfo.getStandardName();
//            }
//        }
//        // Ignore name / GMT offset if the title shows the same information
//        if (name == null || name.equals(title)) {
//            CharSequence gmtOffset = timeZoneInfo.getGmtOffset();
//            return gmtOffset == null || gmtOffset.toString().equals(title) ? "" : gmtOffset.toString();
//        } else {
//            return mContext.getString(R.string.region_zone_information, name, timeZoneInfo.getGmtOffset());
//        }
//    }
//
//    private String getRegionZoneCurrentTime(TimeZone timeZone) {
//        String currentTime = null;
//        // Get the regionalized time format by calling the hide method through reflection.
//        try {
//            Log.w(TAG, "try catch");
//            Class<?> dateFormatClass = android.text.format.DateFormat.class;
//            Method getTimeFormatStringMethod = dateFormatClass.getDeclaredMethod("getTimeFormatString", Context.class);
//            getTimeFormatStringMethod.setAccessible(true);
//            String timeFormatString = (String) getTimeFormatStringMethod.invoke(null, mContext);
//            DateFormat currentTimeFormat = new SimpleDateFormat(timeFormatString, mLocale);
//            currentTime = currentTimeFormat.format(Calendar.getInstance(timeZone));
//            Log.w(TAG, "currentTime = " + currentTime);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return currentTime;
//    }
//
//    //Sort RegionZones
//    static class TimeZoneInfoComparator implements Comparator<TimeZoneInfo> {
//        private final Collator mCollator;
//        private final Date mNow;
//
//        TimeZoneInfoComparator(Collator collator, Date now) {
//            mCollator = collator;
//            mNow = now;
//        }
//
//        @Override
//        public int compare(TimeZoneInfo tzi1, TimeZoneInfo tzi2) {
//            int result = Integer.compare(tzi1.getTimeZone().getOffset(mNow.getTime()), tzi2.getTimeZone().getOffset(mNow.getTime()));
//            if (result == 0) {
//                result = Integer.compare(tzi1.getTimeZone().getRawOffset(), tzi2.getTimeZone().getRawOffset());
//            }
//            if (result == 0) {
//                result = mCollator.compare(tzi1.getExemplarLocation(), tzi2.getExemplarLocation());
//            }
//            if (result == 0 && tzi1.getGenericName() != null && tzi2.getGenericName() != null) {
//                result = mCollator.compare(tzi1.getGenericName(), tzi2.getGenericName());
//            }
//            return result;
//        }
//    }
//
//    private String getCanonicalZoneId(TimeZone timeZone) {
//        final String id = timeZone.getID();
//        final String canonicalId = TimeZone.getCanonicalID(id);
//        if (canonicalId != null) {
//            return canonicalId;
//        }
//        return id;
//    }

}

class RegionInfo {
    private String id;
    private String name;

    public RegionInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

class RegionZoneInfo {
    private String id;
    private String name;
    private String summary;
    private String currentTime;

    public RegionZoneInfo(String id, String name, String summary, String currentTime) {
        this.id = id;
        this.name = name;
        this.summary = summary;
        this.currentTime = currentTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }
}

class TimeZoneInfoComparator implements Comparator<TimeZoneInfo> {
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