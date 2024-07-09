package com.android.provision;

import static com.android.provision.LanguageActivity.ADD_LOCALE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocaleStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressLint("ValidFragment")
public class LocaleListEditFragment extends Fragment {

    private LocaleDragAndDropAdapter mAdapter;
    private LanguageActivity.LanguageListener languageListener;
    private View mAddLanguage, mRemoveModeView, mRemoveLanguageView;
    private final String TAG = "LocaleListEditFragment";
    private final int PADDING = 94;

    private boolean mRemoveMode;
    private boolean mShowingRemoveDialog;
    private boolean mIsUiRestricted;


    public LocaleListEditFragment() {
    }

    public LocaleListEditFragment(LanguageActivity.LanguageListener languageListener) {
        this.languageListener = languageListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: savedInstanceState:" + savedInstanceState + "");
        LocaleStore.fillCache(getContext());
        final List<LocaleStore.LocaleInfo> feedsList = getUserLocaleList();
        mAdapter = new LocaleDragAndDropAdapter(getContext(), feedsList);
    }

    private List<LocaleStore.LocaleInfo> getUserLocaleList() {
        final List<LocaleStore.LocaleInfo> result = new ArrayList<>();
        LocaleList list = LocalePicker.getLocales();
        for (int i = 0; i < list.size(); i++) {
            Locale locale = list.get(i);
            result.add(LocaleStore.getLocaleInfo(locale));
        }
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        languageListener.showAndHideButton(View.VISIBLE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().setPadding(0, 0, 0, PADDING);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View myLayout = inflater.inflate(R.layout.locale_order_list, container, false);
        configureDragAndDrop(myLayout);
        Bundle arguments = getArguments();
        if (arguments != null) {
            LocaleStore.LocaleInfo localeInfo = (LocaleStore.LocaleInfo) arguments.get(ADD_LOCALE);
            Log.d(TAG, "onCreateView add localeInfo: " + localeInfo);
            mAdapter.addLocale(localeInfo);
        }
        return myLayout;
    }

    private void configureDragAndDrop(View view) {
        final RecyclerView list = view.findViewById(R.id.dragList);
        final LinearLayoutManager llm = new LinearLayoutManager(getContext());
        list.setLayoutManager(llm);
        list.setHasFixedSize(true);
        mAdapter.setRecyclerView(list);
        list.setAdapter(mAdapter);
        mAddLanguage = view.findViewById(R.id.add_language);
        mRemoveModeView = view.findViewById(R.id.remove_mode);
        mRemoveLanguageView = view.findViewById(R.id.remove_language);
        mAddLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageListener.addLanguage();
            }
        });
        mRemoveModeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRemoveMode(true);
            }
        });

        mRemoveLanguageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int checkedCount = mAdapter.getCheckedCount();
                if (checkedCount == 0) {
                    setRemoveMode(false);
                    return;
                }
                if (checkedCount == mAdapter.getItemCount()) {
                    mShowingRemoveDialog = true;
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.dlg_remove_locales_error_title).setMessage(R.string.dlg_remove_locales_error_message).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mShowingRemoveDialog = false;
                        }
                    }).create().show();
                    return;
                }
                mShowingRemoveDialog = true;
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                if (mAdapter.isFirstLocaleChecked()) {
                    builder.setMessage(R.string.dlg_remove_locales_message);
                }
                final String title = getResources().getQuantityString(R.plurals.dlg_remove_locales_title, checkedCount);
                builder.setTitle(title).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setRemoveMode(false);
                    }
                }).setPositiveButton(R.string.locale_remove_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRemoveMode = false;
                        mShowingRemoveDialog = false;
                        mAdapter.removeChecked();
                        setRemoveMode(false);
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mShowingRemoveDialog = false;
                    }
                }).create().show();
            }
        });
    }

    private void setRemoveMode(boolean mRemoveMode) {
        this.mRemoveMode = mRemoveMode;
        mAdapter.setRemoveMode(mRemoveMode);
        mAddLanguage.setVisibility(mRemoveMode ? View.INVISIBLE : View.VISIBLE);
        mRemoveModeView.setVisibility(mRemoveMode ? View.INVISIBLE : View.VISIBLE);
        mRemoveLanguageView.setVisibility(mRemoveMode ? View.VISIBLE : View.INVISIBLE);
        languageListener.showAndHideButton(mRemoveMode ? View.INVISIBLE : View.VISIBLE);
        // Set padding for the root view
        setFragmentPadding(0, 0, 0, mRemoveMode ? 0 : PADDING);
    }

    private void setFragmentPadding(int left, int top, int right, int bottom) {
        float density = getResources().getDisplayMetrics().density;
        // in pixels
        int paddingLeft = Math.round(left * density);
        int paddingTop = Math.round(top * density);
        int paddingRight = Math.round(right * density);
        int paddingBottom = Math.round(bottom * density);
        getView().setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }
}
