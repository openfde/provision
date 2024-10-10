package com.android.oobe.application.installer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.util.Log;

import com.android.oobe.App;
import com.android.oobe.application.model.Event;
import com.android.oobe.application.model.EventType;
import com.android.oobe.application.model.InstallRequest;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ApkSilentInstaller {
    /**
     * Installation method adapted for Android 9.
     * Performs a full replacement installation.
     *
     * @return
     */
    private static String TAG = "ApkSilentInstaller";
    private static String APP_NAME = "appName";
    private static boolean isInstalling = false;
    private static Queue<InstallRequest> installQueue = new ConcurrentLinkedQueue<>();
    private static int mSessionId = -1;

/**
 *    PackageInstaller in a multi-threaded installation environment causes broadcast messages
 *    about successful installations to become mixed up, making it difficult to determine the
 *    status of each app's installation. This notification mechanism converts parallel
 *    installations into serial installations.
**/
    public static synchronized void enqueueInstall(String appName, String apkFilePath) {
        installQueue.add(new InstallRequest(appName, apkFilePath));
        startNextAppInstallation();
    }

    public static synchronized void startNextAppInstallation() {
        if (installQueue.isEmpty() || isInstalling) return;
        isInstalling = true;
        InstallRequest installRequest = installQueue.poll();
        installApk(installRequest);
    }

    public static void setIsInstalling(boolean isInstalling) {
        ApkSilentInstaller.isInstalling = isInstalling;
    }

    public static Boolean installApk(InstallRequest installRequest) {
        String appName = installRequest.getAppName();
        String apkFilePath = installRequest.getApkFilePath();
        Context context = App.getContext();
        EventBus.getDefault().post(new Event(EventType.INSTALL_STARTED, appName));
        File apkFile = new File(apkFilePath);
        if (!apkFile.exists()) {
            return null;
        }

        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        sessionParams.setSize(apkFile.length());
        try {
            mSessionId = packageInstaller.createSession(sessionParams);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mSessionId != -1) {
            Log.w(TAG, "mSessionId != -1");
            boolean copySuccess = onTransfersApkFile(context, apkFilePath);
            if (copySuccess) {
                execInstallAPP(appName, context);
            }
        }
        return null;
    }

    /**
     * Transfers the APK file via file streams.
     *
     * @param apkFilePath Path of the APK file
     * @return true if the transfer was successful, false otherwise
     */
    private static boolean onTransfersApkFile(Context context, String apkFilePath) {
        InputStream in = null;
        OutputStream out = null;
        PackageInstaller.Session session = null;
        boolean success = false;
        try {
            File apkFile = new File(apkFilePath);
            session = context.getPackageManager().getPackageInstaller().openSession(mSessionId);
            out = session.openWrite("base.apk", 0, apkFile.length());
            in = new FileInputStream(apkFile);
            int total = 0, c;
            byte[] buffer = new byte[1024 * 1024];
            while ((c = in.read(buffer)) != -1) {
                total += c;
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != session) {
                session.close();
            }
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Executes the installation and notifies the result.
     */
    private static void execInstallAPP(String appName, Context context) {
        PackageInstaller.Session session = null;
        try {
            session = context.getPackageManager().getPackageInstaller().openSession(mSessionId);
            Intent intent = new Intent(context, InstallResultReceiver.class);
            intent.setAction("com.android.oobe.ACTION_INSTALL_RESULT");
            intent.putExtra("SessionId", mSessionId);
            intent.putExtra(APP_NAME, appName);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            session.commit(pendingIntent.getIntentSender());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != session) {
                session.close();
            }
        }
    }
}