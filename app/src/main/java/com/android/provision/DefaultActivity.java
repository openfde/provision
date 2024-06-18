package com.android.provision;

import android.app.Activity;
import android.os.Bundle;

public class DefaultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_default);
    }
}
