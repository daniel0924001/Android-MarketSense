package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.data.Stock;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel.hsieh on 2018/5/3.
 */

public class StockListRecyclerViewAdapter extends RecyclerView.Adapter {

    public interface OnItemClickListener {
        void onItemClick(Stock stock);
    }

    private final Context mContext;
    private List<Stock> mStockList;

    private StockSearchResultRenderer mStockSearchResultRenderer;
    private OnItemClickListener mOnItemClickListener;

    public StockListRecyclerViewAdapter(Context context, List<Stock> stockList) {
        mContext = context;
        mStockList = new ArrayList<>(stockList);
        mStockSearchResultRenderer = new StockSearchResultRenderer();
    }

    public void setOnClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mStockSearchResultRenderer.createView(mContext, parent));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if(mStockList != null) {
            final Stock stock = mStockList.get(position);
            if(stock != null) {
                mStockSearchResultRenderer.renderView(holder.itemView, stock);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(stock);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(mStockList != null) {
            return mStockList.size();
        } else {
            return 0;
        }
    }

    public void filterList(ArrayList<Stock> filterStocks) {
        mStockList = filterStocks;
        notifyDataSetChanged();
    }
}
