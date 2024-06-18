package com.android.provision;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.Arrays;

public class LanguageActivity extends Activity {

    private static final String TAG = "LanguageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_language);
        String[] locales = Resources.getSystem().getAssets().getLocales();
        String string = Arrays.stream(locales).toArray().toString();
        Log.d(TAG, "onCreate():  language :" + string + "");
    }
}
