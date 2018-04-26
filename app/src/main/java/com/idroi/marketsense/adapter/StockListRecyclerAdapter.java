package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.datasource.StockListPlacer;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListRecyclerAdapter extends RecyclerView.Adapter {

    public interface OnItemClickListener {
        void onItemClick(Stock stock);
    }

    private Activity mActivity;
    private StockListPlacer mStockListPlacer;
    private StockListRenderer mStockListRenderer;
    private OnItemClickListener mOnItemClickListener;

    private Handler mHandler;

    public StockListRecyclerAdapter(final Activity activity) {
        mActivity = activity;
        mStockListPlacer = new StockListPlacer(activity);
        mStockListRenderer = new StockListRenderer();
        mHandler = new Handler();
        mStockListPlacer.setStockListListener(new StockListPlacer.StockListListener() {
            @Override
            public void onStockListLoaded() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void loadStockList(String url) {
        mStockListPlacer.loadStockList(url);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mStockListRenderer.createView(mActivity, parent));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Stock stock = mStockListPlacer.getStockData(position);
        if(stock != null) {
            mStockListRenderer.renderView(holder.itemView, stock);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(mStockListPlacer.getStockData(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStockListPlacer.getItemCount();
    }

    public void destroy() {
        mStockListRenderer.clear();
        mStockListPlacer.clear();
    }
}
