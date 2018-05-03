package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.idroi.marketsense.R;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by daniel.hsieh on 2018/4/30.
 */

public class SettingAdapter extends BaseAdapter {

    private WeakReference<Activity> mActivity;
    private List<String> mStringData;
    private List<Integer> mDrawableIdData;

    private static final int TYPE_USER = 0;
    private static final int TYPE_OTHER = 1;
    private static final int TYPE_MAX_COUNT = TYPE_OTHER + 1;

    public SettingAdapter(Activity activity, List<String> titleList, List<Integer> idList) {
        mActivity = new WeakReference<Activity>(activity);
        mStringData = titleList;
        mDrawableIdData = idList;
    }

    @Override
    public int getCount() {
        return mStringData.size();
    }

    @Override
    public Object getItem(int position) {
        return mStringData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? TYPE_USER : TYPE_OTHER;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        int viewType = getItemViewType(position);
        SettingViewHolder settingViewHolder = null;
        if(convertView == null) {
            switch (viewType) {
                case TYPE_USER:
                    convertView = LayoutInflater.from(mActivity.get())
                            .inflate(R.layout.setting_list_item_user, viewGroup, false);
                    break;
                case TYPE_OTHER:
                    convertView = LayoutInflater.from(mActivity.get())
                            .inflate(R.layout.setting_list_item, viewGroup, false);
                    settingViewHolder = SettingViewHolder.convertToViewHolder(convertView);
                    convertView.setTag(settingViewHolder);
                    break;
            }
        }

        if(viewType == TYPE_OTHER) {
            settingViewHolder = (SettingViewHolder) convertView.getTag();
            settingViewHolder.titleView.setText(getItem(position).toString());

            if (position == 1) {
                settingViewHolder.switchView.setVisibility(View.VISIBLE);
                settingViewHolder.gotoView.setVisibility(View.GONE);
            } else {
                settingViewHolder.switchView.setVisibility(View.GONE);
                settingViewHolder.gotoView.setVisibility(View.VISIBLE);
                settingViewHolder.iconView.setImageDrawable(
                        mActivity.get().getResources().getDrawable(getDrawableId(position)));
            }
        }

        return convertView;
    }

    public int getDrawableId(int position) {
        return mDrawableIdData.get(position);
    }
}
