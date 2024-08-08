package com.android.provision;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.icu.util.TimeZone;
import android.os.Bundle;
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
import com.android.settingslib.datetime.ZoneGetter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import libcore.timezone.CountryZonesFinder;
import libcore.timezone.TimeZoneFinder;

public class LanguageActivity extends Activity implements LocalePickerWithRegion.LocaleSelectedListener, DatePickerDialog.OnDateSetListener {
    private static final int CHOOSE_LANGUAGE = 1;
    private static final int CHOOSE_KEYBOARD = 2;
    private static final int CHOOSE_APP = 3;
    private static final int CHOOSE_LOCATION = 4;
    private static final int CHOOSE_TIME = 5;
    private static int state = CHOOSE_LANGUAGE;
    private static final String TAG = "LanguageActivity";
    public static final String ADD_LOCALE = "addLocale";
    private TextView mLanguageTitle, mLanguageHint;
    private Button mPrevBtn, mNextBtn, mReturn;
    private LinearLayout direction1, direction2, direction3, direction4, direction5;
    private Fragment localeListEditor, localeListAdd, virtualKeyboard, appSelect, locationSelect, TimeSelect;

    private String LOCALE_LIST_EDITOR = "localeListEditor", LOCALE_LIST_ADD = "localeListAdd", VIRTUAL_KEYBOARD = "virtualKeyboard", APP_OPTION = "appOption";
    private LanguageListener languageListener = new LanguageListener() {
        @Override
        public void languageChanged(Locale locale) {

        }

        @Override
        public void addLanguage() {
            gotoAddFragment();
        }

        @Override
        public void setTimeZone() {
            gotoTimeZoneFragment();
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

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_language);

        //todo remove this after testing first run

        //Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        //Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);
        // remove this activity from the package manager.
        // PackageManager pm = getPackageManager();
        // ComponentName name = new ComponentName(this, DefaultActivity.class);
        // pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        //         PackageManager.DONT_KILL_APP);


        Intent intent = new Intent("com.fde.SYSTEM_INIT_ACTION");
        intent.setPackage("com.boringdroid.systemui");
        sendBroadcast(intent);
        Settings.Global.putString(getContentResolver(), Settings.Global.DEVICE_NAME, "OpenFDE device");


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
        Locale mLocale = this.getResources().getConfiguration().locale;
        String upperCase = mLocale.getCountry().toUpperCase(Locale.US);
        Log.w(TAG, "upperCase = " + upperCase);
        gotoFragment();
        switchListen();
        final Calendar now = Calendar.getInstance();
        //Get TimeZone
        CharSequence timeZoneOffsetAndName = ZoneGetter.getTimeZoneOffsetAndName(this, now.getTimeZone(), now.getTime());
        Log.w(TAG, "timeZoneOffsetAndName = " + timeZoneOffsetAndName);
    }

//    private void goNext() {
//        gotoVirtualKeyboard();
//    }

    private void gotoEditFragment(LocaleStore.LocaleInfo localeInfo) {
//        mPrevBtn.setVisibility(View.GONE);
//        mNextBtn.setVisibility(View.VISIBLE);
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
        if (getFragmentManager().findFragmentByTag(LOCALE_LIST_ADD) != null) {
            Log.w(TAG, "gotoAddFragment");
        } else if (localeListAdd == null) {
            localeListAdd = LocalePickerWithRegion.createLanguagePicker(
                    this, this, false /* translate only */);
        }
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, localeListAdd, LOCALE_LIST_ADD)
                .addToBackStack(null)
                .commit();
    }

    private void gotoVirtualKeyboard() {
//        Log.w(TAG, "gotoVirtualKeyboard");
        if (getFragmentManager().findFragmentByTag(VIRTUAL_KEYBOARD) != null) {
//            Log.w(TAG, "gotoVirtualKeyboard != null");
        } else if (virtualKeyboard == null) {
//            Log.w(TAG, "gotoVirtualKeyboard == null");
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
        GpsSetFragment gpsSetFragment = new GpsSetFragment();
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, gpsSetFragment, VIRTUAL_KEYBOARD)
                .addToBackStack(null)
                .commit();
    }

    private void gotoAppFragment() {
        if (getFragmentManager().findFragmentByTag(APP_OPTION) != null) {
            Log.w(TAG, "gotoAppOptionFragment");
        } else if (appSelect == null) {
            appSelect = new AppOptionFragment();
        }
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, appSelect, APP_OPTION)
                .addToBackStack(null)
                .commit();
    }

    private void gotoTimeFragment() {
//        if (getFragmentManager().findFragmentByTag(APP_OPTION) != null) {
//        } else if (appSelect == null) {
//            appSelect = new AppOptionFragment();
//        }
        TimeFragment timeFragment = new TimeFragment(languageListener);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, timeFragment, APP_OPTION)
                .addToBackStack(null)
                .commit();
    }

    private void gotoTimeZoneFragment() {
        Log.w(TAG, "gotoTimeZoneFragment");
        languageListener.showAndHideButton(View.GONE);
        languageListener.showAndHideReturnButton(View.VISIBLE);
        TimeZoneFragment timeZoneFragment = new TimeZoneFragment(languageListener);
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, timeZoneFragment, LOCALE_LIST_ADD)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLocaleSelected(LocaleStore.LocaleInfo localeInfo) {
        Log.d(TAG, "onLocaleSelected: localeInfo:" + localeInfo + "");
        gotoEditFragment(localeInfo);
    }

    public void switchListen() {
        mPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state <= CHOOSE_LANGUAGE) return;
                state--;
                int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
                getFragmentManager().popBackStack();
                Log.w(TAG, "mPrevBtn onClick and backStackEntryCount = " + backStackEntryCount);
                setView();
            }
        });
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state >= CHOOSE_LANGUAGE && state <= CHOOSE_LOCATION) {
                    state++;
                    gotoFragment();
                } else if (state == CHOOSE_TIME) {//OOBE END, GOTO START
                    if (appSelect != null)
                        ((AppOptionFragment) appSelect).InstallApp();
                    else {// BUG
                    }
                }
                Log.w(TAG, "state = " + state);
            }
        });
        mReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                setView();
            }
        });
    }

    public void gotoFragment() {
        setView();
        switch (state) {
            case CHOOSE_LANGUAGE:
                gotoEditFragment(null);
                break;
            case 2:
                gotoVirtualKeyboard();
                break;
            case 3:
                gotoAppFragment();
                break;
            case 4:
                gotoGpsFragment();
                break;
            case 5:
                gotoTimeFragment();
        }
    }

    public void setView() {
        if (state == CHOOSE_LANGUAGE) {
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
        } else if (state == CHOOSE_KEYBOARD) {
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
        } else if (state == CHOOSE_APP) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_marked_color);
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
            mNextBtn.setLayoutParams(params);
            mLanguageTitle.setText(R.string.application_panel_text);
        } else if (state == CHOOSE_LOCATION) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_marked_color);
            direction4.setBackgroundResource(R.color.direction_marked_color);
            direction5.setBackgroundResource(R.color.direction_color);
            mNextBtn.setBackgroundResource(R.drawable.next_button);
            mNextBtn.setText(R.string.next_button_text);
            mLanguageTitle.setText(R.string.location_panel_text);
        } else if (state == CHOOSE_TIME) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_marked_color);
            direction4.setBackgroundResource(R.color.direction_marked_color);
            direction5.setBackgroundResource(R.color.direction_marked_color);
            languageListener.showAndHideButton(View.VISIBLE);
            mNextBtn.setBackgroundResource(R.drawable.done_button);
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

        void showAndHidePrevButton(int visibility);

        void showAndHideNextButton(int visibility);

        void showAndHideButton(int visibility);

        void showAndHideReturnButton(int visibility);

        void setTimeZone();
    }
}
