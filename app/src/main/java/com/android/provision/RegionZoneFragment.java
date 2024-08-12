package com.android.provision;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RegionZoneFragment extends Fragment {
    private String TAG = "RegionZoneFragment";
    private String mRegionId;
    private LanguageActivity.LanguageListener mLanguageListener;
    private List<RegionZoneInfo> mRegionZoneInfoList;
    private RegionZoneAdapter mRegionZoneAdapter;
    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_region_zone, container, false);

        mRecyclerView = view.findViewById(R.id.recyclerView);

        mRegionZoneInfoList = TimeZoneProvider.getRegionZoneInfoList(getContext(), mRegionId);
        if (mRegionZoneInfoList.size() == 1) {
            TimeZoneProvider.saveTimeZone(getContext(), mRegionZoneInfoList.get(0).getId());
            mLanguageListener.backToTime();
        }

        mRegionZoneAdapter = new RegionZoneAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRegionZoneAdapter);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setRegionId(String regionId) {
        this.mRegionId = regionId;
    }

    public void setLanguageListener(LanguageActivity.LanguageListener languageListener) {
        this.mLanguageListener = languageListener;
    }

    class RegionZoneAdapter extends RecyclerView.Adapter<RegionZoneAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_region_zone, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String regionZoneName = mRegionZoneInfoList.get(position).getName();
            String summary = mRegionZoneInfoList.get(position).getSummary();
            String currentTime = mRegionZoneInfoList.get(position).getCurrentTime();

            holder.regionZoneNameTxt.setText(regionZoneName);
            holder.summaryTxt.setText(summary);
            holder.currentTimeTxt.setText(currentTime);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimeZoneProvider.saveTimeZone(getContext(), mRegionZoneInfoList.get(position).getId());
                    mLanguageListener.backToTime();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mRegionZoneInfoList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView regionZoneNameTxt;
            private final TextView summaryTxt;
            private final TextView currentTimeTxt;

            public ViewHolder(@NonNull View view) {
                super(view);
                regionZoneNameTxt = view.findViewById(R.id.regionZoneNameTxt);
                summaryTxt = view.findViewById(R.id.summaryTxt);
                currentTimeTxt = view.findViewById(R.id.currentTimeTxt);
            }
        }

    }

}
