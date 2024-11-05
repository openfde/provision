package com.android.oobe.application;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.oobe.BaseSonFragment;
import com.android.oobe.ButtonTextEvent;
import com.android.oobe.EventBusUtils;
import com.android.oobe.LanguageActivity;
import com.android.oobe.R;
import com.android.oobe.Utils;
import com.android.oobe.application.model.AppDownloadInfo;
import com.android.oobe.application.model.AppInfo;
import com.android.oobe.application.model.RequestStatus;
import com.android.oobe.application.net.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AppOptionFragment extends BaseSonFragment {

    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;
    public static final int ERROR = 3;

    public static final Boolean IS_SELECTED = true;
    public static final Boolean IS_INITIATED = true;

    private final String TAG = "AppOptionFragment";
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ConstraintLayout networkErrorLayout;

    private final Singleton singleton = Singleton.getInstance();
    private AppAdapter appAdapter = new AppAdapter();

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void dispatchMessage(@NonNull Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case SUCCESS:
                    appAdapter.setAppDownloadInfoList(singleton.getAppDownloadInfoList());
                    recyclerView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                case FAILURE:
                case ERROR:
                    recyclerView.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    networkErrorLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    public AppOptionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View myLayout = inflater.inflate(R.layout.layout_application_option, container, false);
        progressBar = myLayout.findViewById(R.id.loading);
        networkErrorLayout = myLayout.findViewById(R.id.networkErrorLayout);
        recyclerView = myLayout.findViewById(R.id.application_recycler_view);
        getAppInfoList();

        int spanCount = 3;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this.getContext(), spanCount);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(appAdapter);
        return myLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        networkErrorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAppInfoList();
            }
        });
    }

    private void getAppInfoList() {
        if (!singleton.hasNetworkRequestSucceeded()) {
            progressBar.setVisibility(View.VISIBLE);
            networkErrorLayout.setVisibility(View.INVISIBLE);
            HttpUtils.get(HttpUtils.APP_INFO_URL, new HttpUtils.HttpCallback() {
                @Override
                public void onResponse(okhttp3.Response response) {
                    try {
                        String jsonResponse = response.body().string();
                        Gson gson = new Gson();
                        Type appInfoListType = new TypeToken<ArrayList<AppInfo>>() {
                        }.getType();
                        List<AppInfo> appInfoList = gson.fromJson(jsonResponse, appInfoListType);
                        List<AppDownloadInfo> appDownloadInfoList = new ArrayList<>();
                        for (AppInfo appInfo : appInfoList) {
                            appDownloadInfoList.add(new AppDownloadInfo(appInfo, IS_SELECTED, Utils.base64ToBitmap(appInfo.getIconString())));
                        }
                        singleton.setAppDownloadInfoList(appDownloadInfoList);
                        singleton.setRequestStatus(RequestStatus.REQUEST_SUCCESS);

                        handler.sendMessage(handler.obtainMessage(SUCCESS));
                        EventBusUtils.sendButtonTextEvent(new ButtonTextEvent(getString(R.string.start_download)));
                    } catch (Exception e) {
                        singleton.setRequestStatus(RequestStatus.REQUEST_FAILED);
                        Log.e(TAG, "http onResponse exception = " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "http failure exception = " + e.getMessage());
                    handler.sendMessage(handler.obtainMessage(FAILURE));
                    EventBusUtils.sendButtonTextEvent(new ButtonTextEvent(getString(R.string.done_button_text)));
                }

                @Override
                public void onError(IOException e) {
                    Log.e(TAG, "http error exception = " + e.getMessage());
                    handler.sendMessage(handler.obtainMessage(ERROR));
                    EventBusUtils.sendButtonTextEvent(new ButtonTextEvent(getString(R.string.done_button_text)));
                }
            });
        } else {
            handler.sendMessage(handler.obtainMessage(SUCCESS));
        }
    }
}