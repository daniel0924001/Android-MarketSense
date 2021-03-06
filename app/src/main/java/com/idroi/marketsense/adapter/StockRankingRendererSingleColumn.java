package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.idroi.marketsense.R;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.Stock;

import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/7/28.
 */

public class StockRankingRendererSingleColumn implements MarketSenseRenderer<Stock> {

    private final WeakHashMap<View, StockRankingViewHolder> mViewHolderMap;

    public StockRankingRendererSingleColumn() {
        mViewHolderMap = new WeakHashMap<>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.stock_ranking_list_item_single_column, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull Stock content) {
        StockRankingViewHolder stockRankingViewHolder = mViewHolderMap.get(view);
        if(stockRankingViewHolder == null) {
            stockRankingViewHolder = StockRankingViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, stockRankingViewHolder);
        }

        MarketSenseRendererHelper.addTextView(stockRankingViewHolder.nameView, content.getName());
        MarketSenseRendererHelper.addTextView(stockRankingViewHolder.codeView, content.getCode());
        MarketSenseRendererHelper.addTextView(stockRankingViewHolder.priceView, content.getPrice());
        MarketSenseRendererHelper.addTextView(stockRankingViewHolder.diffView, content.getDiffPercentage());
        setColor(view.getContext(), stockRankingViewHolder, content);
    }

    private void setColor(Context context, StockRankingViewHolder stockRankingViewHolder, Stock content) {
        int colorResourceId = context.getResources().getColor(content.getDiffColorResourceId());
        stockRankingViewHolder.diffView.setTextColor(colorResourceId);
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
