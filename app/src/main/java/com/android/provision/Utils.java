package com.android.provision;


import android.content.Context;
import android.view.View;

import java.util.Locale;

public class Utils {

    public static final int PADDING = 94;
    public static final String REGION_ID = "region_id";

    public static void setFragmentPadding(View view, int left, int top, int right, int bottom) {
        Context context = view.getContext();
        float density = context.getResources().getDisplayMetrics().density;
        // in pixels
        int paddingLeft = Math.round(left * density);
        int paddingTop = Math.round(top * density);
        int paddingRight = Math.round(right * density);
        int paddingBottom = Math.round(bottom * density);
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }


    public static boolean isChineseLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.equals("zh");
    }

    public static int ToInt(Object ojb) {
        if (ojb == null) {
            return 0;
        } else {
            return ToDouble(ojb).intValue();
        }
    }

    public static Double ToDouble(Object ojb) {
        if (ojb == null) {
            return 0.0;
        } else {
            return Double.valueOf(ToString(ojb));
        }
    }

    public static String ToString(Object ojb) {
        if (ojb == null) {
            return "";
        } else {
            return String.valueOf(ojb).trim();
        }
    }
}