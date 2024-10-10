package com.android.oobe.application;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.android.oobe.BaseSonFragment;
import com.android.oobe.EventBusUtils;
import com.android.oobe.LanguageActivity;
import com.android.oobe.R;
import com.android.oobe.application.model.AppDownloadInfo;
import com.android.oobe.application.model.AppInfo;
import com.android.oobe.application.model.Event;
import com.android.oobe.application.model.EventType;
import com.android.oobe.application.net.DownloadService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AppDownloadFragment extends BaseSonFragment {
    private final String TAG = "AppDownloadFragment";
    private final boolean IS_SELECTED = true;
    private final boolean IS_NOT_SELECTED = false;
    private RecyclerView downloadingRecyclerView;
    private RecyclerView noDownloadingRecyclerView;
    private AppDownloadAdapter appDownloadAdapter = new AppDownloadAdapter();
    private AppNoDownloadAdapter appNoDownloadAdapter = new AppNoDownloadAdapter();

    private DownloadService downloadService;
    private Singleton singleton;

    public AppDownloadFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        singleton = Singleton.getInstance();
        downloadService = ((LanguageActivity) getActivity()).getDownloadService();
        EventBus.getDefault().register(this);
        installApp();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.layout_application_download, container, false);

        downloadingRecyclerView = view.findViewById(R.id.downloadingRecyclerView);
        noDownloadingRecyclerView = view.findViewById(R.id.noDownloadingRecyclerView);

        downloadingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        downloadingRecyclerView.setAdapter(appDownloadAdapter);
        ((SimpleItemAnimator) downloadingRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        noDownloadingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        noDownloadingRecyclerView.setAdapter(appNoDownloadAdapter);
        ((SimpleItemAnimator) downloadingRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void handlerEvent(Event event) {
        String appName = event.getAppName();
        switch (event.eventType) {
            case DOWNLOAD_START:
                downloadStart(appName);
                break;
            case DOWNLOAD_IN_PROGRESS:
                updateProgress(appName, event.getProgress());
                break;
            case DOWNLOAD_STOP:
                downloadStop(appName);
                break;
            case DOWNLOAD_COMPLETED:
            case INSTALL_STARTED:
                installStart(appName);
                break;
            case INSTALL_COMPLETED:
                installComplete(appName);
                break;
            case DOWNLOAD_FAILED:
                downloadFailed(appName);
                break;
        }
    }

    private void downloadCompleted(String appName) {
    }

    public void updateProgress(String appName, int progress) {
        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        if (appDownloadInfo.getEventType() != EventType.DOWNLOAD_IN_PROGRESS || progress == appDownloadInfo.getProgress())
            return;
        appDownloadInfo.setProgress(progress);
        appDownloadAdapter.updateProgress(appName);
    }

    public void downloadStart(String appName) {
        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        EventType eventType = appDownloadInfo.getEventType();
        if (eventType != EventType.DOWNLOAD_PENDING && eventType != EventType.DOWNLOAD_FAILED) {
            return;
        }
        appDownloadInfo.setEventType(EventType.DOWNLOAD_IN_PROGRESS);
        EventBusUtils.sendEvent(new Event(EventType.DOWNLOAD_IN_PROGRESS, appName));

        AppInfo appInfo = appDownloadInfo.getAppInfo();
        appDownloadInfo.setSelected(IS_SELECTED);

        if (eventType == EventType.DOWNLOAD_FAILED && appInfo.getBackupUrl() != null) {
            downloadService.downloadApk(appInfo.getBackupUrl(), appInfo.getName(), appInfo.getBackupSize(), appInfo.getBackupMd5Checksum());
        } else {
            downloadService.downloadApk(appInfo.getPrimaryUrl(), appInfo.getName(), appInfo.getPrimarySize(), appInfo.getPrimaryMd5Checksum());
        }
        appDownloadAdapter.add(appDownloadInfo);
        appNoDownloadAdapter.remove(appDownloadInfo);
    }

    public void downloadStop(String appName) {
        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        appDownloadInfo.setProgress(-1);
        appDownloadInfo.setSelected(IS_NOT_SELECTED);
        appDownloadInfo.setEventType(EventType.DOWNLOAD_PENDING);

        downloadService.cancel(appName);

        appDownloadAdapter.remove(appDownloadInfo);
        appNoDownloadAdapter.add(appDownloadInfo);
    }

    public void installStart(String appName) {
        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        appDownloadInfo.setEventType(EventType.INSTALL_STARTED);

        appDownloadAdapter.updateEventType(appName);
    }

    public void installComplete(String appName) {
        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        appDownloadInfo.setEventType(EventType.INSTALL_COMPLETED);

        downloadService.cancel(appName);

        appDownloadAdapter.updateEventType(appName);
    }

    public void downloadFailed(String appName) {
        Toast.makeText(getContext(), appName + getContext().getString(R.string.download_failed), Toast.LENGTH_SHORT).show();

        AppDownloadInfo appDownloadInfo = singleton.getAppDownloadInfo(appName);
        appDownloadInfo.setProgress(-1);
        appDownloadInfo.setEventType(EventType.DOWNLOAD_FAILED);

        downloadService.cancel(appName);

        appDownloadAdapter.updateEventType(appName);
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }

    public void installApp() {
        if (Singleton.getInstance().getAppDownloadInfoList() == null) {
            return;
        }

        for (int pos = 0; pos < Singleton.getInstance().getAppDownloadInfoList().size(); pos++) {
            AppDownloadInfo appDownloadInfo = Singleton.getInstance().getAppDownloadInfo(pos);
            AppInfo appInfo = appDownloadInfo.getAppInfo();
            if (appInfo.getPrimaryUrl() != null) {
                if (appDownloadInfo != null && appInfo != null && appDownloadInfo.isSelected()) {
                    downloadStart(appInfo.getName());
                } else {
                    appNoDownloadAdapter.add(appDownloadInfo);
                }
            }
        }
    }
}