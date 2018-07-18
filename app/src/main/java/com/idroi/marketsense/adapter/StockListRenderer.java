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
    private final int[] mBarRedResourceId, mBarGreenResourceId, mAttitudeRedResourceId, mAttitudeGreenResourceId;

    StockListRenderer() {
        mViewHolderMap = new WeakHashMap<>();
        mBarRedResourceId = new int[] {R.mipmap.ic_bar1_overview_red, R.mipmap.ic_bar2_overview_red, R.mipmap.ic_bar3_overview_red};
        mBarGreenResourceId = new int[] {R.mipmap.ic_bar1_overview_green, R.mipmap.ic_bar2_overview_green, R.mipmap.ic_bar3_overview_green};
        mAttitudeRedResourceId = new int[] {R.string.title_level_up_high, R.string.title_level_up_high, R.string.title_level_up_highest};
        mAttitudeGreenResourceId = new int[] {R.string.title_level_down_high, R.string.title_level_down_high, R.string.title_level_down_highest};
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
            textView.setBackgroundColor(textView.getContext().getResources().getColor(R.color.colorTrendUp));
            textView.setTextColor(textView.getContext().getResources().getColor(R.color.text_white));
        } else {
            textView.setBackgroundColor(textView.getResources().getColor(R.color.colorTrendDown));
            textView.setTextColor(textView.getResources().getColor(R.color.text_white));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setBackgroundColor(textView.getResources().getColor(R.color.text_white));
                textView.setTextColor(textView.getResources().getColor(stock.getDiffColorResourceId()));
            }
        }, 100);
    }

    private void update(final Context context, final StockViewHolder stockViewHolder, Stock content) {

        MarketSenseRendererHelper.addTextView(stockViewHolder.nameView, content.getName());
        MarketSenseRendererHelper.addTextView(stockViewHolder.codeView, content.getCode());

        MarketSenseRendererHelper.addTextView(stockViewHolder.priceView, content.getPrice());
        MarketSenseRendererHelper.addTextView(stockViewHolder.diffView, content.getDiffPercentage());

        int peopleLevel = content.getPredictPeopleLevel();
        int newsLevel = content.getPredictNewsLevel();

        if(content.getPredictPeopleDirection() == Stock.TREND_UP) {
            stockViewHolder.predictPeopleImageView.setImageResource(mBarRedResourceId[peopleLevel]);
            stockViewHolder.predictPeopleAttitude.setText(mAttitudeRedResourceId[peopleLevel]);
        } else if(content.getPredictPeopleDirection() == Stock.TREND_DOWN) {
            stockViewHolder.predictPeopleImageView.setImageResource(mBarGreenResourceId[peopleLevel]);
            stockViewHolder.predictPeopleAttitude.setText(mAttitudeGreenResourceId[peopleLevel]);
        } else {
            stockViewHolder.predictPeopleImageView.setImageResource(R.mipmap.ic_bar0_overview_none);
            stockViewHolder.predictPeopleAttitude.setText(R.string.title_level_flat);
        }

        if(content.getConfidenceDirection() == Stock.TREND_UP) {
            stockViewHolder.predictNewsImageView.setImageResource(mBarRedResourceId[newsLevel]);
            stockViewHolder.predictNewsAttitude.setText(mAttitudeRedResourceId[newsLevel]);
        } else if(content.getConfidenceDirection() == Stock.TREND_DOWN) {
            stockViewHolder.predictNewsImageView.setImageResource(mBarGreenResourceId[newsLevel]);
            stockViewHolder.predictNewsAttitude.setText(mAttitudeGreenResourceId[newsLevel]);
        } else {
            stockViewHolder.predictNewsImageView.setImageResource(R.mipmap.ic_bar0_overview_none);
            stockViewHolder.predictNewsAttitude.setText(R.string.title_level_flat);
        }

        setColor(context, stockViewHolder, content);

        content.setRightPredictionBlock(context,
                stockViewHolder.rightBlock, stockViewHolder.rightTitle, stockViewHolder.rightValue);
    }

    private void setColor(Context context, StockViewHolder stockViewHolder, Stock content) {
        int colorResourceId = context.getResources().getColor(content.getDiffColorResourceId());
        stockViewHolder.diffView.setTextColor(colorResourceId);
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
