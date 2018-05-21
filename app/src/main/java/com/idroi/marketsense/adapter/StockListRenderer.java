package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.Stock;

import java.util.Locale;
import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListRenderer implements MarketSenseRenderer<Stock>{

    @NonNull private final WeakHashMap<View, StockViewHolder> mViewHolderMap;

    StockListRenderer() {
        mViewHolderMap = new WeakHashMap<>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.stock_list_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull Stock content) {
        StockViewHolder stockViewHolder = mViewHolderMap.get(view);
        if(stockViewHolder == null) {
            stockViewHolder = StockViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, stockViewHolder);
        }
        update(view.getContext(), stockViewHolder, content);
        setViewVisibility(stockViewHolder, View.VISIBLE);
    }

    private void update(final Context context, final StockViewHolder stockViewHolder, Stock content) {

        MarketSenseRendererHelper.addTextView(stockViewHolder.nameView, content.getName());
        MarketSenseRendererHelper.addTextView(stockViewHolder.codeView, content.getCode());

        MarketSenseRendererHelper.addTextView(stockViewHolder.priceView, content.getPrice());
        MarketSenseRendererHelper.addTextView(stockViewHolder.diffNumberView, content.getDiffNumber());
        MarketSenseRendererHelper.addTextView(stockViewHolder.diffPercentageView, content.getDiffPercentage());

        MarketSenseRendererHelper.addTextView(stockViewHolder.predictNewsScore, content.getPredictNewsString());
        stockViewHolder.predictNewsStars.setRating(content.getPredictNewsScore());

        MarketSenseRendererHelper.addTextView(stockViewHolder.predictPeopleScore, content.getPredictPeopleString());
        stockViewHolder.predictPeopleStars.setRating(content.getPredictPeopleScore());

//        MarketSenseRendererHelper.addTextView(stockViewHolder.predictOurScore, content.getPredictOurString());
//        stockViewHolder.predictOurStars.setRating(content.getPredictOurScore());

        setColor(context, stockViewHolder, content);
    }

    private void setColor(Context context, StockViewHolder stockViewHolder, Stock content) {

        int colorResourceId = context.getResources().getColor(content.getDiffColorResourceId());
        stockViewHolder.priceView.setTextColor(colorResourceId);
        stockViewHolder.diffPercentageView.setTextColor(colorResourceId);
        stockViewHolder.diffNumberView.setTextColor(colorResourceId);

        colorResourceId = context.getResources().getColor(content.getNewsColorResourceId());
        stockViewHolder.predictNewsScore.setTextColor(colorResourceId);
        colorResourceId = context.getResources().getColor(content.getPeopleColorResourceId());
        stockViewHolder.predictPeopleScore.setTextColor(colorResourceId);
//        colorResourceId = context.getResources().getColor(content.getOurColorResourceId());
//        stockViewHolder.predictOurScore.setTextColor(colorResourceId);
    }

    private void setViewVisibility(final StockViewHolder stockViewHolder, final int visibility) {
        if(stockViewHolder.mainView != null) {
            stockViewHolder.mainView.setVisibility(visibility);
        }
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
