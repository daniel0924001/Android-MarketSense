package com.idroi.marketsense.data;

import android.graphics.drawable.Drawable;

import com.idroi.marketsense.adapter.SettingAdapter;

/**
 * Created by daniel.hsieh on 2018/6/29.
 */

public class SettingItem {

    public enum SettingType {
        TYPE_USER(SettingAdapter.TYPE_USER),
        TYPE_OTHER(SettingAdapter.TYPE_OTHER),
        TYPE_SWITCH(SettingAdapter.TYPE_SWITCH),
        TYPE_NO_DRAWABLE(SettingAdapter.TYPE_NO_DRAWABLE);

        private final int typeId;

        SettingType(final int typeId) {
            this.typeId = typeId;
        }

        public int getType() {
            return typeId;
        }
    }

    public static class Builder {
        private final int mId;
        private final SettingType mType;
        private String mTitle;
        private Drawable mDrawableResourceId;
        private boolean mIsClickable;

        public Builder(final SettingType settingType, int id) {
            mType = settingType;
            mId = id;
            mIsClickable = true;
        }

        public Builder title(final String title) {
            mTitle = title;
            return this;
        }

        public Builder drawableResourceId(final Drawable drawable) {
            mDrawableResourceId = drawable;
            return this;
        }

        public Builder isClickable(boolean isClickable) {
            mIsClickable = isClickable;
            return this;
        }

        public SettingItem build() {
            return new SettingItem(this);
        }
    }

    private final int mId;
    private final SettingType mType;
    private final String mTitle;
    private final Drawable mDrawableResourceId;
    private final boolean mIsClickable;

    private SettingItem(final Builder builder) {
        mId = builder.mId;
        mType = builder.mType;
        mTitle = builder.mTitle;
        mDrawableResourceId = builder.mDrawableResourceId;
        mIsClickable = builder.mIsClickable;
    }

    public int getId() {
        return mId;
    }

    public int getType() {
        return mType.getType();
    }

    public String getTitle() {
        return mTitle;
    }

    public Drawable getDrawable() {
        return mDrawableResourceId;
    }

    public boolean isClickable() {
        return mIsClickable;
    }
}
