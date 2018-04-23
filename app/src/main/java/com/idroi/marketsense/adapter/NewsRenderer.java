package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.WeakHashMap;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.FrescoImageHelper;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.News;

/**
 * Created by daniel.hsieh on 2018/4/19.
 */

public class NewsRenderer implements MarketSenseRenderer<News>{

    @NonNull private final WeakHashMap<View, NewsViewHolder> mViewHolderMap;

    NewsRenderer() {
        mViewHolderMap = new WeakHashMap<View, NewsViewHolder>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.news_list_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull News content) {
        NewsViewHolder newsViewHolder = mViewHolderMap.get(view);
        if(newsViewHolder == null) {
            newsViewHolder = NewsViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, newsViewHolder);
        }
        update(newsViewHolder, content);
        setViewVisibility(newsViewHolder, View.VISIBLE);
    }

    private void update(final NewsViewHolder newsViewHolder, News content) {
        MarketSenseRendererHelper.addTextView(newsViewHolder.titleView, content.getTitle());
        MarketSenseRendererHelper.addTextView(newsViewHolder.dateView, content.getDate());
        if(!TextUtils.isEmpty(content.getUrlImage())) {
            FrescoImageHelper.loadImageView(content.getUrlImage(),
                    newsViewHolder.mainImageView, FrescoImageHelper.MAIN_IMAGE_RATIO);
            newsViewHolder.mainImageView.setVisibility(View.VISIBLE);
        } else {
            newsViewHolder.mainImageView.setVisibility(View.GONE);
        }
    }

    private void setViewVisibility(final NewsViewHolder newsViewHolder, final int visibility) {
        if(newsViewHolder.mainView != null) {
            newsViewHolder.mainView.setVisibility(visibility);
        }
    }

    void clear() {
        mViewHolderMap.clear();
    }
}
