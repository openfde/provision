package com.android.oobe.time;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.oobe.LanguageActivity;
import com.android.oobe.R;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.indexablerv.IndexableLayout;

/**
 * RegionFragment is an Android Fragment that displays a list of regions, allowing users to search for and select specific regions.
 * It uses IndexableLayout to list regions alphabetically, while a search feature dynamically filters the list based on user input.
 * When a user types into the search field, RecyclerView displays only matching items, hiding the full list in IndexableLayout.
 * RegionFragment also includes a LanguageListener to manage interactions with region selections.
 */
public class RegionFragment extends Fragment {
    private String TAG = "RegionFragment";
    private EditText mSearchEdt;
    private RegionAdapter mRegionAdapter = new RegionAdapter();
    private IndexableLayout mIndexAbleLayout;
    private RecyclerView mRecyclerView;

    private RegionSearchAdapter mRegionSearchAdapter;

    private LanguageActivity.LanguageListener languageListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_region, container, false);
        mSearchEdt = view.findViewById(R.id.searchEdt);

        mIndexAbleLayout = view.findViewById(R.id.indexAbleLayout);
        mIndexAbleLayout.setLayoutManager(new LinearLayoutManager(getContext()));
        // You must set up the adapter first, then set up the data
        mIndexAbleLayout.setAdapter(mRegionAdapter);
        mRegionAdapter.setDatas(transform(TimeZoneProvider.getRegionInfoList(getContext())));

        int color = ContextCompat.getColor(getContext(), R.color.next_button_color);
        mIndexAbleLayout.setOverlayStyle_MaterialDesign(color);

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRegionSearchAdapter = new RegionSearchAdapter(transform(TimeZoneProvider.getRegionInfoList(getContext())), languageListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRegionSearchAdapter);

        mSearchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString().trim();
                if (!TextUtils.isEmpty(str)) {
                    mIndexAbleLayout.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRegionSearchAdapter.search(str.toLowerCase());
                } else {
                    mIndexAbleLayout.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setLanguageListener(LanguageActivity.LanguageListener languageListener) {
        this.languageListener = languageListener;
        mRegionAdapter.setLanguageListener(languageListener);
    }

    private List<RegionEntity> transform(List<RegionInfo> regionInfoList) {
        List<RegionEntity> regionEntityList = new ArrayList<>();
        for (RegionInfo regionInfo : regionInfoList) {
            regionEntityList.add(new RegionEntity(regionInfo));
        }
        return regionEntityList;
    }
}