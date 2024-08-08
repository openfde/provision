package com.android.provision;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BaseTimeZoneAdapter extends RecyclerView.Adapter<BaseTimeZoneAdapter.ViewHolder> {
    private String TAG = "BaseTimeZoneAdapter";
    private Context mContext;
    private int state;
    private ArrayList<BaseTimeZoneItem> items;

    public BaseTimeZoneAdapter(Context context, ArrayList<BaseTimeZoneItem> items) {
        this.mContext = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_time_zone, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BaseTimeZoneItem item = items.get(position);
        boolean available = item.mAvailable;
        setTextView(holder.title, item.mTitle, available);
        setTextView(holder.summary, item.mSummary, false);
        setTextView(holder.currentTime, item.mCurrentTime, false);

        if (available) holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.mOnListItemClickListener.onListItemClick(state == TimeZoneFragment.STATE_REGION_ZONE ? TimeZoneFragment.STATE_TIME_ZONE : state == TimeZoneFragment.STATE_TIME_ZONE && position == 1 ? TimeZoneFragment.STATE_REGION_ZONE : state + 1, item.mId);
            }
        });
        else holder.itemView.setClickable(available);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setTextView(TextView txtView, String txt, Boolean available) {
        if (txt == null || txt.isEmpty()) {
            txtView.setVisibility(View.INVISIBLE);
        } else {
            txtView.setVisibility(View.VISIBLE);
            txtView.setText(txt);
            txtView.setTextColor(mContext.getResources().getColor(available ? R.color.black : R.color.connected_state_color));
        }
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public ArrayList<BaseTimeZoneItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<BaseTimeZoneItem> items) {
        this.items = items;
    }

    public static interface OnListItemClickListener {
        void onListItemClick(int state, String result);
    }

    public static class BaseTimeZoneItem {
        private final String mId;
        private final String mTitle;
        private final String mSummary;
        private final String mCurrentTime;
        private final boolean mAvailable;
        private OnListItemClickListener mOnListItemClickListener;

        public BaseTimeZoneItem(String title, String summary, boolean available, OnListItemClickListener onListItemClickListener) {
            this(null, title, summary, null, available, onListItemClickListener);
        }

        public BaseTimeZoneItem(String id, String title, OnListItemClickListener onListItemClickListener) {
            this(id, title, null, null, true, onListItemClickListener);
        }

        public BaseTimeZoneItem(String id, String title, String summary, String currentTime, OnListItemClickListener onListItemClickListener) {
            this(id, title, summary, currentTime, true, onListItemClickListener);
        }

        public BaseTimeZoneItem(String id, String title, String summary, String currentTime, boolean available, OnListItemClickListener onListItemClickListener) {
            this.mId = id;
            this.mTitle = title;
            this.mSummary = summary;
            this.mCurrentTime = currentTime;
            this.mAvailable = available;
            this.mOnListItemClickListener = onListItemClickListener;
        }

        public String getId() {
            return mId;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getSummary() {
            return mSummary;
        }

        public String getCurrentTime() {
            return mCurrentTime;
        }

        public boolean isAvailable() {
            return mAvailable;
        }

        public OnListItemClickListener getOnListItemClickListener() {
            return mOnListItemClickListener;
        }

        public void setOnListItemClickListener(OnListItemClickListener mOnListItemClickListener) {
            this.mOnListItemClickListener = mOnListItemClickListener;
        }
    }

//    public static class RegionItem extends BaseTimeZoneItem {
//        private final boolean mAvailable;
//        private final String title;
//        String RegionId;
//        String displayName;
//
//        RegionItem(int state, String title, Boolean available, OnListItemClickListener onListItemClickListener) {
//            this.state = state;
//            this.title = title;
//            mAvailable = available;
//            this.onListItemClickListener = onListItemClickListener;
//            // Allow to search with ISO_3166-1 alpha-2 code. It's handy for english users in some
//            // countries, e.g. US for United States. It's not best search keys for users, but
//            // ICU doesn't have the data for the alias names of a region.
//        }
//
//        @Override
//        public String getTitle() {
//            return title;
//        }
//
//        @Override
//        public String getSummary() {
//            return null;
//        }
//
//        @Override
//        public String getIconText() {
//            return "";
//        }
//
//        @Override
//        public String getCurrentTime() {
//            return "";
//        }
//
//        @Override
//        public boolean isAvailable() {
//            return mAvailable;
//        }
//
//        @Override
//        public long getItemId() {
//            return 0;
//        }
//
//        @Override
//        public String[] getSearchKeys() {
//            return new String[0];
//        }
//
//    }
//
//    public static class RegionZoneItem extends BaseTimeZoneItem {
//        @Override
//        public String getTitle() {
//            return null;
//        }
//
//        @Override
//        public String getSummary() {
//            return null;
//        }
//
//        @Override
//        public String getIconText() {
//            return "";
//        }
//
//        @Override
//        public String getCurrentTime() {
//            return "";
//        }
//
//        @Override
//        public boolean isAvailable() {
//            return false;
//        }
//
//        @Override
//        public long getItemId() {
//            return 0;
//        }
//
//        @Override
//        public String[] getSearchKeys() {
//            return new String[0];
//        }
//    }
//
//    public static abstract class BaseTimeZoneItem {
//        int state = -1;
//
//        OnListItemClickListener onListItemClickListener;
//
//        abstract String getTitle();
//
//        abstract String getSummary();
//
//        abstract String getIconText();
//
//        abstract String getCurrentTime();
//
//        abstract boolean isAvailable();
//
//        /**
//         * @return unique non-negative number
//         */
//
//        abstract long getItemId();
//
//        abstract String[] getSearchKeys();
//    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView currentTime;
        private TextView summary;
        private RelativeLayout summaryFrame;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            currentTime = itemView.findViewById(R.id.current_time);
            summary = itemView.findViewById(R.id.summary);
            summaryFrame = itemView.findViewById(R.id.summary_frame);
        }
    }

}