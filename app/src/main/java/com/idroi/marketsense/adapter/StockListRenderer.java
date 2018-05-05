package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


        switch (content.getDiffDirection()) {
            case Stock.TREND_UP:
            case Stock.TREND_FLAT:
                stockViewHolder.trendImageView.setImageResource(R.drawable.ic_trending_up_red_24px);
                stockViewHolder.trendTextView.setTextColor(context.getResources().getColor(R.color.colorTrendUp));
                String rankNameUp = String.format(Locale.US, "%.2f", content.getDiff()) +
                        context.getResources().getString(R.string.title_percent);
                MarketSenseRendererHelper.addTextView(
                        stockViewHolder.trendTextView, rankNameUp);
                break;
            case Stock.TREND_DOWN:
                stockViewHolder.trendImageView.setImageResource(R.drawable.ic_trending_down_green_24px);
                stockViewHolder.trendTextView.setTextColor(context.getResources().getColor(R.color.colorTrendDown));
                String rankNameDown = String.format(Locale.US, "%.2f", content.getDiff()) +
                        context.getResources().getString(R.string.title_percent);
                MarketSenseRendererHelper.addTextView(
                        stockViewHolder.trendTextView, rankNameDown);
                break;
            default:
                stockViewHolder.trendImageView.setImageResource(R.drawable.ic_trending_up_red_24px);
                stockViewHolder.trendTextView.setTextColor(context.getResources().getColor(R.color.colorTrendUp));
                MarketSenseRendererHelper.addTextView(
                        stockViewHolder.trendTextView, String.valueOf(87));
        }

        stockViewHolder.progressBar.setProgress((int)content.getConfidence());
        switch (content.getConfidenceDirection()) {
            case Stock.TREND_UP:
                String confidenceUp = context.getResources().getString(R.string.title_predict) +
                        String.format(Locale.US, "%.0f", content.getConfidence()) +
                        context.getResources().getString(R.string.title_percent) +
                        context.getResources().getString(R.string.title_go_up);
                MarketSenseRendererHelper.addTextView(
                        stockViewHolder.confidenceTextView, confidenceUp);
                stockViewHolder.confidenceTextView.setTextColor(context.getResources().getColor(R.color.colorTrendUp));
                stockViewHolder.progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progressbar_go_up));
                break;
            case Stock.TREND_FLAT:
                String confidenceFlat = context.getResources().getString(R.string.title_predict) +
                        String.format(Locale.US, "%.0f", content.getConfidence()) +
                        context.getResources().getString(R.string.title_percent) +
                        context.getResources().getString(R.string.title_go_flat);
                MarketSenseRendererHelper.addTextView(
                        stockViewHolder.confidenceTextView, confidenceFlat);
                stockViewHolder.confidenceTextView.setTextColor(context.getResources().getColor(R.color.colorTrendFlat));
                stockViewHolder.progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progressbar_go_up));
                break;
            case Stock.TREND_DOWN:
                String confidenceDown = context.getResources().getString(R.string.title_predict) +
                        String.format(Locale.US, "%.0f", content.getConfidence()) +
                        context.getResources().getString(R.string.title_percent) +
                        context.getResources().getString(R.string.title_go_down);
                MarketSenseRendererHelper.addTextView(
                        stockViewHolder.confidenceTextView, confidenceDown);
                stockViewHolder.confidenceTextView.setTextColor(context.getResources().getColor(R.color.colorTrendDown));
                stockViewHolder.progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progressbar_go_down));
                break;
            default:
                String confidenceName = context.getResources().getString(R.string.title_predict) +
                        String.format(Locale.US, "%.0f", content.getConfidence()) +
                        context.getResources().getString(R.string.title_percent) +
                        context.getResources().getString(R.string.title_go_up);
                MarketSenseRendererHelper.addTextView(
                        stockViewHolder.confidenceTextView, confidenceName);
                stockViewHolder.confidenceTextView.setTextColor(context.getResources().getColor(R.color.colorTrendUp));
                stockViewHolder.progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.progressbar_go_up));
        }
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
