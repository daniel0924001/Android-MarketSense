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

import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/7/29.
 */

public class RelatedStockNameRenderer implements MarketSenseRenderer<Stock> {

    private final WeakHashMap<View, RelatedStockNameViewHolder> mViewHolderMap;

    RelatedStockNameRenderer() {
        mViewHolderMap = new WeakHashMap<>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.related_stock_name_list_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull Stock content) {
        RelatedStockNameViewHolder viewHolder = mViewHolderMap.get(view);
        if(viewHolder == null) {
            viewHolder = RelatedStockNameViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, viewHolder);
        }

        MarketSenseRendererHelper.addTextView(viewHolder.titleView, content.getName());
        MarketSenseRendererHelper.addTextView(viewHolder.diffView, content.getDiffPercentage());
        MSLog.e("name: " + content.getName() + ", diff: " + content.getDiffPercentage());

        int colorResourceId = view.getContext().getResources().getColor(content.getDiffColorResourceId());
        viewHolder.diffView.setTextColor(colorResourceId);
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
