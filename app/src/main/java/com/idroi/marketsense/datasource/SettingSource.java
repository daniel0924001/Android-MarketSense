package com.idroi.marketsense.datasource;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.idroi.marketsense.BuildConfig;
import com.idroi.marketsense.R;
import com.idroi.marketsense.data.SettingItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel.hsieh on 2018/6/29.
 */

public class SettingSource {

    private Context mContext;
    private List<SettingItem> mSettingItemList;

    public SettingSource(final Context context) {
        mContext = context.getApplicationContext();
        mSettingItemList = new ArrayList<>();
        mSettingItemList.add(
                new SettingItem
                        .Builder(SettingItem.SettingType.TYPE_USER, 1)
                        .build()

        );
        mSettingItemList.add(
                new SettingItem
                        .Builder(SettingItem.SettingType.TYPE_OTHER, R.string.preference_knowledge)
                        .title(mContext.getResources().getString(R.string.preference_knowledge))
                        .drawableResourceId(mContext.getResources().getDrawable(R.drawable.setting_about))
                        .isLargeMargin(true)
                        .build()

        );
        mSettingItemList.add(
                new SettingItem
                        .Builder(SettingItem.SettingType.TYPE_SWITCH, R.string.preference_notification)
                        .title(mContext.getResources().getString(R.string.preference_notification))
                        .drawableResourceId(mContext.getResources().getDrawable(R.drawable.setting_notification))
                        .build()

        );
        mSettingItemList.add(
                new SettingItem
                        .Builder(SettingItem.SettingType.TYPE_OTHER, R.string.preference_share)
                        .title(mContext.getResources().getString(R.string.preference_share))
                        .drawableResourceId(mContext.getResources().getDrawable(R.drawable.setting_share))
                        .build()

        );
        mSettingItemList.add(
                new SettingItem
                        .Builder(SettingItem.SettingType.TYPE_OTHER, R.string.preference_feedback)
                        .title(mContext.getResources().getString(R.string.preference_feedback))
                        .drawableResourceId(mContext.getResources().getDrawable(R.drawable.setting_feedback))
                        .build()

        );
        mSettingItemList.add(
                new SettingItem
                        .Builder(SettingItem.SettingType.TYPE_OTHER, R.string.preference_star)
                        .title(mContext.getResources().getString(R.string.preference_star))
                        .drawableResourceId(mContext.getResources().getDrawable(R.drawable.setting_star))
                        .build()

        );
        mSettingItemList.add(
                new SettingItem
                        .Builder(SettingItem.SettingType.TYPE_NO_DRAWABLE, R.string.preference_privacy_statement)
                        .title(mContext.getResources().getString(R.string.preference_privacy_statement))
                        .isLargeMargin(true)
                        .build()

        );
        mSettingItemList.add(
                new SettingItem
                        .Builder(SettingItem.SettingType.TYPE_NO_DRAWABLE, R.string.preference_term_of_service)
                        .title(mContext.getResources().getString(R.string.preference_term_of_service))
                        .build()

        );
        mSettingItemList.add(
                new SettingItem
                        .Builder(SettingItem.SettingType.TYPE_NO_DRAWABLE, R.string.preference_disclaimer)
                        .title(mContext.getResources().getString(R.string.preference_disclaimer))
                        .build()

        );
        mSettingItemList.add(
                new SettingItem
                        .Builder(SettingItem.SettingType.TYPE_NO_DRAWABLE, R.string.preference_email)
                        .title(mContext.getResources().getString(R.string.preference_email))
                        .hideArrow(true)
                        .build()

        );
        mSettingItemList.add(
                new SettingItem
                        .Builder(SettingItem.SettingType.TYPE_NO_DRAWABLE, R.string.preference_version)
                        .title(String.format(mContext.getResources().getString(R.string.preference_version), BuildConfig.VERSION_NAME))
                        .isClickable(false)
                        .hideArrow(true)
                        .build()

        );
    }

    public SettingItem getItem(int position) {
        return mSettingItemList.get(position);
    }

    public int getSize() {
        return mSettingItemList.size();
    }

    public int getId(int position) {
        SettingItem settingItem = mSettingItemList.get(position);
        if(settingItem != null) {
            return settingItem.getId();
        } else {
            return -1;
        }
    }

    public String getTitle(int position) {
        SettingItem settingItem = mSettingItemList.get(position);
        return settingItem.getTitle();
    }

    public Drawable getDrawable(int position) {
        SettingItem settingItem = mSettingItemList.get(position);
        return settingItem.getDrawable();
    }

    public boolean isLargeMargin(int position) {
        SettingItem settingItem = mSettingItemList.get(position);
        return settingItem.isLargeMargin();
    }

    public boolean hideArrow(int position) {
        SettingItem settingItem = mSettingItemList.get(position);
        return settingItem.hideArrow();
    }

    public void destroy() {
        mSettingItemList.clear();
    }
}
