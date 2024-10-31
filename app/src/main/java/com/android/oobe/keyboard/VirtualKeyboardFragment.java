package com.android.oobe.keyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.widget.LinearLayoutManager;
import com.android.internal.widget.RecyclerView;
import com.android.oobe.LanguageActivity;
import com.android.oobe.R;
import com.android.oobe.language.ToggleSwitch;
import com.android.settingslib.inputmethod.InputMethodPreference;
import com.android.settingslib.inputmethod.InputMethodSettingValuesWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@SuppressLint("ValidFragment")
public class VirtualKeyboardFragment extends Fragment {

    private final ArrayList<InputMethodPreference> mInputMethodPreferenceList = new ArrayList<>();
    private InputMethodSettingValuesWrapper mInputMethodSettingValues;
    private InputMethodManager mImm;
    private DevicePolicyManager mDpm;

    LanguageActivity.LanguageListener languageListener;
    private static final String TAG = "VirtualKeyboardFragment";

    private static final boolean DEBUG = true;
    private static final String SUBTYPE_MODE_KEYBOARD = "keyboard";
    private static final char INPUT_METHOD_SEPARATER = ':';
    private static final char INPUT_METHOD_SUBTYPE_SEPARATER = ';';
    private static final int NOT_A_SUBTYPE_ID = -1;

    private RecyclerView mRecyclerView;

    private static final TextUtils.SimpleStringSplitter sStringInputMethodSplitter
            = new TextUtils.SimpleStringSplitter(INPUT_METHOD_SEPARATER);

    private static final TextUtils.SimpleStringSplitter sStringInputMethodSubtypeSplitter
            = new TextUtils.SimpleStringSplitter(INPUT_METHOD_SUBTYPE_SEPARATER);

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View myLayout = inflater.inflate(R.layout.layout_keyboard, container, false);
        mRecyclerView = (RecyclerView) myLayout.findViewById(R.id.keyboard_recycler_view);
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
        final List<InputMethodInfo> imis = mInputMethodSettingValues.getInputMethodList();
        initInputMethodList(imis);
    }

    private void initInputMethodList(List<InputMethodInfo> imis) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new RecyclerView.Adapter<InputMethodViewHolder>() {
            @Override
            public InputMethodViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_inputmethod, viewGroup, false);
                InputMethodViewHolder holder = new InputMethodViewHolder(view);
                return holder;
            }

            @Override
            public void onBindViewHolder(InputMethodViewHolder holder, int position) {
                InputMethodInfo inputMethodInfo = imis.get(position);
                CharSequence label = inputMethodInfo.loadLabel(getContext().getPackageManager());
                boolean alwaysCheckedIme = mInputMethodSettingValues.isAlwaysCheckedIme(inputMethodInfo);
                boolean enabledImi = mInputMethodSettingValues.isEnabledImi(inputMethodInfo);
                Drawable drawable = inputMethodInfo.loadIcon(getContext().getPackageManager());
                holder.icon.setImageDrawable(drawable);
                holder.title.setText(label);
                holder.toggleSwitch.setChecked(enabledImi);
                if (alwaysCheckedIme) {
                    holder.toggleSwitch.setEnabled(true);
                    holder.toggleSwitch.setEnabled(false);
                    holder.itemView.setClickable(false);
                    holder.title.setTextColor(getResources().getColor(R.color.connected_state_color));
                } else {
                    holder.toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            updateInputMethodEnable(inputMethodInfo, isChecked);
                        }
                    });
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!alwaysCheckedIme) {
                                boolean checked = holder.toggleSwitch.isChecked();
                                holder.toggleSwitch.setChecked(!checked);
                            }
                        }
                    });
                }

            }

            @Override
            public int getItemCount() {
                return imis.size();
            }
        });
    }

    private void updateInputMethodEnable(InputMethodInfo inputMethodInfo, boolean isChecked) {
        if (isChecked) setInputMethodDefault(inputMethodInfo);
        String id = inputMethodInfo.getId();
        final HashMap<String, HashSet<String>> enabledIMEsAndSubtypesMap =
                getEnabledInputMethodsAndSubtypeList(getContext().getContentResolver());
        if (enabledIMEsAndSubtypesMap.containsKey(id) && !isChecked) {
            enabledIMEsAndSubtypesMap.remove(id);
        } else if (isChecked && !enabledIMEsAndSubtypesMap.containsKey(id)) {
            enabledIMEsAndSubtypesMap.put(id, new HashSet<String>());
        }
        String textImiString = buildInputMethodsAndSubtypesString(enabledIMEsAndSubtypesMap);
        Settings.Secure.putString(getContext().getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS, textImiString);
        setInputMethodDefault(isChecked ? inputMethodInfo : getAlwaysCheckedInputMethod());
    }

    private InputMethodInfo getAlwaysCheckedInputMethod() {
        InputMethodInfo alwaysCheckedMethod = null;
        List<InputMethodInfo> inputMethodList = mInputMethodSettingValues.getInputMethodList();

        for (InputMethodInfo inputMethodInfo : inputMethodList) {
            if (mInputMethodSettingValues.isAlwaysCheckedIme(inputMethodInfo)) {
                alwaysCheckedMethod = inputMethodInfo;
            }
        }
        return alwaysCheckedMethod;

    }

    private void setInputMethodDefault(InputMethodInfo inputMethodInfo) {
        String id = inputMethodInfo.getId();
        final HashMap<String, HashSet<String>> enabledIMEsAndSubtypesMap =
                getEnabledInputMethodsAndSubtypeList(getContext().getContentResolver());
        HashSet<String> stringHashSet = enabledIMEsAndSubtypesMap.get(id);

        if (stringHashSet != null) {
            enabledIMEsAndSubtypesMap.clear();
            enabledIMEsAndSubtypesMap.put(id, stringHashSet);
            String textImiString = buildInputMethodsAndSubtypesString(enabledIMEsAndSubtypesMap);
            Settings.Secure.putString(getContext().getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD, textImiString);
        }
    }

    public class InputMethodViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title;
        ToggleSwitch toggleSwitch;


        public InputMethodViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.icon);
            title = (TextView) view.findViewById(R.id.title);
            toggleSwitch = (ToggleSwitch) view.findViewById(R.id.switch_toggle);
        }
    }

    public static String buildInputMethodsAndSubtypesString(
            final HashMap<String, HashSet<String>> imeToSubtypesMap) {
        final StringBuilder builder = new StringBuilder();
        for (final String imi : imeToSubtypesMap.keySet()) {
            if (builder.length() > 0) {
                builder.append(INPUT_METHOD_SEPARATER);
            }
            final HashSet<String> subtypeIdSet = imeToSubtypesMap.get(imi);
            builder.append(imi);
            for (final String subtypeId : subtypeIdSet) {
                builder.append(INPUT_METHOD_SUBTYPE_SEPARATER).append(subtypeId);
            }
        }
        return builder.toString();
    }


    private static HashMap<String, HashSet<String>> getEnabledInputMethodsAndSubtypeList(
            ContentResolver resolver) {
        final String enabledInputMethodsStr = Settings.Secure.getString(
                resolver, Settings.Secure.ENABLED_INPUT_METHODS);
        if (DEBUG) {
            Log.d(TAG, "--- Load enabled input methods: " + enabledInputMethodsStr);
        }
        return parseInputMethodsAndSubtypesString(enabledInputMethodsStr);
    }

    public static HashMap<String, HashSet<String>> parseInputMethodsAndSubtypesString(
            final String inputMethodsAndSubtypesString) {
        final HashMap<String, HashSet<String>> subtypesMap = new HashMap<>();
        if (TextUtils.isEmpty(inputMethodsAndSubtypesString)) {
            return subtypesMap;
        }
        sStringInputMethodSplitter.setString(inputMethodsAndSubtypesString);
        while (sStringInputMethodSplitter.hasNext()) {
            final String nextImsStr = sStringInputMethodSplitter.next();
            sStringInputMethodSubtypeSplitter.setString(nextImsStr);
            if (sStringInputMethodSubtypeSplitter.hasNext()) {
                final HashSet<String> subtypeIdSet = new HashSet<>();
                // The first element is {@link InputMethodInfoId}.
                final String imiId = sStringInputMethodSubtypeSplitter.next();
                while (sStringInputMethodSubtypeSplitter.hasNext()) {
                    subtypeIdSet.add(sStringInputMethodSubtypeSplitter.next());
                }
                subtypesMap.put(imiId, subtypeIdSet);
            }
        }
        return subtypesMap;
    }


    private static int getInputMethodSubtypeSelected(ContentResolver resolver) {
        try {
            return Settings.Secure.getInt(resolver,
                    Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE);
        } catch (Settings.SettingNotFoundException e) {
            return -1;
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
