package com.idroi.marketsense.data;

import android.graphics.drawable.Drawable;

/**
 * Created by daniel.hsieh on 2018/11/8.
 */

public class KnowledgeClass {

    private String mTitle;
    private Drawable mIcon;

    public KnowledgeClass(String title, Drawable icon) {
        mTitle = title;
        mIcon = icon;
    }

    public String getTitle() {
        return mTitle;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public static class Builder {
        private String mTitle;
        private Drawable mIcon;

        public Builder() {

        }

        public Builder title(final String title) {
            mTitle = title;
            return this;
        }

        public Builder icon(final Drawable icon) {
            mIcon = icon;
            return this;
        }

        public KnowledgeClass build() {
            return new KnowledgeClass(mTitle, mIcon);
        }
    }
}
