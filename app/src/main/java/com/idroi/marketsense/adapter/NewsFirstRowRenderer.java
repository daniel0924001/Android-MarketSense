package com.idroi.marketsense.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.R;
import com.idroi.marketsense.common.FrescoImageHelper;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.News;

import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/5/16.
 */

public class NewsFirstRowRenderer implements MarketSenseRenderer<News> {

    @NonNull
    private final WeakHashMap<View, NewsFirstRowViewHolder> mViewHolderMap;

    NewsFirstRowRenderer() {
        mViewHolderMap = new WeakHashMap<View, NewsFirstRowViewHolder>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.news_list_first_row_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull News content) {
        NewsFirstRowViewHolder newsFirstRowViewHolder = mViewHolderMap.get(view);
        if(newsFirstRowViewHolder == null) {
            newsFirstRowViewHolder = NewsFirstRowViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, newsFirstRowViewHolder);
        }

        MarketSenseRendererHelper.addTextView(newsFirstRowViewHolder.titleView, content.getTitle());
        if(!TextUtils.isEmpty(content.getUrlImage())) {
            FrescoImageHelper.loadImageView(content.getUrlImage(),
                    newsFirstRowViewHolder.mainImageView, FrescoImageHelper.MAIN_IMAGE_RATIO);
        } FrescoImageHelper.loadImageView(R.mipmap.news_default_image,
                newsFirstRowViewHolder.mainImageView, FrescoImageHelper.MAIN_IMAGE_RATIO);
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
