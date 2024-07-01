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


import com.android.internal.app.LocalePickerWithRegion;
import com.android.internal.app.LocaleStore;

import java.util.Locale;

public class LanguageActivity extends Activity implements LocalePickerWithRegion.LocaleSelectedListener {

    private static final String TAG = "LanguageActivity";
    public static final String ADD_LOCALE = "addLocale";
    private Fragment localeListEditor, localeListAdd, virtualKeyboard;

    private String LOCALE_LIST_EDITOR = "localeListEditor", LOCALE_LIST_ADD = "localeListAdd"
            ,VIRTUAL_KEYBOARD = "virtualKeyboard";
    private LanguageListener languageListener = new LanguageListener() {
        @Override
        public void languageChanged(Locale locale) {
        }

        @Override
        public void addLanguage() {
            gotoAddFragment();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        gotoEditFragment(null);

    }

    private void goNext() {
        gotoVirtualKeyboard();
    }

    private void gotoEditFragment(LocaleStore.LocaleInfo localeInfo) {
        if(getFragmentManager().findFragmentByTag(LOCALE_LIST_EDITOR) != null && localeInfo != null){
            Bundle bundle = new Bundle();
            bundle.putSerializable(ADD_LOCALE, localeInfo);
            localeListEditor.setArguments(bundle);
        } else if(localeListEditor == null){
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
        if(getFragmentManager().findFragmentByTag(LOCALE_LIST_ADD) != null){

        } else if(localeListAdd == null){
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
        if(getFragmentManager().findFragmentByTag(VIRTUAL_KEYBOARD) != null){

        } else if(virtualKeyboard == null){
            Log.d(TAG, "gotoVirtualKeyboard");
            virtualKeyboard = new VirtualKeyboardFragment(languageListener);
        }
        getFragmentManager()
                .beginTransaction()
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


    public interface LanguageListener {

        void languageChanged(Locale locale);

        void addLanguage();
    }
}
