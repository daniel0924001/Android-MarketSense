package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.WeakHashMap;

import com.idroi.marketsense.R;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.UserProfile;

/**
 * Created by daniel.hsieh on 2018/4/19.
 */

public class NewsRenderer implements MarketSenseRenderer<News>{

    @NonNull private final WeakHashMap<View, NewsViewHolder> mViewHolderMap;
    private boolean mIsShowRelatedStockNames;

    NewsRenderer(boolean showRelatedStockNames) {
        mViewHolderMap = new WeakHashMap<View, NewsViewHolder>();
        mIsShowRelatedStockNames = showRelatedStockNames;
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

        UserProfile userProfile = ClientData.getInstance().getUserProfile();
        if(userProfile.hasReadThisNews(content.getId())) {
            newsViewHolder.titleView.setTextColor(context.getResources().getColor(R.color.text_second));
        }

        // related stock news
        String[] relatedStockNames = content.getStockKeywords();
        newsViewHolder.relatedStockNameAdapter.setRelatedStockNames(relatedStockNames);
        if(mIsShowRelatedStockNames && newsViewHolder.relatedStockNameAdapter.hasRelatedStock()) {
            newsViewHolder.relatedRecyclerView.setVisibility(View.VISIBLE);
        } else {
            newsViewHolder.relatedRecyclerView.setVisibility(View.GONE);
        }

        // fire text
        if(content.isOptimistic()) {
            newsViewHolder.predictionView.setVisibility(View.VISIBLE);
            newsViewHolder.predictionView.setTextColor(context.getResources().getColor(R.color.trend_red));
            newsViewHolder.relatedStockNameAdapter.setMaxItemCount(2);
        } else if(content.isPessimistic()) {
            newsViewHolder.predictionView.setVisibility(View.VISIBLE);
            newsViewHolder.predictionView.setTextColor(context.getResources().getColor(R.color.trend_green));
            newsViewHolder.relatedStockNameAdapter.setMaxItemCount(2);
        } else {
            newsViewHolder.predictionView.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) newsViewHolder.relatedRecyclerView.getLayoutParams();
            params.goneStartMargin = 0;
            newsViewHolder.relatedRecyclerView.setLayoutParams(params);
            newsViewHolder.relatedStockNameAdapter.setMaxItemCount(3);
        }

        // fire image
        switch (content.getLevel()) {
            case 3:
                newsViewHolder.predictionView.setText(R.string.title_news_good3);
                break;
            case 2:
                newsViewHolder.predictionView.setText(R.string.title_news_good2);
                break;
            case 1:
                newsViewHolder.predictionView.setText(R.string.title_news_good1);
                break;
            case -1:
                newsViewHolder.predictionView.setText(R.string.title_news_bad1);
                break;
            case -2:
                newsViewHolder.predictionView.setText(R.string.title_news_bad2);
                break;
            case -3:
                newsViewHolder.predictionView.setText(R.string.title_news_bad3);
                break;
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
