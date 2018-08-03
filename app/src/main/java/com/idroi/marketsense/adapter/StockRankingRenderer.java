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

public class StockRankingRenderer implements MarketSenseRenderer<Stock> {

    public static final int RANKING_BY_TECH = 1;
    public static final int RANKING_BY_NEWS = 2;

    private final WeakHashMap<View, StockRankingViewHolder> mViewHolderMap;
    private int mRankType;

    StockRankingRenderer(int rankType) {
        mViewHolderMap = new WeakHashMap<>();
        mRankType = rankType;
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.stock_ranking_list_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull Stock content) {
        StockRankingViewHolder stockRankingViewHolder = mViewHolderMap.get(view);
        if(stockRankingViewHolder == null) {
            stockRankingViewHolder = StockRankingViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, stockRankingViewHolder);
        }

        MarketSenseRendererHelper.addTextView(stockRankingViewHolder.nameView, content.getName());
        MarketSenseRendererHelper.addTextView(stockRankingViewHolder.priceView, content.getPrice());
        setColor(view.getContext(), stockRankingViewHolder, content);
        setRankingIcon(stockRankingViewHolder.priceImageView,
                stockRankingViewHolder.predictionImageView, content);
    }

    private void setColor(Context context, StockRankingViewHolder stockRankingViewHolder, Stock content) {
        int colorResourceId = context.getResources().getColor(content.getDiffColorResourceId());
        stockRankingViewHolder.priceView.setTextColor(colorResourceId);
    }

    private void setRankingIcon(ImageView priceImageView, ImageView predictImageView, Stock stock) {
        int direction = Stock.TREND_UP;
        switch (mRankType) {
            case RANKING_BY_TECH:
                direction = stock.getPredictTechDirection();
                break;
            case RANKING_BY_NEWS:
                direction = stock.getConfidenceDirection();
                break;
        }

        if(direction == Stock.TREND_DOWN) {
            predictImageView.setImageResource(R.mipmap.ic_direction_down);
        } else {
            predictImageView.setImageResource(R.mipmap.ic_direction_up);
        }

        if(stock.getDiffDirection() == Stock.TREND_UP) {
            priceImageView.setImageResource(R.mipmap.ic_trend_arrow_up);
        } else {
            priceImageView.setImageResource(R.mipmap.ic_trend_arrow_down);
        }
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
