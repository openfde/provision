package com.android.provision;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.app.LocalePickerWithRegion;
import com.android.internal.app.LocaleStore;

import java.util.Locale;

public class LanguageActivity extends Activity implements LocalePickerWithRegion.LocaleSelectedListener {
    private static final int SELECT_LANGUAGE = 1;
    private static final int SELECT_KEYBOARD = 2;
    private static final int SELECT_APP = 3;
    private static int state = SELECT_LANGUAGE;
    private static final String TAG = "LanguageActivity";
    public static final String ADD_LOCALE = "addLocale";
    private TextView mLanguageTitle, mLanguageHint;
    private Button mPrevBtn, mNextBtn;
    private LinearLayout direction1, direction2, direction3;
    private Fragment localeListEditor, localeListAdd, virtualKeyboard, appOption;

    private String LOCALE_LIST_EDITOR = "localeListEditor", LOCALE_LIST_ADD = "localeListAdd", VIRTUAL_KEYBOARD = "virtualKeyboard", APP_OPTION = "appOption";
    private LanguageListener languageListener = new LanguageListener() {
        @Override
        public void languageChanged(Locale locale) {

        }

        @Override
        public void addLanguage() {
            gotoAddFragment();
        }

        public void showAndHideButton(int visibility) {
            mNextBtn.setVisibility(visibility);
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
        mPrevBtn = findViewById(R.id.prevBtn);
        mNextBtn = findViewById(R.id.nextBtn);
        switchListen();
        gotoFragment();
//        gotoEditFragment(null);
    }

//    private void goNext() {
//        gotoVirtualKeyboard();
//    }

    private void gotoEditFragment(LocaleStore.LocaleInfo localeInfo) {
        if (getFragmentManager().findFragmentByTag(LOCALE_LIST_EDITOR) != null && localeInfo != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(ADD_LOCALE, localeInfo);
            localeListEditor.setArguments(bundle);
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
    }

    private void gotoAddFragment() {
        languageListener.showAndHideButton(View.INVISIBLE);
        if (getFragmentManager().findFragmentByTag(LOCALE_LIST_ADD) != null) {
        } else if (localeListAdd == null) {
            Log.d(TAG, "gotoAddFragment");
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
        if (getFragmentManager().findFragmentByTag(VIRTUAL_KEYBOARD) != null) {
        } else if (virtualKeyboard == null) {
            Log.d(TAG, "gotoVirtualKeyboard");
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

    private void gotoAppOptionFragment() {
        Log.d(TAG, "gotoAppOptionFragment");
        if (getFragmentManager().findFragmentByTag(APP_OPTION) != null) {
        } else if (appOption == null) {
            appOption = new AppOptionFragment();
        }
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, appOption, APP_OPTION)
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
                if (state <= SELECT_LANGUAGE) return;
                state--;
                getFragmentManager().popBackStack();
                setView();
            }
        });
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state >= SELECT_LANGUAGE && state <= SELECT_KEYBOARD) {
                    state++;
                    gotoFragment();
                } else if (state == SELECT_APP) {//OOBE END, GOTO START
                    if (appOption != null)
                        ((AppOptionFragment) appOption).InstallApp();
                    else {// BUG
                    }
                }
            }
        });
    }

    public void gotoFragment() {
        setView();
        switch (state) {
            case 1:
                gotoEditFragment(null);
                break;
            case 2:
                gotoVirtualKeyboard();
                break;
            case 3:
                gotoAppOptionFragment();
                break;
        }
    }

    public void setView() {
        if (state == SELECT_LANGUAGE) {
            // Set directionColor
            direction1.setBackgroundResource(R.color.direction_marked_color);

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
        } else if (state == SELECT_KEYBOARD) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
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
            mLanguageTitle.setText(R.string.keyboard_panel_text);
            mLanguageHint.setVisibility(View.INVISIBLE);
        } else if (state == SELECT_APP) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_marked_color);
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
            mNextBtn.setBackgroundResource(R.drawable.done_button);
            mNextBtn.setText(R.string.done_button_text);
            mLanguageTitle.setText(R.string.application_panel_text);
        }

    }

    public interface LanguageListener {

        void languageChanged(Locale locale);

        void addLanguage();

        void showAndHideButton(int visibility);
    }
}
