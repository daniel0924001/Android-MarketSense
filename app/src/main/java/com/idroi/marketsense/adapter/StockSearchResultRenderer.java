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
 * Created by daniel.hsieh on 2018/5/31.
 */

public class StockSearchResultRenderer implements MarketSenseRenderer<Stock> {

    @NonNull private final WeakHashMap<View, StockSearchResultViewHolder> mViewHolderMap;

    StockSearchResultRenderer() {
        mViewHolderMap = new WeakHashMap<>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.stock_list_simple_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull Stock content) {
        StockSearchResultViewHolder viewHolder = mViewHolderMap.get(view);
        if(viewHolder == null) {
            viewHolder = StockSearchResultViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, viewHolder);
        }

        MarketSenseRendererHelper.addTextView(viewHolder.nameView, content.getName());
        MarketSenseRendererHelper.addTextView(viewHolder.codeView, content.getCode());
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
