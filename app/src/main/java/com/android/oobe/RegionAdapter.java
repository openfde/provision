package com.android.oobe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.yokeyword.indexablerv.IndexableAdapter;

public class RegionAdapter extends IndexableAdapter<RegionEntity> {
    private LanguageActivity.LanguageListener mLanguageListener;

    public RegionAdapter() {
    }

    public void setLanguageListener(LanguageActivity.LanguageListener languageListener){
        mLanguageListener = languageListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateTitleViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_region_title, parent, false);
        return new TitleViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_region_content, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindTitleViewHolder(RecyclerView.ViewHolder viewHolder, String s) {
        TitleViewHolder titleViewHolder = (TitleViewHolder) viewHolder;
        titleViewHolder.titleTxt.setText(s);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder viewHolder, RegionEntity regionEntity) {
        ContentViewHolder contentViewHolder = (ContentViewHolder) viewHolder;
        contentViewHolder.regionNameTxt.setText(regionEntity.getRegionName());
        contentViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLanguageListener.addRegionZone(regionEntity.getRegionInfo().getId());
            }
        });
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt;

        public TitleViewHolder(@NonNull View view) {
            super(view);
            titleTxt = view.findViewById(R.id.titleTxt);
        }
    }

    class ContentViewHolder extends RecyclerView.ViewHolder {
        TextView regionNameTxt;

        public ContentViewHolder(@NonNull View view) {
            super(view);
            regionNameTxt = view.findViewById(R.id.regionNameTxt);
        }
    }

}