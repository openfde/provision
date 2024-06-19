package com.android.provision;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.UserManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

public class Utility {
    public static final String ROTATION_NAME = "default_rotation";
    public static final String BACKKEYLAUNCHED = "BackKeyLaunched";

    public static int getSetupComplete(Context context) {
        int setupComplete = 0;
        try {
            setupComplete = Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete");
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return setupComplete;
    }

    public static boolean isPackageExist(Context context, String packageName) {
        if (packageName == null || packageName.equals("")) {
            return false;
        }
        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            Log.d(Define.TAG, "package does't exist:" + packageName);
            return false;
        }
    }

    public static boolean isIntentExist(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        ResolveInfo info = pm.resolveActivity(intent, 0);
        if (info == null) {
            Log.d(Define.TAG, "intent is invaid:" + intent);
            return false;
        } else {
            return true;
        }
    }

    public static void controlMobileConnection(Context context, boolean enabled) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method setMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (setMobileDataEnabledMethod != null) {
                setMobileDataEnabledMethod.invoke(tm, enabled);
            }
        } catch (Exception e) {
            Log.e(Define.TAG, "TelephonyManager setMobileDataEnabled error", e);
        }
    }

    public static void enableWiFiConnection(Context context) {
        try {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                Log.e(Define.TAG, "Can't get WifiManager");
                return;
            }
            if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                return;
            }
            wifiManager.setWifiEnabled(true);
        } catch (Exception e) {
            Log.e(Define.TAG, "enableWiFiConnection exception:", e);
        }
    }

    public static void disableWiFiConnection(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifi.disconnect();
    }

    public static boolean has3GModule(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return false; //cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

         NetworkInfo localNetworkInfo = connectivityManager.getActiveNetworkInfo();
         if ((localNetworkInfo != null) && (localNetworkInfo.isConnected())) {
             return true;
         }
         return false;
    }

    public static boolean isRestricted(Context context, String restrictionKey) {
        final UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
        return um.hasUserRestriction(restrictionKey);
    }

}