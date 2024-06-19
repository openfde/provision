package com.android.provision;

import android.content.res.Resources;
import android.util.Log;

import java.lang.reflect.Field;

public class ResourcesUtil {

    public static String[] getStringArray(String key) {
        try {
            Resources systemResources = Resources.getSystem();
            Class<?> rArrayClass = Class.forName("com.android.internal.R$array");
            Field field = rArrayClass.getField(key);
            int resourceId = field.getInt(null);
            String[] specialLocaleCodes = systemResources.getStringArray(resourceId);
            for (String code : specialLocaleCodes) {
                Log.d("TAG", "getStringArray: code:" + code + "");
            }
            return specialLocaleCodes;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
