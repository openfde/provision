package com.android.oobe;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ApplicationHolder> {
    private static final String TAG = "AppAdapter";
    private final List<AppListResult.DataBeanX.DataBean> mApplicationList;
    private RecyclerView mRecyclerView;

    private final Context mContext;

    @Override
    public ApplicationHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_application, viewGroup, false);
        ApplicationHolder holder = new ApplicationHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ApplicationHolder applicationHolder, int position) {
        Log.d(TAG, "onApplication BindViewHolder");
        AppListResult.DataBeanX.DataBean application = mApplicationList.get(position);
        applicationHolder.applicationIcon.setImageResource(R.drawable.img);
        applicationHolder.checkedIcon.setVisibility(application.isChecked() ? View.VISIBLE : View.INVISIBLE);
//        applicationHolder.checkedIcon.setImageBitmap(application.getImageBitmap());
//        applicationHolder.checkedIcon.setVisibility(View.VISIBLE);
//        applicationHolder.applicationName.setText("Genshin Impact");
//        applicationHolder.applicationName.setText(application.getName());
        applicationHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick");
                if ( applicationHolder.checkedIcon.getVisibility() == View.INVISIBLE) {
                    applicationHolder.checkedIcon.setVisibility(View.VISIBLE);
                    application.setChecked(true);
                } else if ( applicationHolder.checkedIcon.getVisibility() == View.VISIBLE) {
                    applicationHolder.checkedIcon.setVisibility(View.INVISIBLE);
                    application.setChecked(false);
                } else {
                    // GONE
                }
            }
        });
    }

    public AppAdapter(Context context, List<AppListResult.DataBeanX.DataBean> applicationList) {
        mContext = context;
        mApplicationList = applicationList;
    }

    @Override
    public int getItemCount() {
        return mApplicationList.size();
    }

    static class ApplicationHolder extends RecyclerView.ViewHolder {
        ImageView applicationIcon;
        TextView applicationName;
        ImageView checkedIcon;

        public ApplicationHolder(View view) {
            super(view);
            applicationIcon = view.findViewById(R.id.application_icon);
            applicationName = view.findViewById(R.id.applicationName);
            checkedIcon = view.findViewById(R.id.checked);
        }
    }
}
