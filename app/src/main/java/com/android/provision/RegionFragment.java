package com.android.provision;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.indexablerv.IndexableLayout;

public class RegionFragment extends Fragment {
    private String TAG = "RegionFragment";
    private EditText mSearchEdt;
    private final int PADDING = 94;
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
        //You must set up the adapter first, then set up the data
        mIndexAbleLayout.setAdapter(mRegionAdapter);
        mRegionAdapter.setDatas(transform(TimeZoneProvider.getRegionInfoList(getContext())));

        int color = ContextCompat.getColor(getContext(), R.color.next_button_color);
        mIndexAbleLayout.setOverlayStyle_MaterialDesign(color);

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRegionSearchAdapter = new RegionSearchAdapter(transform(TimeZoneProvider.getRegionInfoList(getContext())), languageListener);
        mRecyclerView.setHasFixedSize(true);
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
        Utils.setFragmentPadding(view, 0, 0, 0, Utils.PADDING);
    }

    public void setLanguageListener(LanguageActivity.LanguageListener languageListener) {
        this.languageListener = languageListener;
        Log.w(TAG, "regionAdapter" + mRegionAdapter);
        mRegionAdapter.setLanguageListener(languageListener);
    }


    private List<RegionEntity> transform(List<RegionInfo> regionInfoList) {
        List<RegionEntity> regionEntityList = new ArrayList<>();
        Log.w(TAG, "regionInfoList.size() = " + regionInfoList.size());
        for (RegionInfo regionInfo : regionInfoList) {
            regionEntityList.add(new RegionEntity(regionInfo));
        }
        return regionEntityList;
    }
}