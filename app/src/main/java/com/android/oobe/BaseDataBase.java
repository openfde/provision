package com.android.oobe;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.android.oobe.application.model.RegionInfo;
import com.android.oobe.dao.RegionDao;

@Database(entities = {RegionInfo.class}, version = 12, exportSchema = false)
public abstract class BaseDataBase extends RoomDatabase {
    private static BaseDataBase instance;

    public abstract RegionDao regionDao();

    public static synchronized BaseDataBase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context,
                            BaseDataBase.class, "fde_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
