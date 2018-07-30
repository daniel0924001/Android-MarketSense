package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;

import com.idroi.marketsense.R;
import com.idroi.marketsense.data.SettingItem;
import com.idroi.marketsense.datasource.SettingSource;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_USER_SETTING;
import static com.idroi.marketsense.common.Constants.USER_SETTING_NOTIFICATION_KEY;

/**
 * Created by daniel.hsieh on 2018/4/30.
 */

public class SettingAdapter extends BaseAdapter {

    public interface SettingOnClickListener {
        void onLoginBtnClick();
        void onSwitchClick(boolean isChecked);
    }

    private WeakReference<Activity> mActivity;

    public static final int TYPE_USER = 0;
    public static final int TYPE_OTHER = 1;
    public static final int TYPE_SWITCH = 2;
    public static final int TYPE_NO_DRAWABLE = 3;
    private static final int TYPE_MAX_COUNT = TYPE_NO_DRAWABLE + 1;

    private SettingOnClickListener mLoginOnClickListener;
    private SettingSource mSettingSource;

    public SettingAdapter(Activity activity, SettingSource settingSource) {
        mActivity = new WeakReference<>(activity);
        mSettingSource = settingSource;
    }

    public void setSettingOnClickListener(SettingOnClickListener listener) {
        mLoginOnClickListener = listener;
    }

    @Override
    public int getCount() {
        return mSettingSource.getSize();
    }

    @Override
    public Object getItem(int position) {
        return mSettingSource.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return mSettingSource.getItem(position).getType();
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
                    Button LoginBtn = convertView.findViewById(R.id.setting_login_btn);
                    LoginBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(mLoginOnClickListener != null) {
                                mLoginOnClickListener.onLoginBtnClick();
                            }
                        }
                    });
                    break;
                case TYPE_OTHER:
                    convertView = LayoutInflater.from(mActivity.get())
                            .inflate(R.layout.setting_list_item, viewGroup, false);
                    settingViewHolder = SettingViewHolder.convertToViewHolder(convertView);
                    convertView.setTag(settingViewHolder);
                    break;
                case TYPE_SWITCH:
                    convertView = LayoutInflater.from(mActivity.get())
                            .inflate(R.layout.setting_list_item_switch, viewGroup, false);
                    settingViewHolder = SettingViewHolder.convertToViewHolder(convertView);
                    convertView.setTag(settingViewHolder);
                    break;
                case TYPE_NO_DRAWABLE:
                    convertView = LayoutInflater.from(mActivity.get())
                            .inflate(R.layout.setting_list_item_no_drawable, viewGroup, false);
                    settingViewHolder = SettingViewHolder.convertToViewHolder(convertView);
                    convertView.setTag(settingViewHolder);
                    break;
            }
        }

        if(convertView != null && convertView.getTag() != null) {
            settingViewHolder = (SettingViewHolder) convertView.getTag();

            if(settingViewHolder.titleView != null) {
                settingViewHolder.titleView.setText(getTitle(position));
            }

            if(settingViewHolder.iconView != null) {
                settingViewHolder.iconView.setImageDrawable(getDrawable(position));
            }

            if(settingViewHolder.switchView != null) {
                boolean isNotificationSetting = true;
                Activity activity = mActivity.get();
                if(activity != null) {
                    SharedPreferences sharedPreferences =
                            activity.getSharedPreferences(SHARED_PREFERENCE_USER_SETTING, Context.MODE_PRIVATE);
                    isNotificationSetting = sharedPreferences.getBoolean(USER_SETTING_NOTIFICATION_KEY, true);
                }
                settingViewHolder.switchView.setChecked(isNotificationSetting);
                settingViewHolder.switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if(mLoginOnClickListener != null) {
                            mLoginOnClickListener.onSwitchClick(isChecked);
                        }
                    }
                });
            }
        }

        return convertView;
    }

    private String getTitle(int position) {
        return mSettingSource.getTitle(position);
    }

    private Drawable getDrawable(int position) {
        return mSettingSource.getDrawable(position);
    }
}
