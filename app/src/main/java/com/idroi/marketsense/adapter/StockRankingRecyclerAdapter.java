package com.idroi.marketsense.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.Stock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.idroi.marketsense.adapter.StockRankingRenderer.RANKING_BY_NEWS;
import static com.idroi.marketsense.adapter.StockRankingRenderer.RANKING_BY_TECH;

/**
 * Created by daniel.hsieh on 2018/7/28.
 */

public class StockRankingRecyclerAdapter extends RecyclerView.Adapter {

    public interface OnItemClickListener {
        void onItemClick(Stock stock);
    }

    private Context mContext;
    private ArrayList<Stock> mStockList;
    private StockRankingRenderer mStockRankingRenderer;
    private int mItemCount;
    private int mRankType;

    private OnItemClickListener mOnItemClickListener;

    public StockRankingRecyclerAdapter(final Context context) {
        mContext = context;
        mItemCount = 5;
    }

    public void setStockList(ArrayList<Stock> stockArrayList, final int rankType, boolean needToSort) {
        mRankType = rankType;
        mStockList = new ArrayList<>(stockArrayList);
        mStockRankingRenderer = new StockRankingRenderer(rankType);

        if(needToSort) {
            Collections.sort(mStockList, genComparator());
            ClientData.getInstance().setSortedRealTimePrices(rankType, mStockList);
        } else {
            if(ClientData.getInstance().isWorkDayAndStockMarketIsOpen()) {
                HandlerThread handlerThread = new HandlerThread("UpdateSortedRealPrice");
                handlerThread.start();
                Handler backgroundHandler = new Handler(handlerThread.getLooper());
                backgroundHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Stock> stocks = ClientData.getInstance().getStockPrices();
                        if (stocks != null) {
                            Collections.sort(stocks, genComparator());
                            ClientData.getInstance().setSortedRealTimePrices(rankType, stocks);
                        }
                    }
                });
            }
        }
    }

    public void setItemCount(int itemCount) {
        mItemCount = itemCount;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mStockRankingRenderer.createView(mContext, parent));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Stock stock = mStockList.get(position);
        if(stock != null) {
            mStockRankingRenderer.renderView(holder.itemView, stock);
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

    @Override
    public int getItemCount() {
        if(mItemCount < 0) {
            return 0;
        }

        return Math.min(mItemCount, mStockList.size());
    }

    private Comparator<Stock> genComparator() {
        return new Comparator<Stock>() {
            @Override
            public int compare(Stock stock1, Stock stock2) {
                switch (mRankType) {
                    case RANKING_BY_TECH:
                        return compareValue(stock2, stock1, RANKING_BY_TECH);
                    case RANKING_BY_NEWS:
                        return compareValue(stock2, stock1, RANKING_BY_NEWS);
                    default:
                        return 0;
                }
            }
        };
    }

    private int compareValue(float double1, float double2) {
        if(double1 > double2) {
            return 1;
        } else if(double1 == double2) {
            return 0;
        } else {
            return -1;
        }
    }

    private int compareValue(Stock stock1, Stock stock2, int type) {
        float value1 = 0, value2 = 0;

        // we want to find hot stock
        value1 = (stock1.getYesterdayVolume() > 1500) ? 10 : -10;
        value2 = (stock2.getYesterdayVolume() > 1500) ? 10 : -10;

        if(type == RANKING_BY_TECH) {
            value1 += (stock1.getDiffDirection() == stock1.getPredictTechDirection()) ? 20 : -20;
            value2 += (stock2.getDiffDirection() == stock2.getPredictTechDirection()) ? 20 : -20;

            value1 += Math.abs(stock1.getPredictionTechScore());
            value2 += Math.abs(stock2.getPredictionTechScore());
        } else if(type == RANKING_BY_NEWS) {
            value1 += (stock1.getDiffDirection() == stock1.getConfidenceDirection()) ? 20 : -20;
            value2 += (stock2.getDiffDirection() == stock2.getConfidenceDirection()) ? 20 : -20;

            value1 += Math.abs(stock1.getPredictNewsScore());
            value2 += Math.abs(stock2.getPredictNewsScore());
        }

        return compareValue(value1, value2);
    }

    public void destroy() {
        if(mStockList != null) {
            mStockList.clear();
        }
        mStockRankingRenderer.clear();
        mOnItemClickListener = null;
    }
}
