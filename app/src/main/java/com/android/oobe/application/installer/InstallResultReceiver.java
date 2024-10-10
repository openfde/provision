package com.android.oobe.application.installer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.util.Log;

import com.android.oobe.application.model.Event;
import com.android.oobe.application.model.EventType;

import org.greenrobot.eventbus.EventBus;

public class InstallResultReceiver extends BroadcastReceiver {
    private String TAG = "InstallResultReceiver";
    private boolean INSTALLED = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (intent != null) { // 安装的广播
            final int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            String appName = intent.getStringExtra("appName");
            if (status == PackageInstaller.STATUS_SUCCESS) {
                EventBus.getDefault().post(new Event(EventType.INSTALL_COMPLETED, appName));
            } else {
                String msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            }
            ApkSilentInstaller.setIsInstalling(INSTALLED);
            ApkSilentInstaller.startNextAppInstallation();
        }
    }
}
