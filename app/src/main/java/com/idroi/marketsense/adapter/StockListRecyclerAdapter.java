package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListRecyclerAdapter extends RecyclerView.Adapter {

    private Activity mActivity;
    private StockListRenderer mStockListRenderer;

    public StockListRecyclerAdapter(final Activity activity) {
        mActivity = activity;
        mStockListRenderer = new StockListRenderer();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mStockListRenderer.createView(mActivity, parent));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
