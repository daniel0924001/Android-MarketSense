package com.idroi.marketsense.data;

import android.graphics.drawable.Drawable;

/**
 * Created by daniel.hsieh on 2018/11/8.
 */

public class KnowledgeClass {

    private String mTitle;
    private Drawable mIcon;
    private boolean mPresent;

    public KnowledgeClass(String title, Drawable icon, boolean present) {
        mTitle = title;
        mIcon = icon;
        mPresent = present;
    }

    public String getTitle() {
        return mTitle;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setPresent(boolean present) {
        mPresent = present;
    }

    public boolean isPresent() {
        return mPresent;
    }

    public static class Builder {
        private String mTitle;
        private Drawable mIcon;
        private boolean mPresent;

        public Builder() {
            mPresent = false;
        }

        public Builder title(final String title) {
            mTitle = title;
            return this;
        }

        public Builder icon(final Drawable icon) {
            mIcon = icon;
            return this;
        }

        public Builder present(final boolean present) {
            mPresent = present;
            return this;
        }

        public KnowledgeClass build() {
            return new KnowledgeClass(mTitle, mIcon, mPresent);
        }
    }
}
