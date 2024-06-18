package com.android.provision;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class DefaultActivity extends Activity {

    private String TAG = "DefaultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate():  savedInstanceState :" + savedInstanceState + "");
        setContentView(R.layout.layout_default);
        startActivity(new Intent(this, LanguageActivity.class));
//        Intent intent = new Intent();
//        intent.setAction("com.fde.oobe.CHOOSE_LANGUAGE");
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        if (Utility.isIntentExist(this, intent)){
//            startActivity(intent);
//            finish();
//        }
    }
}
