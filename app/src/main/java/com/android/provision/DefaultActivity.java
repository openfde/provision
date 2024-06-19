package com.android.provision;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class DefaultActivity extends Activity {

    private String TAG = "DefaultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent("com.fde.SYSTEM_INIT_ACTION");
        intent.setPackage("com.boringdroid.systemui");
        sendBroadcast(intent);
        Settings.Global.putString(getContentResolver(), Settings.Global.DEVICE_NAME, "OpenFDE device");
        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);
        startActivity(new Intent(this, LanguageActivity.class));
        finish();

        // remove this activity from the package manager.
        // PackageManager pm = getPackageManager();
        // ComponentName name = new ComponentName(this, DefaultActivity.class);
        // pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        //         PackageManager.DONT_KILL_APP);
    }
}
