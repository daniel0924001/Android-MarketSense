package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.R;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.StatisticDataItem;

import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/7/23.
 */

public class CriticalStatistics5ColumnsRenderer implements MarketSenseRenderer<StatisticDataItem> {

    private final WeakHashMap<View, CriticalStatistics5ColumnsViewHolder> mViewHolderMap;

    CriticalStatistics5ColumnsRenderer() {
        mViewHolderMap = new WeakHashMap<>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.statistics_list_item_5_columns, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull StatisticDataItem content) {
        CriticalStatistics5ColumnsViewHolder viewHolder = mViewHolderMap.get(view);
        if(viewHolder == null) {
            viewHolder = CriticalStatistics5ColumnsViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, viewHolder);
        }

        MarketSenseRendererHelper.addTextView(viewHolder.titleTextView, content.getTitle());
        MarketSenseRendererHelper.addTextView(viewHolder.value1TextView, content.getValueInIndex(0));
        MarketSenseRendererHelper.addTextView(viewHolder.value2TextView, content.getValueInIndex(1));
        MarketSenseRendererHelper.addTextView(viewHolder.value3TextView, content.getValueInIndex(2));
        MarketSenseRendererHelper.addTextView(viewHolder.value4TextView, content.getValueInIndex(3));

        content.changeColor(view.getContext(), viewHolder.value1TextView, content.getValueInIndex(0));
        content.changeColor(view.getContext(), viewHolder.value2TextView, content.getValueInIndex(1));
        content.changeColor(view.getContext(), viewHolder.value3TextView, content.getValueInIndex(2));
        content.changeColor(view.getContext(), viewHolder.value4TextView, content.getValueInIndex(3));
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
