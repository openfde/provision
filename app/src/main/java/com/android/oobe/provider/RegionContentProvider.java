package com.android.oobe.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import com.android.oobe.BaseDataBase;
import com.android.oobe.dao.RegionDao;

public class RegionContentProvider extends ContentProvider {
    private BaseDataBase database;
    private RegionDao dao;

    private static final String TABLE_REGION_INFO = "REGION_INFO";
    private static final String REGION_COUNTRY = "REGION_COUNTRY";
    private static final String REGION_PROVINCE = "REGION_PROVINCE";

    private static final int CODE_REGION_INFO = 100;
    private static final int CODE_REGION_COUNTRY = 200;
    private static final int CODE_REGION_PROVINCE = 300;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI("com.android.oobe.region", TABLE_REGION_INFO, CODE_REGION_INFO);
        uriMatcher.addURI("com.android.oobe.region", REGION_COUNTRY, CODE_REGION_COUNTRY);
        uriMatcher.addURI("com.android.oobe.region", REGION_PROVINCE, CODE_REGION_PROVINCE);
    }

    @Override
    public boolean onCreate() {
        database = Room.databaseBuilder(getContext(), BaseDataBase.class, "fde_database").build();
        dao = database.regionDao();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case CODE_REGION_INFO:
                cursor = dao.getAllCitys(selectionArgs[0]);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case CODE_REGION_COUNTRY:
                cursor = dao.getAllCoutry();
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case CODE_REGION_PROVINCE:
                cursor = dao.getAllProvinces();
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
        }
        return  cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
