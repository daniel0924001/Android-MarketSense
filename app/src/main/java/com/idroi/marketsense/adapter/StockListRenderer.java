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

import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListRenderer implements MarketSenseRenderer<Stock>{

    @NonNull private final WeakHashMap<View, StockViewHolder> mViewHolderMap;
    private final int[] mBarRedResourceId, mBarGreenResourceId;

    StockListRenderer() {
        mViewHolderMap = new WeakHashMap<>();
        mBarRedResourceId = new int[] {R.mipmap.ic_bar1_red, R.mipmap.ic_bar2_red, R.mipmap.ic_bar3_red};
        mBarGreenResourceId = new int[] {R.mipmap.ic_bar1_green, R.mipmap.ic_bar2_green, R.mipmap.ic_bar3_green};
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
        String format = context.getResources().getString(R.string.title_company_name_code_format);
        MarketSenseRendererHelper.addTextView(stockViewHolder.diffView, String.format(format, content.getDiffNumber(), content.getDiffPercentage()));

        int peopleLevel = content.getPredictPeopleLevel();
        int newsLevel = content.getPredictNewsLevel();

        if(content.getPredictPeopleDirection() == Stock.TREND_UP) {
            stockViewHolder.predictPeopleText.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_arrow_red_s, 0, 0, 0);
            stockViewHolder.predictPeopleImageView.setImageResource(mBarRedResourceId[peopleLevel]);
        } else if(content.getPredictPeopleDirection() == Stock.TREND_DOWN) {
            stockViewHolder.predictPeopleText.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_arrow_green_s, 0, 0, 0);
            stockViewHolder.predictPeopleImageView.setImageResource(mBarGreenResourceId[peopleLevel]);
        } else {
            stockViewHolder.predictPeopleText.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_arrow_red_s, 0, 0, 0);
            stockViewHolder.predictPeopleImageView.setImageResource(R.mipmap.ic_bar1_red);
        }

        if(content.getConfidenceDirection() == Stock.TREND_UP) {
            stockViewHolder.predictNewsText.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_arrow_red_s, 0, 0, 0);
            stockViewHolder.predictNewsImageView.setImageResource(mBarRedResourceId[newsLevel]);
        } else if(content.getConfidenceDirection() == Stock.TREND_DOWN) {
            stockViewHolder.predictNewsText.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_arrow_green_s, 0, 0, 0);
            stockViewHolder.predictNewsImageView.setImageResource(mBarGreenResourceId[newsLevel]);
        } else {
            stockViewHolder.predictNewsText.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_arrow_red_s, 0, 0, 0);
            stockViewHolder.predictNewsImageView.setImageResource(R.mipmap.ic_bar1_red);
        }

        setColor(context, stockViewHolder, content);
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
