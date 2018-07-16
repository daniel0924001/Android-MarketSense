package com.idroi.marketsense.adapter;

import android.content.Context;
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

public class NewsSecondRowRenderer implements MarketSenseRenderer<News> {

    @NonNull
    private final WeakHashMap<View, NewsSecondRowViewHolder> mViewHolderMap;

    NewsSecondRowRenderer() {
        mViewHolderMap = new WeakHashMap<View, NewsSecondRowViewHolder>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.news_list_second_row_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull News content) {
        NewsSecondRowViewHolder newsSecondRowViewHolder = mViewHolderMap.get(view);
        if(newsSecondRowViewHolder == null) {
            newsSecondRowViewHolder = NewsSecondRowViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, newsSecondRowViewHolder);
        }
        update(view.getContext(), newsSecondRowViewHolder, content);
    }

    private void update(final Context context, final NewsSecondRowViewHolder newsViewHolder, News content) {

        // left part
        MarketSenseRendererHelper.addTextView(newsViewHolder.leftTitleView, content.getTitle());
        MarketSenseRendererHelper.addTextView(newsViewHolder.leftDateView, content.getDate());

        if(content.getImportant()) {
            if(content.isOptimistic()) {
                newsViewHolder.leftDateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendUp));
                newsViewHolder.leftAlarmImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notifications_red_24px));
                newsViewHolder.leftDateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendUp));
            } else {
                newsViewHolder.leftDateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendDown));
                newsViewHolder.leftAlarmImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notifications_green_24px));
                newsViewHolder.leftDateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendDown));
            }
            newsViewHolder.leftDateView.setTextColor(context.getResources().getColor(R.color.marketsense_text_white));
            newsViewHolder.leftAlarmImageView.setVisibility(View.VISIBLE);
        } else {
            newsViewHolder.leftDateView.setBackgroundColor(context.getResources().getColor(R.color.marketsense_text_white));
            newsViewHolder.leftDateView.setTextColor(context.getResources().getColor(R.color.marketsense_text_gray));
            newsViewHolder.leftAlarmImageView.setVisibility(View.GONE);
        }
        if(!TextUtils.isEmpty(content.getUrlImage())) {
            FrescoImageHelper.loadImageView(content.getUrlImage(),
                    newsViewHolder.leftMainImageView, FrescoImageHelper.MAIN_IMAGE_RATIO);
        } else {
//            FrescoImageHelper.loadImageView(R.mipmap.news_default_image,
//                    newsViewHolder.leftMainImageView, FrescoImageHelper.MAIN_IMAGE_RATIO);
        }

        // right part
        News nextContent = content.getNextNews();
        MarketSenseRendererHelper.addTextView(newsViewHolder.rightTitleView, nextContent.getTitle());
        MarketSenseRendererHelper.addTextView(newsViewHolder.rightDateView, nextContent.getDate());

        if(nextContent.getImportant()) {
            if(nextContent.isOptimistic()) {
                newsViewHolder.rightDateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendUp));
                newsViewHolder.rightAlarmImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notifications_red_24px));
                newsViewHolder.rightDateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendUp));
            } else {
                newsViewHolder.rightDateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendDown));
                newsViewHolder.rightAlarmImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notifications_green_24px));
                newsViewHolder.rightDateView.setBackgroundColor(context.getResources().getColor(R.color.colorTrendDown));
            }
            newsViewHolder.rightDateView.setTextColor(context.getResources().getColor(R.color.marketsense_text_white));
            newsViewHolder.rightAlarmImageView.setVisibility(View.VISIBLE);
        } else {
            newsViewHolder.rightDateView.setBackgroundColor(context.getResources().getColor(R.color.marketsense_text_white));
            newsViewHolder.rightDateView.setTextColor(context.getResources().getColor(R.color.marketsense_text_gray));
            newsViewHolder.rightAlarmImageView.setVisibility(View.GONE);
        }
        if(!TextUtils.isEmpty(nextContent.getUrlImage())) {
            FrescoImageHelper.loadImageView(nextContent.getUrlImage(),
                    newsViewHolder.rightMainImageView, FrescoImageHelper.MAIN_IMAGE_RATIO);
        } else {
//            FrescoImageHelper.loadImageView(R.mipmap.news_default_image,
//                    newsViewHolder.rightMainImageView, FrescoImageHelper.MAIN_IMAGE_RATIO);
        }
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
