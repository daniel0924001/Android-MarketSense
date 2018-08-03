package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.Stock;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/7/29.
 */

public class RelatedStockNameAdapter extends RecyclerView.Adapter {

    public interface OnItemClickListener {
        void onItemClick(Stock stock);
    }

    private Context mContext;
    private RelatedStockNameRenderer mRelatedStockNameRenderer;
    private OnItemClickListener mOnItemClickListener;
    private ArrayList<Stock> mRelatedStocks;
    private int mMaxItemCount;

    public RelatedStockNameAdapter(final Context context) {
        mContext = context;
        mRelatedStockNameRenderer = new RelatedStockNameRenderer();
        mMaxItemCount = 3;
    }

    public void setRelatedStockNames(String[] relatedStockNames) {
        if(mRelatedStocks != null) {
            mRelatedStocks.clear();
        } else {
            mRelatedStocks = new ArrayList<>();
        }

        ClientData clientData = ClientData.getInstance();
        for(String name : relatedStockNames) {
            Stock stock = clientData.getPriceFromName(name);
            if(stock != null) {
                mRelatedStocks.add(stock);
            }
        }
        notifyDataSetChanged();
    }

    public boolean hasRelatedStock() {
        return mRelatedStocks != null && mRelatedStocks.size() > 0;
    }

    public void setMaxItemCount(int maxItemCount) {
        mMaxItemCount = maxItemCount;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mRelatedStockNameRenderer.createView(mContext, parent));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(mRelatedStocks != null) {
            final Stock stock = mRelatedStocks.get(position);
            if(stock != null) {
                mRelatedStockNameRenderer.renderView(holder.itemView, stock);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onItemClick(stock);
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        if(mRelatedStocks != null) {
            return Math.min(mMaxItemCount, mRelatedStocks.size());
        } else {
            return 0;
        }
    }

    public void clear() {
        mRelatedStocks.clear();
        mRelatedStocks = null;
    }
}
