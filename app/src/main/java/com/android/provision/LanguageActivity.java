package com.android.provision;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.internal.app.LocalePickerWithRegion;
import com.android.internal.app.LocaleStore;

import java.util.Locale;

public class LanguageActivity extends Activity implements LocalePickerWithRegion.LocaleSelectedListener {
    private static final int SELECT_LANGUAGE = 1;
    private static final int SELECT_KEYBOARD = 2;
    private static final int THIRD = 3;
    private static int state = SELECT_LANGUAGE;
    private static final String TAG = "LanguageActivity";
    public static final String ADD_LOCALE = "addLocale";
    private Fragment localeListEditor, localeListAdd, virtualKeyboard;

    private String LOCALE_LIST_EDITOR = "localeListEditor", LOCALE_LIST_ADD = "localeListAdd", VIRTUAL_KEYBOARD = "virtualKeyboard";
    private LanguageListener languageListener = new LanguageListener() {
        @Override
        public void languageChanged(Locale locale) {
        }

        @Override
        public void addLanguage() {
            gotoAddFragment();
        }

        public void showAndHideButton(int visibility) {
            findViewById(R.id.nextBtn).setVisibility(visibility);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_language);

        //todo remove this after testing first run

        //      Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
//        Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);
        // remove this activity from the package manager.
        // PackageManager pm = getPackageManager();
        // ComponentName name = new ComponentName(this, DefaultActivity.class);
        // pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        //         PackageManager.DONT_KILL_APP);


        Intent intent = new Intent("com.fde.SYSTEM_INIT_ACTION");
        intent.setPackage("com.boringdroid.systemui");
        sendBroadcast(intent);
        Settings.Global.putString(getContentResolver(), Settings.Global.DEVICE_NAME, "OpenFDE device");


        findViewById(R.id.nextBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });
        Listen();
        gotoFragment();
//        gotoEditFragment(null);
    }

    private void goNext() {
        gotoVirtualKeyboard();
    }

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
                .addToBackStack(LOCALE_LIST_ADD)
                .commit();
    }

    private void gotoAddFragment() {
        findViewById(R.id.nextBtn).setVisibility(View.INVISIBLE);
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
                .addToBackStack(LOCALE_LIST_EDITOR)
                .commit();
    }

    private void gotoVirtualKeyboard() {
//        TextView tv = findViewById(R.id.language_title);
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
                .addToBackStack(LOCALE_LIST_EDITOR)
                .commit();
    }

    @Override
    public void onLocaleSelected(LocaleStore.LocaleInfo localeInfo) {
        Log.d(TAG, "onLocaleSelected: localeInfo:" + localeInfo + "");
        gotoEditFragment(localeInfo);
    }

    public void Listen() {
        findViewById(R.id.direction1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = 1;
                gotoFragment();
            }
        });

        findViewById(R.id.direction2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = 2;
                gotoFragment();
            }
        });

        findViewById(R.id.direction3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = 3;
                gotoFragment();
            }
        });

        findViewById(R.id.nextBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state++;
                gotoFragment();
            }
        });
    }

    public void gotoFragment() {
        setDirectionColor();
        switch (state) {
            case 1:
                gotoEditFragment(null);
                break;
            case 2:
                gotoVirtualKeyboard();
                break;
            case 3:
                gotoEditFragment(null);
                break;
        }
    }

    public void setDirectionColor() {
        findViewById(R.id.direction1).setBackgroundResource(R.color.direction_color);
        findViewById(R.id.direction2).setBackgroundResource(R.color.direction_color);
        findViewById(R.id.direction3).setBackgroundResource(R.color.direction_color);
        if (state >= SELECT_LANGUAGE)
            findViewById(R.id.direction1).setBackgroundResource(R.color.direction_marked_color);
        if (state >= SELECT_KEYBOARD)
            findViewById(R.id.direction2).setBackgroundResource(R.color.direction_marked_color);
        if (state >= THIRD)
            findViewById(R.id.direction3).setBackgroundResource(R.color.direction_marked_color);
    }

    public interface LanguageListener {

        void languageChanged(Locale locale);

        void addLanguage();

        void showAndHideButton(int visibility);
    }
}
