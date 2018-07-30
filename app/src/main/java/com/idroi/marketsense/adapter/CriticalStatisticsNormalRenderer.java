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

public class CriticalStatisticsNormalRenderer implements MarketSenseRenderer<StatisticDataItem> {

    private final WeakHashMap<View, CriticalStatisticsNormalViewHolder> mViewHolderMap;

    CriticalStatisticsNormalRenderer() {
        mViewHolderMap = new WeakHashMap<>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.statistics_list_item_normal, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull StatisticDataItem content) {
        CriticalStatisticsNormalViewHolder viewHolder = mViewHolderMap.get(view);
        if(viewHolder == null) {
            viewHolder = CriticalStatisticsNormalViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, viewHolder);
        }

        MarketSenseRendererHelper.addTextView(viewHolder.titleTextView, content.getTitle());
        MarketSenseRendererHelper.addTextView(viewHolder.valueTextView, content.getValue());

        content.changeColor(view.getContext(), viewHolder.valueTextView, content.getValue());
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
