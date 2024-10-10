package com.android.oobe.time;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.oobe.LanguageActivity;
import com.android.oobe.R;

import java.util.ArrayList;
import java.util.List;

public class RegionSearchAdapter extends RecyclerView.Adapter<RegionSearchAdapter.RegionSearchViewHolder> implements Filterable {

    List<RegionEntity> mRegionEntityList;
    List<RegionEntity> mRegionFilterEntityList = new ArrayList<>();
    private LanguageActivity.LanguageListener mLanguageListener;

    RegionSearchAdapter(List<RegionEntity> regionEntityList, LanguageActivity.LanguageListener languageListener) {
        mRegionEntityList = regionEntityList;
        mLanguageListener = languageListener;
    }

    @NonNull
    @Override
    public RegionSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RegionSearchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_region_content, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RegionSearchViewHolder holder, int position) {
        RegionEntity regionEntity = mRegionFilterEntityList.get(position);
        holder.regionNameTxt.setText(regionEntity.getRegionName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLanguageListener.addRegionZone(regionEntity.getRegionInfo().getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRegionFilterEntityList.size();
    }

    public void search(String key) {
        getFilter().filter(key);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<RegionEntity> filterRegionEntityList = new ArrayList<>();
                for (RegionEntity regionEntity : mRegionEntityList) {
                    if (regionEntity.getRegionNamePingYin().startsWith(constraint.toString()) || regionEntity.getRegionName().startsWith(constraint.toString())) {
                        filterRegionEntityList.add(regionEntity);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.count = filterRegionEntityList.size();
                filterResults.values = filterRegionEntityList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<RegionEntity> filterRegionEntityList = (ArrayList<RegionEntity>) results.values;
//                You must retain references to the original data source
                mRegionFilterEntityList.clear();
                mRegionFilterEntityList.addAll(filterRegionEntityList);
                notifyDataSetChanged();

            }
        };
    }

    class RegionSearchViewHolder extends RecyclerView.ViewHolder {
        TextView regionNameTxt;

        RegionSearchViewHolder(View view) {
            super(view);
            regionNameTxt = view.findViewById(R.id.regionNameTxt);
        }
    }
}