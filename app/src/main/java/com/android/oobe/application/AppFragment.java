package com.android.oobe.application;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.android.oobe.R;

/**
 * The AppFragment class manages AppOptionFragment and AppDownloadFragment.
 * It tracks the currently displayed Fragment using the state variable and switches between selection and download states.
 */
public class AppFragment extends Fragment {
    private final String TAG = "AppFragment";
    private final int APP_CHOOSE_STATE = 0;
    private final int APP_DOWNLOAD_STATE = 1;
    private final Singleton singleton = Singleton.getInstance();
    private final String APP_CHOOSE_STR = "appChoose", APP_DOWNLOAD_STR = "appDownload";
    private int state = APP_CHOOSE_STATE;
    FrameLayout frameLayout;
    AppOptionFragment appOptionFragment = new AppOptionFragment();
    AppDownloadFragment appDownloadFragment;

    public AppFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.layout_app, container, false);
        frameLayout = view.findViewById(R.id.frameLayout);
        switch (state) {
            case APP_CHOOSE_STATE:
                gotoAppChooseFragment();
                break;
            case APP_DOWNLOAD_STATE:
                gotoAppDownloadFragment();
        }
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public boolean isDownloadFragment() {
        return state == APP_DOWNLOAD_STATE;
    }

    private void gotoAppChooseFragment() {
        state = APP_CHOOSE_STATE;
        if (getChildFragmentManager().findFragmentByTag(APP_CHOOSE_STR) != null) {
        } else if (appOptionFragment == null) {
            appOptionFragment = new AppOptionFragment();
        }
        getChildFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.frameLayout, appOptionFragment, APP_CHOOSE_STR)
                .addToBackStack(null)
                .commit();
    }

    public void gotoAppDownloadFragment() {
        state = APP_DOWNLOAD_STATE;
        if (getChildFragmentManager().findFragmentByTag(APP_DOWNLOAD_STR) != null) {
        } else if (appDownloadFragment == null) {
            appDownloadFragment = new AppDownloadFragment();
        }
        getChildFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.frameLayout, appDownloadFragment, APP_DOWNLOAD_STR)
                .addToBackStack(null)
                .commit();
    }
}