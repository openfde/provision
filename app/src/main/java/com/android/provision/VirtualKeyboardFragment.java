package com.android.provision;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;


import com.android.settingslib.inputmethod.InputMethodAndSubtypeUtilCompat;
import com.android.settingslib.inputmethod.InputMethodPreference;
import com.android.settingslib.inputmethod.InputMethodSettingValuesWrapper;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class VirtualKeyboardFragment extends Fragment  {

    private final ArrayList<InputMethodPreference> mInputMethodPreferenceList = new ArrayList<>();
    private InputMethodSettingValuesWrapper mInputMethodSettingValues;
    private InputMethodManager mImm;
    private DevicePolicyManager mDpm;

    LanguageActivity.LanguageListener languageListener;
    private String TAG = "VirtualKeyboardFragment";

    public VirtualKeyboardFragment(LanguageActivity.LanguageListener languageListener) {
        this.languageListener = languageListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        mInputMethodSettingValues = InputMethodSettingValuesWrapper.getInstance(activity);
        mImm = activity.getSystemService(InputMethodManager.class);
        mDpm = activity.getSystemService(DevicePolicyManager.class);
    }

    @Override
    public View onCreateView( LayoutInflater inflater,  ViewGroup container,Bundle savedInstanceState) {
        final View myLayout = inflater.inflate(R.layout.layout_keyboard, container, false);
        return myLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
        updateInputMethodPreferenceViews();
    }

    private void updateInputMethodPreferenceViews() {
        //TODO show all keyboard
        mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
        // Clear existing "InputMethodPreference"s
        mInputMethodPreferenceList.clear();
        List<String> permittedList = null; //= mDpm.getPermittedInputMethodsForCurrentUser();
//        final Context context = getPrefContext();
        final List<InputMethodInfo> imis = mInputMethodSettingValues.getInputMethodList();
        final int numImis = (imis == null ? 0 : imis.size());
        Context context = getContext();
        for (int i = 0; i < numImis; ++i) {
            final InputMethodInfo imi = imis.get(i);
            final boolean isAllowedByOrganization = permittedList == null
                    || permittedList.contains(imi.getPackageName());
            CharSequence label = imi.loadLabel(context.getPackageManager());
            boolean alwaysCheckedIme = mInputMethodSettingValues.isAlwaysCheckedIme(imi);
            boolean enabledImi = mInputMethodSettingValues.isEnabledImi(imi);
            Log.d(TAG, "updateInputMethodPreferenceViews: ---------------");
            Log.d(TAG, "updateInputMethodPreferenceViews: label:" + label);
            Log.d(TAG, "updateInputMethodPreferenceViews: alwaysCheckedIme:" + alwaysCheckedIme);
            Log.d(TAG, "updateInputMethodPreferenceViews: enabledImi:" + enabledImi);
//            final InputMethodPreference pref = new InputMethodPreference(
//                    context, imi, true, isAllowedByOrganization, null);
//            pref.setIcon(imi.loadIcon(context.getPackageManager()));
//            mInputMethodPreferenceList.add(pref);
        }
        final Collator collator = Collator.getInstance();
//        mInputMethodPreferenceList.sort((lhs, rhs) -> lhs.compareTo(rhs, collator));
//        getPreferenceScreen().removeAll();
        for (int i = 0; i < numImis; ++i) {
//            final InputMethodPreference pref = mInputMethodPreferenceList.get(i);
//            pref.setOrder(i);
//            getPreferenceScreen().addPreference(pref);
//            InputMethodAndSubtypeUtilCompat.removeUnnecessaryNonPersistentPreference(pref);
//            pref.updatePreferenceViews();
        }
    }

    public void onSaveInputMethodPreference(InputMethodInfo inputMethodInfo) {
        //TODO open keyboard
//        final boolean hasHardwareKeyboard = getResources().getConfiguration().keyboard
//                == Configuration.KEYBOARD_QWERTY;
//        InputMethodAndSubtypeUtilCompat.saveInputMethodSubtypeList(this, getContentResolver(),
//                mImm.getInputMethodList(), hasHardwareKeyboard);
//        // Update input method settings and preference list.
//        mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
//        for (final InputMethodPreference p : mInputMethodPreferenceList) {
//            p.updatePreferenceViews();
//        }
    }

    public interface OnSavePreferenceListener {
        void onSaveInputMethodPreference(InputMethodInfo inputMethodInfo);
    }
}
