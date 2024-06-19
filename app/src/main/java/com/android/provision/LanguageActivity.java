package com.android.provision;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.android.internal.app.LocalePickerWithRegion;
import com.android.internal.app.LocaleStore;

import java.util.Locale;

public class LanguageActivity extends Activity implements LocalePickerWithRegion.LocaleSelectedListener {

    private static final String TAG = "LanguageActivity";
    public static final String ADD_LOCALE = "addLocale";
    private Fragment localeListEditor, localeListAdd;
    private String LOCALE_LIST_EDITOR = "localeListEditor", LOCALE_LIST_ADD = "localeListAdd";
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
        gotoEditFragment(null);
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
