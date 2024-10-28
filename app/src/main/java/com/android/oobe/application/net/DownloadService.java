package com.android.oobe.application.net;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.android.oobe.Utils;
import com.android.oobe.application.installer.ApkSilentInstaller;
import com.android.oobe.application.model.Event;
import com.android.oobe.application.model.EventType;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

public class DownloadService extends Service {
    private final String TAG = "DownloadService";
    private static final String SOCKET_CLOSED_ERROR_1 = "Socket is closed";
    private static final String SOCKET_CLOSED_ERROR_2 = "Socket Closed";
    private static final String CANCEL_ERROR = "cancel";

    private final Long DEFAULT_BYTE_SIZE = (long) 250 * 1024 * 1024;
    private DownloadBinder downloadBinder;
    private Map<String, Call> callMap;

    // Binder for binding the service to a client component.
    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    // // Cancel an ongoing download for a specific app.
    public boolean cancel(String appName) {
        if (!callMap.containsKey(appName)) return false;
        Call call = callMap.get(appName);
        if (call != null) call.cancel();
        callMap.remove(appName);
        return true;
    }

    // Initiates APK download and registers a callback for handling the response.
    public void downloadApk(String url, String appName, long size, String md5Checksum) {
        if (callMap.containsKey(appName)) return;
        Call call = HttpUtils.get(url, new HttpUtils.HttpCallback() {
            @Override
            public void onResponse(Response response) {
                if (response == null || response.body() == null) {
                    onFailure(new IOException(appName + "response == null .apk Download Failed"));
                    return;
                }
                long contentLength = response.body().contentLength();
                contentLength = contentLength > 0 ? contentLength : DEFAULT_BYTE_SIZE;
                long totalSize = size > 0 ? size : contentLength;

                try {
                    saveApk(response, appName, totalSize, md5Checksum);
                } catch (Exception exception) {
                    onFailure(exception);
                }
            }

            @Override
            public void onFailure(Exception exception) {
                downloadFailed(appName, exception);
            }

            @Override
            public void onError(IOException exception) {
                downloadFailed(appName, exception);
            }
        });
        EventBus.getDefault().post(new Event(EventType.DOWNLOAD_IN_PROGRESS, appName));
        callMap.put(appName, call);
    }

    // Handles download failures by posting a failure event, except for socket-related cancellations.
    private void downloadFailed(String appName, Exception exception) {
        String message = exception.getMessage();
        if (message != null && (StringUtils.containsIgnoreCase(message, SOCKET_CLOSED_ERROR_1) || StringUtils.containsIgnoreCase(message, SOCKET_CLOSED_ERROR_2) || StringUtils.containsIgnoreCase(message, CANCEL_ERROR))) {
            return;
        }
        EventBus.getDefault().post(new Event(EventType.DOWNLOAD_FAILED, appName));
    }

    // Saves the downloaded APK to the device's external storage and verifies it via MD5 checksum.
    private void saveApk(Response response, String appName, long totalSize, String md5Checksum) throws IOException, NoSuchAlgorithmException {
        String apkName = appName + ".apk";
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadDir, apkName);
        long downloadedSize = 0;

        InputStream inputStream = response.body().byteStream();
        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024 * 10];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            downloadedSize += bytesRead;
            int progress = (int) ((downloadedSize * 100) / totalSize);
            EventBus.getDefault().post(new Event(EventType.DOWNLOAD_IN_PROGRESS, appName, progress));
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();

        EventBus.getDefault().post(new Event(EventType.DOWNLOAD_COMPLETED, appName));
        if (StringUtils.isNotEmpty(md5Checksum) && !StringUtils.equals(md5Checksum, Utils.getFileMD5(file))) {
            EventBus.getDefault().post(new Event(EventType.DOWNLOAD_FAILED, appName));
        } else {
            EventBus.getDefault().post(new Event(EventType.DOWNLOAD_COMPLETED, appName));
            ApkSilentInstaller.enqueueInstall(appName, file.getAbsolutePath());
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        downloadBinder = new DownloadBinder();
        callMap = new HashMap<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (String appName : callMap.keySet()) {
            cancel(appName);
        }
        callMap.clear();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return downloadBinder;
    }
}