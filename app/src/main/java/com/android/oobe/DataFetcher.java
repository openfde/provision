package com.android.oobe;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.android.oobe.time.TimeZoneProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataFetcher {

    public static boolean isAvailable(Context context) {
        return isGpsAvailable(context) && isTimeZoneAvailable(context);
    }

    private static Boolean isGpsAvailable(Context context) {
        final String REGION_URI = "content://com.boringdroid.systemuiprovider.region";
        Cursor cursor = null;
        Map<String, Object> result = null;
        String selection = null;
        String[] selectionArgs = null;
        List<String> list = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(Uri.parse(REGION_URI + "/REGION_COUNTRY"), null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    if (Utils.isChineseLanguage(context)) {
                        String COUNTRY_NAME = cursor.getString(cursor.getColumnIndex("COUNTRY_NAME"));
                        list.add(COUNTRY_NAME);
                    } else {
                        String COUNTRY_NAME_EN = cursor.getString(cursor.getColumnIndex("COUNTRY_NAME_EN"));
                        list.add(COUNTRY_NAME_EN);
                    }
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (list == null || list.isEmpty()) return false;


        String countryName = list.get(0);
        selection = "COUNTRY_NAME = ?";
        if (!Utils.isChineseLanguage(context)) {
            selection = "COUNTRY_NAME_EN = ?";
        }
        selectionArgs = new String[]{countryName};
        list = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(Uri.parse(REGION_URI + "/REGION_PROVINCE"), null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    if (Utils.isChineseLanguage(context)) {
                        String PROVINCE_NAME = cursor.getString(cursor.getColumnIndex("PROVINCE_NAME"));
                        list.add(PROVINCE_NAME);
                    } else {
                        String PROVINCE_NAME_EN = cursor.getString(cursor.getColumnIndex("PROVINCE_NAME_EN"));
                        list.add(PROVINCE_NAME_EN);
                    }
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (list == null || list.isEmpty()) return false;


        String province = list.get(0);
        cursor = null;
        result = null;
        selection = "PROVINCE_NAME = ?";
        if (!Utils.isChineseLanguage(context)) {
            selection = "PROVINCE_NAME_EN = ?";
        }
        selectionArgs = new String[]{province};
        list = null;
        List<String> listCityGps = new ArrayList<>();
        String gpsValue = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(Uri.parse(REGION_URI + "/REGION_INFO"), null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    String CITY_ID = cursor.getString(cursor.getColumnIndex("CITY_ID"));
                    String GPS = cursor.getString(cursor.getColumnIndex("GPS"));
                    listCityGps.add(GPS);
                    if (Utils.isChineseLanguage(context)) {
                        String CITY_NAME = cursor.getString(cursor.getColumnIndex("CITY_NAME"));
                        list.add(CITY_NAME);
                    } else {
                        String CITY_NAME_EN = cursor.getString(cursor.getColumnIndex("CITY_NAME_EN"));
                        list.add(CITY_NAME_EN);
                    }

                } while (cursor.moveToNext());
            }
            gpsValue = listCityGps.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (list == null || list.isEmpty() || gpsValue == null) return false;

        return true;
    }

    private static boolean isTimeZoneAvailable(Context context) {
        return TimeZoneProvider.getRegionId(context) != null;
    }
}
