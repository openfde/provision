package com.android.provision;


import android.content.Context;

import java.util.Locale;

public class Utils {
    public static boolean isChineseLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.equals("zh");
    }

    public  static  int ToInt(Object ojb){
        if(ojb == null){
            return 0;
        }else {
            return  ToDouble(ojb).intValue();
        }
    }

    public  static  Double ToDouble(Object ojb){
        if(ojb == null){
            return 0.0;
        }else {
            return  Double.valueOf(ToString(ojb));
        }
    }

    public  static  String ToString(Object ojb){
        if(ojb == null){
            return "";
        }else {
            return  String.valueOf(ojb).trim();
        }
    }
}