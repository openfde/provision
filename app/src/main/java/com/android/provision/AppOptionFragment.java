package com.android.provision;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Fragment;

import androidx.annotation.Nullable;


import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppOptionFragment extends Fragment {
    public static final String BASIP = "127.0.0.1";
    public static final String BASEURL = "http://" + BASIP + ":18080";
    public static final String URL_GETAPP = null;
    private final String TAG = "AppOptionFragment";
    private RecyclerView mRecyclerView;
    private static final ArrayList<AppListResult.DataBeanX.DataBean> mAppList = new ArrayList<>();
    LanguageActivity.LanguageListener languageListener;

    static {
        // Only one network request is made.
        getApplication();
    }

    public AppOptionFragment() {
        Log.d(TAG, "AppOptionFragment Init");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View myLayout = inflater.inflate(R.layout.layout_application_option, container, false);
        mRecyclerView = (RecyclerView) myLayout.findViewById(R.id.application_recycler_view);
        int spanCount = mAppList.size() <= 1 ? 1 : 3;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this.getContext(), spanCount);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(new AppAdapter(getContext(), mAppList));
        return myLayout;
    }

    private static void getApplication() {
        mAppList.add(new AppListResult.DataBeanX.DataBean());
        mAppList.add(new AppListResult.DataBeanX.DataBean());
        mAppList.add(new AppListResult.DataBeanX.DataBean());
    }

    public void InstallApp() {
        Log.e(TAG, "InstallApplication");
        if (!mAppList.isEmpty()) {
            mAppList.forEach(app -> {
                if (app.isChecked()) {
                }
            });
        }
    }
}