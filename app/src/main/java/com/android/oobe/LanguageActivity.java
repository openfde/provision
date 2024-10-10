package com.android.oobe;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.android.internal.app.LocalePickerWithRegion;
import com.android.internal.app.LocaleStore;
import com.android.internal.policy.DecorView;
import com.android.oobe.application.AppFragment;
import com.android.oobe.application.Singleton;
import com.android.oobe.application.net.DownloadService;
import com.android.oobe.keyboard.VirtualKeyboardFragment;
import com.android.oobe.language.LocaleListEditFragment;
import com.android.oobe.location.GpsSetFragment;
import com.android.oobe.time.RegionFragment;
import com.android.oobe.time.RegionZoneFragment;
import com.android.oobe.time.TimeFragment;
import com.android.oobe.time.TimeZoneFragment;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

public class LanguageActivity extends Activity implements LocalePickerWithRegion.LocaleSelectedListener {

    private static final String TAG = "LanguageActivity";

    private static final int CHOOSE_LANGUAGE = 1;
    private static final int CHOOSE_KEYBOARD = 2;
    private static final int CHOOSE_LOCATION = 3;
    private static final int CHOOSE_TIME = 4;
    private static final int CHOOSE_APP = 5;
    private static int state = CHOOSE_LANGUAGE;

    private TextView mLanguageTitle, mLanguageHint;
    private Button mPrevBtn, mNextBtn, mReturn;
    private LinearLayout direction1, direction2, direction3, direction4, direction5;

    private Fragment localeListEditor, localeListAdd, virtualKeyboard, appFragment, gpsSetFragment,
            timeFragment, timeZoneFragment, regionFragment, regionZoneFragment;

    public static final String ADD_LOCALE = "addLocale";
    private String LOCALE_LIST_EDITOR = "localeListEditor", LOCALE_LIST_ADD = "localeListAdd", VIRTUAL_KEYBOARD = "virtualKeyboard",
            GPS_ADD = "gpsAdd", APP = "app", TIME = "time", TIME_ZONE = "timeZone", REGION = "region", REGION_ZONE = "regionZone";

    private Handler handler = new Handler(Looper.getMainLooper());
    private Singleton singleton = Singleton.getInstance();
    private DownloadService downloadService;

    private LanguageListener languageListener = new LanguageListener() {
        @Override
        public void languageChanged(Locale locale) {
        }

        @Override
        public void addLanguage() {
            gotoAddFragment();
        }

        @Override
        public void addTimeZone() {
            gotoTimeZoneFragment();
        }

        @Override
        public void addRegion() {
            gotoRegionFragment();
        }

        @Override
        public void addRegionZone(String regionId) {
            gotoRegionZoneFragment(regionId);
        }


        @Override
        public void showAndHideButton(int visibility) {
            showAndHidePrevButton(visibility);
            showAndHideNextButton(visibility);
        }

        @Override
        public void showAndHidePrevButton(int visibility) {
            mPrevBtn.setVisibility(visibility);
        }

        @Override
        public void showAndHideNextButton(int visibility) {
            mNextBtn.setVisibility(visibility);
        }

        public void showAndHideReturnButton(int visibility) {
            mReturn.setVisibility(visibility);
        }

        @Override
        public void backToTime() {
            goBackToTimeFragment();
        }

    };


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof DownloadService.DownloadBinder) {
                downloadService = ((DownloadService.DownloadBinder) service).getService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intentBroadcast = new Intent("com.fde.SYSTEM_INIT_ACTION");
        intentBroadcast.setPackage("com.boringdroid.systemui");
        sendBroadcast(intentBroadcast);

        Intent intentService = new Intent(this, DownloadService.class);
        startService(intentService);
        bindService(intentService, connection, Context.BIND_AUTO_CREATE);

        fetchDataPeriodically(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DecorView decorView = (DecorView) getWindow().getDecorView();
        decorView.startFullScreenWindow();

        EventBusUtils.register(this);

        setContentView(R.layout.activity_language);
        mLanguageTitle = findViewById(R.id.language_title);
        mLanguageHint = findViewById(R.id.language_hint);
        direction1 = findViewById(R.id.direction1);
        direction2 = findViewById(R.id.direction2);
        direction3 = findViewById(R.id.direction3);
        direction4 = findViewById(R.id.direction4);
        direction5 = findViewById(R.id.direction5);
        mPrevBtn = findViewById(R.id.prevBtn);
        mNextBtn = findViewById(R.id.nextBtn);
        mReturn = findViewById(R.id.returnBtn);

        switchListen();
        gotoFragment();

    }

    public DownloadService getDownloadService() {
        return downloadService;
    }

    @Override
    protected void onDestroy() {
        EventBusUtils.unregister(this);
        Intent intentService = new Intent(this, DownloadService.class);
        stopService(intentService);
        unbindService(connection);
        super.onDestroy();
    }

    private void fetchDataPeriodically(Context context) {
        final Runnable fetchTask = new Runnable() {
            @Override
            public void run() {
                if (DataFetcher.isAvailable(context)) {
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.activity_in);
                    findViewById(R.id.provisionRelativeLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.provisionRelativeLayout).startAnimation(animation);
                    handler.removeCallbacks(this);
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(fetchTask);
    }

    @Subscribe
    public void onNextButtonTextEvent(ButtonTextEvent buttonTextEvent) {
        String message = buttonTextEvent.getMessage();
        mNextBtn.setText(message);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void gotoEditFragment(LocaleStore.LocaleInfo localeInfo) {
        if (getFragmentManager().findFragmentByTag(LOCALE_LIST_EDITOR) != null && localeInfo != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(ADD_LOCALE, localeInfo);
            localeListEditor.setArguments(bundle);
        } else if (localeListEditor == null) {
            localeListEditor = new LocaleListEditFragment(languageListener);
        }
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, localeListEditor, LOCALE_LIST_EDITOR)
                .commit();
    }

    @Override
    public void onLocaleSelected(LocaleStore.LocaleInfo localeInfo) {
        getFragmentManager().popBackStack();
        getFragmentManager().popBackStack();
        gotoEditFragment(localeInfo);
    }

    private void gotoAddFragment() {
        languageListener.showAndHideButton(View.GONE);
        languageListener.showAndHideReturnButton(View.VISIBLE);
        if (localeListAdd == null) {
            localeListAdd = LocalePickerWithRegion.createLanguagePicker(this, this, false /* translate only */);
        }
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, localeListAdd, LOCALE_LIST_ADD)
                .addToBackStack(null)
                .commit();
    }

    private void gotoVirtualKeyboard() {
        if (getFragmentManager().findFragmentByTag(VIRTUAL_KEYBOARD) != null) {
        } else if (virtualKeyboard == null) {
            virtualKeyboard = new VirtualKeyboardFragment(languageListener);
        }
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, virtualKeyboard, VIRTUAL_KEYBOARD)
                .addToBackStack(null)
                .commit();
    }

    private void gotoGpsFragment() {
        if (getFragmentManager().findFragmentByTag(GPS_ADD) != null) {
        } else if (gpsSetFragment == null) {
            gpsSetFragment = new GpsSetFragment();
        }
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, gpsSetFragment, GPS_ADD)
                .addToBackStack(null)
                .commit();
    }

    private void gotoTimeFragment() {
        if (getFragmentManager().findFragmentByTag(TIME) != null) {
        } else if (timeFragment == null) {
            timeFragment = new TimeFragment();
        }
        ((TimeFragment) timeFragment).setLanguageListener(languageListener);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, timeFragment, TIME)
                .addToBackStack(TIME)
                .commit();
    }

    private void gotoTimeZoneFragment() {
        languageListener.showAndHideButton(View.GONE);
        languageListener.showAndHideReturnButton(View.VISIBLE);
        if (getFragmentManager().findFragmentByTag(TIME_ZONE) != null) {
        } else if (timeZoneFragment == null) {
            timeZoneFragment = new TimeZoneFragment();
        }
        ((TimeZoneFragment) timeZoneFragment).setLanguageListener(languageListener);
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, timeZoneFragment, TIME_ZONE)
                .addToBackStack(null)
                .commit();
    }

    private void gotoRegionFragment() {
        if (getFragmentManager().findFragmentByTag(REGION) != null) {
        } else if (regionFragment == null) {
            regionFragment = new RegionFragment();
        }
        ((RegionFragment) regionFragment).setLanguageListener(languageListener);
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, regionFragment, REGION)
                .addToBackStack(null)
                .commit();
    }

    private void gotoRegionZoneFragment(String regionId) {
        if (getFragmentManager().findFragmentByTag(REGION_ZONE) != null) {
        } else if (regionZoneFragment == null) {
            regionZoneFragment = new RegionZoneFragment();
        }
        ((RegionZoneFragment) regionZoneFragment).setRegionId(regionId);
        ((RegionZoneFragment) regionZoneFragment).setLanguageListener(languageListener);
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, regionZoneFragment, REGION_ZONE)
                .addToBackStack(null)
                .commit();
    }

    private void goBackToTimeFragment() {
        getFragmentManager().popBackStack(TIME, 0);
        mReturn.setVisibility(View.GONE);
        setView();
    }

    private void gotoAppFragment() {
        if (getFragmentManager().findFragmentByTag(APP) != null) {
        } else if (appFragment == null) {
            appFragment = new AppFragment();
        }
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_right_in, R.animator.slide_left_out, R.animator.slide_left_in, R.animator.slide_right_out)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.content, appFragment, APP)
                .addToBackStack(null)
                .commit();
    }

    public void switchListen() {
        mPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state <= CHOOSE_LANGUAGE) return;
                state--;
                getFragmentManager().popBackStack();
                setView();
            }
        });
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state >= CHOOSE_LANGUAGE && state <= CHOOSE_TIME) {
                    state++;
                    gotoFragment();
                } else if (state == CHOOSE_APP && singleton.hasNetworkRequestBeenInitiated()) {//Provision end, Start launcher
                    if (StringUtils.equals(mNextBtn.getText(), getString(R.string.done_button_text))) {
                        if (singleton.isNothingDownload()) {
                            finishSetUpWizard();
                        } else {
                            showConfirmationDialog();
                        }
                    } else {
                        ((AppFragment) appFragment).gotoAppDownloadFragment();
                        mNextBtn.setText(getString(R.string.done_button_text));
                    }
                }
            }
        });
        mReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                Fragment fragment = getFragmentManager().findFragmentById(R.id.content);
                if (fragment == localeListEditor)
                    ((LocaleListEditFragment) localeListEditor).setRemoveMode(false);
            }
        });
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.content);
                if (fragment == localeListEditor || fragment == timeFragment) {
                    setView();
                }
            }
        });
    }

    public void gotoFragment() {
        setView();
        switch (state) {
            case CHOOSE_LANGUAGE:
                gotoEditFragment(null);
                break;
            case CHOOSE_KEYBOARD:
                gotoVirtualKeyboard();
                break;
            case CHOOSE_LOCATION:
                gotoGpsFragment();
                break;
            case CHOOSE_TIME:
                gotoTimeFragment();
                break;
            case CHOOSE_APP:
                gotoAppFragment();
        }
    }

    private void finishSetUpWizard() {

        Settings.Global.putString(getContentResolver(), Settings.Global.DEVICE_NAME, "OpenFDE device");
        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        Settings.Secure.putInt(getContentResolver(), "user_setup_complete", 1);

        // remove this activity from the package manager.
        PackageManager pm = getPackageManager();
        ComponentName name = new ComponentName(this, LanguageActivity.class);
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        finish();
    }

    public void setView() {
        if (state == CHOOSE_LANGUAGE) {
            // Set directionColor
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_color);
            direction3.setBackgroundResource(R.color.direction_color);
            direction4.setBackgroundResource(R.color.direction_color);
            direction5.setBackgroundResource(R.color.direction_color);

            // Set prevButton and nextButton
            ViewGroup.LayoutParams params = findViewById(R.id.nextBtn).getLayoutParams();
            params.width = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    300,
                    this.getResources().getDisplayMetrics()
            );
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    44,
                    this.getResources().getDisplayMetrics()
            );
            mReturn.setVisibility(View.GONE);
            mPrevBtn.setVisibility(View.GONE);
            mNextBtn.setVisibility(View.VISIBLE);
            mNextBtn.setLayoutParams(params);
            mNextBtn.setBackgroundResource(R.drawable.next_button_init);
            mNextBtn.setText(R.string.next_button_text);

            // Set Panel
            mLanguageTitle.setText(R.string.language_panel_text);

            // Set Language Hint
            mLanguageHint.setVisibility(View.VISIBLE);
        } else if (state == CHOOSE_KEYBOARD) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_color);
            direction4.setBackgroundResource(R.color.direction_color);
            direction5.setBackgroundResource(R.color.direction_color);

            ViewGroup.LayoutParams params = findViewById(R.id.nextBtn).getLayoutParams();
            params.width = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    199,
                    this.getResources().getDisplayMetrics()
            );
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    44,
                    this.getResources().getDisplayMetrics()
            );
            mReturn.setVisibility(View.GONE);
            mPrevBtn.setVisibility(View.VISIBLE);
            mNextBtn.setVisibility(View.VISIBLE);
            mNextBtn.setLayoutParams(params);
            mNextBtn.setBackgroundResource(R.drawable.next_button);
            mNextBtn.setText(R.string.next_button_text);

            mLanguageTitle.setText(R.string.keyboard_panel_text);

            mLanguageHint.setVisibility(View.INVISIBLE);
        } else if (state == CHOOSE_LOCATION) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_marked_color);
            direction4.setBackgroundResource(R.color.direction_color);
            direction5.setBackgroundResource(R.color.direction_color);

            mReturn.setVisibility(View.GONE);
            mPrevBtn.setVisibility(View.VISIBLE);
            mNextBtn.setVisibility(View.VISIBLE);
            mNextBtn.setText(R.string.next_button_text);

            mLanguageTitle.setText(R.string.location_panel_text);
        } else if (state == CHOOSE_TIME) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_marked_color);
            direction4.setBackgroundResource(R.color.direction_marked_color);
            direction5.setBackgroundResource(R.color.direction_color);

            mReturn.setVisibility(View.GONE);
            mPrevBtn.setVisibility(View.VISIBLE);
            mNextBtn.setVisibility(View.VISIBLE);
            mNextBtn.setText(R.string.next_button_text);

            mLanguageTitle.setText(R.string.time_panel_text);
        } else if (state == CHOOSE_APP) {
            direction1.setBackgroundResource(R.color.direction_marked_color);
            direction2.setBackgroundResource(R.color.direction_marked_color);
            direction3.setBackgroundResource(R.color.direction_marked_color);
            direction4.setBackgroundResource(R.color.direction_marked_color);
            direction5.setBackgroundResource(R.color.direction_marked_color);

            mReturn.setVisibility(View.GONE);
            mPrevBtn.setVisibility(View.VISIBLE);
            mNextBtn.setVisibility(View.VISIBLE);
            if ((singleton.hasNetworkRequestSucceeded() && singleton.isNothingSelected()) || (appFragment != null && ((AppFragment) appFragment).isDownloadFragment())) {
                mNextBtn.setText(R.string.done_button_text);
            } else {
                mNextBtn.setText(R.string.start_download);
            }

            mLanguageTitle.setText(R.string.application_panel_text);
        }
    }

    private void showConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog, null);

        TextView downloadTxt = dialogView.findViewById(R.id.tv_download);
        TextView proceedTxt = dialogView.findViewById(R.id.tv_proceed);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.215);
        params.gravity = Gravity.CENTER;
        params.y = 25;
        dialog.getWindow().setAttributes(params);

        downloadTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        proceedTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finishSetUpWizard();
            }
        });

    }

    public interface LanguageListener {

        void languageChanged(Locale locale);

        void addLanguage();

        void addTimeZone();

        void addRegion();

        void addRegionZone(String regionId);

        void showAndHidePrevButton(int visibility);

        void showAndHideNextButton(int visibility);

        void showAndHideButton(int visibility);

        void showAndHideReturnButton(int visibility);

        void backToTime();
    }
}