package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.WeakHashMap;

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

        // fire text
        if(content.isOptimistic()) {
            newsViewHolder.fireTextView.setText(R.string.title_news_good);
            newsViewHolder.fireTextView.setTextColor(context.getResources().getColor(R.color.colorTrendUp));
            newsViewHolder.fireTextView.setVisibility(View.VISIBLE);
        } else if(content.isPessimistic()) {
            newsViewHolder.fireTextView.setText(R.string.title_news_bad);
            newsViewHolder.fireTextView.setTextColor(context.getResources().getColor(R.color.colorTrendDown));
            newsViewHolder.fireTextView.setVisibility(View.VISIBLE);
        } else {
            newsViewHolder.fireTextView.setVisibility(View.GONE);
        }

        // fire image
        newsViewHolder.fireImageView.setVisibility(View.VISIBLE);
        switch (content.getLevel()) {
            case 3:
                newsViewHolder.fireImageView.setImageResource(R.mipmap.ic_fire_red3);
                break;
            case 2:
                newsViewHolder.fireImageView.setImageResource(R.mipmap.ic_fire_red2);
                break;
            case 1:
                newsViewHolder.fireImageView.setImageResource(R.mipmap.ic_fire_red1);
                break;
            case -1:
                newsViewHolder.fireImageView.setImageResource(R.mipmap.ic_fire_green1);
                break;
            case -2:
                newsViewHolder.fireImageView.setImageResource(R.mipmap.ic_fire_green2);
                break;
            case -3:
                newsViewHolder.fireImageView.setImageResource(R.mipmap.ic_fire_green3);
                break;
            default:
                newsViewHolder.fireImageView.setVisibility(View.GONE);
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
