package com.android.oobe;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.app.LocalePickerWithRegion;
import com.android.internal.app.LocaleStore;
import com.android.internal.policy.DecorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LanguageActivity extends Activity implements LocalePickerWithRegion.LocaleSelectedListener, DatePickerDialog.OnDateSetListener {
    //    private static final int CHOOSE_LANGUAGE = 1;
//    private static final int CHOOSE_KEYBOARD = 2;
//    private static final int CHOOSE_APP = 3;
//    private static final int CHOOSE_LOCATION = 4;
//    private static final int CHOOSE_TIME = 5;
    private static final int CHOOSE_LANGUAGE = 1;
    private static final int CHOOSE_KEYBOARD = 2;
    private static final int CHOOSE_LOCATION = 3;
    private static final int CHOOSE_TIME = 4;
    private static int state = CHOOSE_LANGUAGE;
    private static final String TAG = "LanguageActivity";
    public static final String ADD_LOCALE = "addLocale";
    private static final String AVAILABLE = "isAvailable";
    private Boolean isAvailable = false;
    private TextView mLanguageTitle, mLanguageHint;
    private Button mPrevBtn, mNextBtn, mReturn;
    private LinearLayout direction1, direction2, direction3, direction4, direction5;
    private Fragment localeListEditor, localeListAdd, virtualKeyboard, appOptionFragment, gpsSetFragment,
            timeFragment, timeZoneFragment, regionFragment, regionZoneFragment;
    private SharedPreferences sharedPreferences;

    private String LOCALE_LIST_EDITOR = "localeListEditor", LOCALE_LIST_ADD = "localeListAdd", VIRTUAL_KEYBOARD = "virtualKeyboard",
            GPS_ADD = "gpsAdd", APP_OPTION = "appOption", TIME = "time", TIME_ZONE = "timeZone", REGION = "region", REGION_ZONE = "regionZone";

    private Handler handler = new Handler(Looper.getMainLooper());

    private LanguageListener languageListener = new LanguageListener() {
        @Override
        public void languageChanged(Locale locale) {
        }

        @Override
        public void addLanguage() {
            gotoAddFragment();
        }

        @Override
        public void addTimeZone() {
            gotoTimeZoneFragment();
        }

        @Override
        public void addRegion() {
            gotoRegionFragment();
        }

        @Override
        public void addRegionZone(String regionId) {
            gotoRegionZoneFragment(regionId);
        }


        @Override
        public void showAndHideButton(int visibility) {
            showAndHidePrevButton(visibility);
            showAndHideNextButton(visibility);
        }

        @Override
        public void showAndHidePrevButton(int visibility) {
            mPrevBtn.setVisibility(visibility);
        }

        @Override
        public void showAndHideNextButton(int visibility) {
            mNextBtn.setVisibility(visibility);
        }

        public void showAndHideReturnButton(int visibility) {
            mReturn.setVisibility(visibility);
        }

        @Override
        public void backToTime() {
            goBackToTimeFragment();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        isAvailable = sharedPreferences.getBoolean(AVAILABLE, false);
        if (isAvailable) finishSetUpWizard();

        super.onCreate(savedInstanceState);

        Intent intent = new Intent("com.fde.SYSTEM_INIT_ACTION");
        intent.setPackage("com.boringdroid.systemui");
        sendBroadcast(intent);

        fetchDataPeriodically();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DecorView decorView = (DecorView) getWindow().getDecorView();
        decorView.startFullScreenWindow();
        setContentView(R.layout.activity_language);


//        findViewById(R.id.nextBtn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                goNext();
//            }
//        });
        mLanguageTitle = findViewById(R.id.language_title);
        mLanguageHint = findViewById(R.id.language_hint);
        direction1 = findViewById(R.id.direction1);
        direction2 = findViewById(R.id.direction2);
        direction3 = findViewById(R.id.direction3);
        direction4 = findViewById(R.id.direction4);
        direction5 = findViewById(R.id.direction5);
        mPrevBtn = findViewById(R.id.prevBtn);
        mNextBtn = findViewById(R.id.nextBtn);
        mReturn = findViewById(R.id.returnBtn);
        gotoFragment();
        switchListen();
    }

    private void fetchDataPeriodically() {
        final Runnable fetchTask = new Runnable() {
            @Override
            public void run() {
                if (fetchData()) {
                    findViewById(R.id.provisionRelativeLayout).setVisibility(View.VISIBLE);
                    handler.removeCallbacks(this);
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(fetchTask);
    }

    private boolean fetchData() {
        return isGpsAvailable() && isTimeZoneAvailable();
    }


    private Boolean isGpsAvailable() {
        final String REGION_URI = "content://com.boringdroid.systemuiprovider.region";
        Cursor cursor = null;
        Map<String, Object> result = null;
        String selection = null;
        String[] selectionArgs = null;
        List<String> list = null;
        try {
            ContentResolver contentResolver = getContentResolver();
            cursor = contentResolver.query(Uri.parse(REGION_URI + "/REGION_COUNTRY"), null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    if (Utils.isChineseLanguage(this)) {
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
        if (!Utils.isChineseLanguage(this)) {
            selection = "COUNTRY_NAME_EN = ?";
        }
        selectionArgs = new String[]{countryName};
        list = null;
        try {
            ContentResolver contentResolver = getContentResolver();
            cursor = contentResolver.query(Uri.parse(REGION_URI + "/REGION_PROVINCE"), null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    if (Utils.isChineseLanguage(this)) {
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
        if (!Utils.isChineseLanguage(this)) {
            selection = "PROVINCE_NAME_EN = ?";
        }
        selectionArgs = new String[]{province};
        list = null;
        List<String> listCityGps = new ArrayList<>();
        String gpsValue = null;
        try {
            ContentResolver contentResolver = getContentResolver();
            cursor = contentResolver.query(Uri.parse(REGION_URI + "/REGION_INFO"), null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                list = new ArrayList<>();
                do {
                    String CITY_ID = cursor.getString(cursor.getColumnIndex("CITY_ID"));
                    String GPS = cursor.getString(cursor.getColumnIndex("GPS"));
                    listCityGps.add(GPS);
                    if (Utils.isChineseLanguage(this)) {
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

    private boolean isTimeZoneAvailable() {
        return TimeZoneProvider.getRegionId(this) != null;
    }


    private void gotoEditFragment(LocaleStore.LocaleInfo localeInfo) {
        if (getFragmentManager().findFragmentByTag(LOCALE_LIST_EDITOR) != null && localeInfo != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(ADD_LOCALE, localeInfo);
            localeListEditor.setArguments(bundle);
            Log.w(TAG, "EditFragment != null");
        } else if (localeListEditor == null) {
            localeListEditor = new LocaleListEditFragment(languageListener);
        }
        Log.d(TAG, "gotoEditFragment");
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, localeListEditor, LOCALE_LIST_EDITOR)
                .addToBackStack(null)
                .commit();
        Log.w(TAG, "mPreBtn = " + (mPrevBtn.getVisibility() == View.INVISIBLE));
    }

    private void gotoAddFragment() {
        languageListener.showAndHideButton(View.GONE);
        if (localeListAdd == null) {
            localeListAdd = LocalePickerWithRegion.createLanguagePicker(this, this, false /* translate only */);
        }
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, localeListAdd, LOCALE_LIST_ADD)
                .addToBackStack(null)
                .commit();
    }

    private void gotoVirtualKeyboard() {
        if (getFragmentManager().findFragmentByTag(VIRTUAL_KEYBOARD) != null) {
        } else if (virtualKeyboard == null) {
            virtualKeyboard = new VirtualKeyboardFragment(languageListener);
        }
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, virtualKeyboard, VIRTUAL_KEYBOARD)
                .addToBackStack(null)
                .commit();
    }

    private void gotoGpsFragment() {
        if (getFragmentManager().findFragmentByTag(GPS_ADD) != null) {
        } else if (gpsSetFragment == null) {
            gpsSetFragment = new GpsSetFragment();
        }
        Log.w(TAG, "gpsSetFragment = null ? = " + (gpsSetFragment == null));
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, gpsSetFragment, GPS_ADD)
                .addToBackStack(null)
                .commit();
    }

    private void gotoAppFragment() {
        if (getFragmentManager().findFragmentByTag(APP_OPTION) != null) {
            Log.w(TAG, "gotoAppOptionFragment");
        } else if (appOptionFragment == null) {
            appOptionFragment = new AppOptionFragment();
        }
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, appOptionFragment, APP_OPTION)
                .addToBackStack(null)
                .commit();
    }

    private void gotoTimeFragment() {
        if (getFragmentManager().findFragmentByTag(TIME) != null) {
        } else if (timeFragment == null) {
            timeFragment = new TimeFragment();
        }
        ((TimeFragment) timeFragment).setLanguageListener(languageListener);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, timeFragment, TIME)
                .addToBackStack(TIME)
                .commit();
    }

    private void gotoTimeZoneFragment() {
        languageListener.showAndHideButton(View.GONE);
        languageListener.showAndHideReturnButton(View.VISIBLE);
        if (getFragmentManager().findFragmentByTag(TIME_ZONE) != null) {
        } else if (timeZoneFragment == null) {
            timeZoneFragment = new TimeZoneFragment();
        }
        ((TimeZoneFragment) timeZoneFragment).setLanguageListener(languageListener);
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, timeZoneFragment, TIME_ZONE)
                .addToBackStack(null)
                .commit();
    }

    private void gotoRegionFragment() {
        Log.w(TAG, "gotoTimeZoneFragment");
        if (getFragmentManager().findFragmentByTag(REGION) != null) {
        } else if (regionFragment == null) {
            regionFragment = new RegionFragment();
        }
        ((RegionFragment) regionFragment).setLanguageListener(languageListener);

        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, regionFragment, REGION)
                .addToBackStack(null)
                .commit();
    }

    private void gotoRegionZoneFragment(String regionId) {
        if (getFragmentManager().findFragmentByTag(REGION_ZONE) != null) {
        } else if (regionZoneFragment == null) {
            regionZoneFragment = new RegionZoneFragment();
        }
        ((RegionZoneFragment) regionZoneFragment).setRegionId(regionId);
        ((RegionZoneFragment) regionZoneFragment).setLanguageListener(languageListener);
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, regionZoneFragment, REGION_ZONE)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLocaleSelected(LocaleStore.LocaleInfo localeInfo) {
        Log.d(TAG, "onLocaleSelected: localeInfo:" + localeInfo + "");
        gotoEditFragment(localeInfo);
    }

    private void goBackToTimeFragment() {
        getFragmentManager().popBackStack(TIME, 0);
        mReturn.setVisibility(View.GONE);
        setView();
    }

    public void switchListen() {
        mPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state <= CHOOSE_LANGUAGE) return;
                state--;
                getFragmentManager().popBackStack();
                setView();
            }
        });
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state >= CHOOSE_LANGUAGE && state <= CHOOSE_LOCATION) {
                    state++;
                    gotoFragment();
                } else if (state == CHOOSE_TIME) {//Provision end, Start launcher
                    if (appOptionFragment != null)
                        ((AppOptionFragment) appOptionFragment).InstallApp();
                    finishSetUpWizard();
                }
            }
        });
        mReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "returnOnclickListener");
                goBackToTimeFragment();
            }
        });
    }

    public void gotoFragment() {
        setView();
        switch (state) {
            case CHOOSE_LANGUAGE:
                gotoEditFragment(null);
                break;
            case CHOOSE_KEYBOARD:
                gotoVirtualKeyboard();
                break;
//            case CHOOSE_APP:
//                gotoAppFragment();
//                break;
            case CHOOSE_LOCATION:
                gotoGpsFragment();
                break;
            case CHOOSE_TIME:
                gotoTimeFragment();
        }
    }

    private void finishSetUpWizard() {
        Log.e(TAG, "finishSetUpWizard start");

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(AVAILABLE, false).commit();

        Settings.Global.putString(getContentResolver(), Settings.Global.DEVICE_NAME, "OpenFDE device");
        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);

        // remove this activity from the package manager.
        PackageManager pm = getPackageManager();
        ComponentName name = new ComponentName("com.android.oobe", "com.android.oobe.LanguageActivity");
//        ComponentName name = new ComponentName(this, LanguageActivity.class);
//        ComponentName name = new ComponentName(getApplicationContext(), LanguageActivity.class);
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        Log.e(TAG, "finishSetUpWizard success");
        finish();
    }

    public void setView() {
        if (state == 1) {
            // Set directionColor
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_color);
            direction3.setBackgroundResource(R.color.direction_color);
            direction4.setBackgroundResource(R.color.direction_color);
            direction5.setBackgroundResource(R.color.direction_color);

            // Set prevButton and nextButton
            ViewGroup.LayoutParams params = findViewById(R.id.nextBtn).getLayoutParams();
            params.width = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    300,
                    this.getResources().getDisplayMetrics()
            );
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    44,
                    this.getResources().getDisplayMetrics()
            );
            mPrevBtn.setVisibility(View.GONE);
            mNextBtn.setLayoutParams(params);
            mNextBtn.setBackgroundResource(R.drawable.next_button_init);
            mNextBtn.setText(R.string.next_button_text);

            // Set Panel
            mLanguageTitle.setText(R.string.language_panel_text);

            // Set Language Hint
            mLanguageHint.setVisibility(View.VISIBLE);
        } else if (state == 2) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_color);
            direction4.setBackgroundResource(R.color.direction_color);
            direction5.setBackgroundResource(R.color.direction_color);

            ViewGroup.LayoutParams params = findViewById(R.id.nextBtn).getLayoutParams();
            params.width = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    199,
                    this.getResources().getDisplayMetrics()
            );
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    44,
                    this.getResources().getDisplayMetrics()
            );
            mPrevBtn.setVisibility(View.VISIBLE);
            mNextBtn.setLayoutParams(params);
            mNextBtn.setBackgroundResource(R.drawable.next_button);
            mNextBtn.setText(R.string.next_button_text);

            mLanguageTitle.setText(R.string.keyboard_panel_text);

            mLanguageHint.setVisibility(View.INVISIBLE);
        } else if (state == 3) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_marked_color);
            direction4.setBackgroundResource(R.color.direction_color);
            direction5.setBackgroundResource(R.color.direction_color);

//            mLanguageTitle.setText(R.string.application_panel_text);


            mNextBtn.setText(R.string.next_button_text);
            mLanguageTitle.setText(R.string.location_panel_text);
        } else if (state == 4) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_marked_color);
            direction4.setBackgroundResource(R.color.direction_marked_color);
            direction5.setBackgroundResource(R.color.direction_color);

//            mNextBtn.setBackgroundResource(R.drawable.next_button);
//            mNextBtn.setText(R.string.next_button_text);
//            mLanguageTitle.setText(R.string.location_panel_text);


            mNextBtn.setText(R.string.done_button_text);
            mLanguageTitle.setText(R.string.time_panel_text);
            languageListener.showAndHideButton(View.VISIBLE);
        } else if (state == 5) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_marked_color);
            direction4.setBackgroundResource(R.color.direction_marked_color);
            direction5.setBackgroundResource(R.color.direction_marked_color);

            languageListener.showAndHideButton(View.VISIBLE);
//            mNextBtn.setBackgroundResource(R.drawable.done_button);
            mNextBtn.setText(R.string.done_button_text);

            mLanguageTitle.setText(R.string.time_panel_text);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
    }

    public interface LanguageListener {

        void languageChanged(Locale locale);

        void addLanguage();

        void addTimeZone();

        void addRegion();

        void addRegionZone(String regionId);

        void showAndHidePrevButton(int visibility);

        void showAndHideNextButton(int visibility);

        void showAndHideButton(int visibility);

        void showAndHideReturnButton(int visibility);

        void backToTime();
    }
}
