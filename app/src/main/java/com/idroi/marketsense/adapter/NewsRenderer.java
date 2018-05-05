package com.idroi.marketsense.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
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
        update(view.getContext(), newsViewHolder, content);
        setViewVisibility(newsViewHolder, View.VISIBLE);
    }

    private void update(final Context context, final NewsViewHolder newsViewHolder, News content) {
        MarketSenseRendererHelper.addTextView(newsViewHolder.titleView, content.getTitle());
        MarketSenseRendererHelper.addTextView(newsViewHolder.dateView, content.getDate());

        if(content.getImportant()) {
            if(content.isOptimistic()) {
                newsViewHolder.dateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendUp));
                newsViewHolder.alarmImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notifications_red_24px));
                newsViewHolder.dateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendUp));
            } else {
                newsViewHolder.dateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendDown));
                newsViewHolder.alarmImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notifications_green_24px));
                newsViewHolder.dateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendDown));
            }
            newsViewHolder.dateView.setTextColor(context.getResources().getColor(R.color.marketsense_text_white));
            newsViewHolder.alarmImageView.setVisibility(View.VISIBLE);
        } else {
            newsViewHolder.dateView.setBackgroundColor(context.getResources().getColor(R.color.marketsense_text_white));
            newsViewHolder.dateView.setTextColor(context.getResources().getColor(R.color.marketsense_text_gray));
            newsViewHolder.alarmImageView.setVisibility(View.GONE);
        }
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

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
