package com.idroi.marketsense.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.Stock;

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
                .inflate(R.layout.stock_list_item_v3, parent, false);
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

    public void updateRightPartOnly(final View view, final Stock stock) {
        final StockViewHolder stockViewHolder = mViewHolderMap.get(view);
        if(stockViewHolder == null) {
            MSLog.e("stockViewHolder is null in updateRightPartOnly");
            return;
        }

        stockViewHolder.stockPredictionInfoBlockViewHolder
                .renderTomorrowBlock(view.getContext(), stock);
    }

    public void updatePriceOnly(final View view, final Stock stock) {
        final StockViewHolder stockViewHolder = mViewHolderMap.get(view);
        if(stockViewHolder == null) {
            MSLog.e("stockViewHolder is null in updatePriceOnly");
            return;
        }

        try {
            float oldPrice = Float.valueOf(stockViewHolder.priceView.getText().toString());
            float newPrice = Float.valueOf(stock.getPrice());

            if(newPrice > oldPrice) {
                MSLog.i(oldPrice + " -> " + Float.valueOf(stock.getPrice()) + ", name: " + stock.getName());
                MarketSenseRendererHelper.addTextView(stockViewHolder.priceView, stock.getPrice());
                MarketSenseRendererHelper.addTextView(stockViewHolder.diffView, stock.getDiffPercentage());
                animationForPriceChange(stockViewHolder.diffView, stock, true);
            } else if(newPrice < oldPrice) {
                MSLog.i(oldPrice + " -> " + Float.valueOf(stock.getPrice()) + ", name: " + stock.getName());
                MarketSenseRendererHelper.addTextView(stockViewHolder.priceView, stock.getPrice());
                MarketSenseRendererHelper.addTextView(stockViewHolder.diffView, stock.getDiffPercentage());
                animationForPriceChange(stockViewHolder.diffView, stock, false);
            }
        } catch (Exception e) {
            MSLog.e("Exception in updatePriceOnly: " + e.toString());
        }
    }

    private void animationForPriceChange(final TextView textView, final Stock stock, boolean goUp) {
        if(goUp) {
            textView.setBackgroundColor(textView.getContext().getResources().getColor(R.color.trend_red));
            textView.setTextColor(textView.getContext().getResources().getColor(R.color.text_white));
        } else {
            textView.setBackgroundColor(textView.getResources().getColor(R.color.trend_green));
            textView.setTextColor(textView.getResources().getColor(R.color.text_white));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setBackgroundColor(textView.getResources().getColor(R.color.text_white));
                textView.setTextColor(textView.getResources().getColor(stock.getDiffColorResourceId()));
            }
        }, 300);
    }

    private void update(final Context context, final StockViewHolder stockViewHolder, Stock content) {

        MarketSenseRendererHelper.addTextView(stockViewHolder.nameView, content.getName());
        MarketSenseRendererHelper.addTextView(stockViewHolder.codeView, content.getCode());

        MarketSenseRendererHelper.addTextView(stockViewHolder.priceView, content.getPrice());
        MarketSenseRendererHelper.addTextView(stockViewHolder.diffView, content.getDiffPercentage());

        content.renderDiffColor(context, stockViewHolder.diffView);

        stockViewHolder.stockPredictionInfoBlockViewHolder.render(context, content);

//        Stock.renderTitleAndStars(context, content.getPredictTechDirection(),
//                content.getPredictTechLevel(),
//                stockViewHolder.stockPredictionBlockViewHolder.techBlockView,
//                stockViewHolder.stockPredictionBlockViewHolder.techTitleTextView,
//                stockViewHolder.stockPredictionBlockViewHolder.techUnavailableTextView,
//                stockViewHolder.stockPredictionBlockViewHolder.techImageViews);
//
//        Stock.renderTitleAndStars(context, content.getConfidenceDirection(),
//                content.getPredictNewsLevel(),
//                stockViewHolder.stockPredictionBlockViewHolder.newsBlockView,
//                stockViewHolder.stockPredictionBlockViewHolder.newsTitleTextView,
//                stockViewHolder.stockPredictionBlockViewHolder.newsUnavailableTextView,
//                stockViewHolder.stockPredictionBlockViewHolder.newsImageViews);
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
