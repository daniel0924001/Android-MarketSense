package com.idroi.marketsense.datasource;

import android.app.Activity;

import com.idroi.marketsense.common.Constants;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.data.Stock;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListPlacer {

    public interface StockListListener {
        void onStockListLoaded();
    }

    private static final int RETRY_TIME_CONST = 2;
    private int mCurrentRetries = 0;

    private ArrayList<Stock> mStockArrayList;
    private MarketSenseStockFetcher.MarketSenseStockNetworkListener mMarketSenseStockNetworkListener;
    private StockListListener mStockListListener;

    private Activity mActivity;
    private MarketSenseStockFetcher mMarketSenseStockFetcher;

    public StockListPlacer(Activity activity) {
        mActivity = activity;

        mMarketSenseStockNetworkListener = new MarketSenseStockFetcher.MarketSenseStockNetworkListener() {
            @Override
            public void onStockListLoad(ArrayList<Stock> stockArrayList) {
                mStockArrayList = stockArrayList;
                if(mStockListListener != null) {
                    mStockListListener.onStockListLoaded();
                }
            }

            @Override
            public void onStockListFail(MarketSenseError marketSenseError) {
                increaseRetryTime();
                if(isRetry()) {
                    mMarketSenseStockFetcher.makeRequest();
                } else {
                    generateDefaultStockList();
                    mStockListListener.onStockListLoaded();
                }
            }
        };
        resetRetryTime();
    }

    private boolean isRetry() {
        return mCurrentRetries <= RETRY_TIME_CONST;
    }

    private void increaseRetryTime() {
        if(mCurrentRetries <= RETRY_TIME_CONST) {
            mCurrentRetries++;
        }
    }

    private void resetRetryTime() {
        mCurrentRetries = 0;
    }

    public void setStockListListener(StockListListener listener) {
        mStockListListener = listener;
    }

    public void loadStockList() {
        loadStockList(new MarketSenseStockFetcher(mActivity, mMarketSenseStockNetworkListener));
    }

    public void loadStockList(MarketSenseStockFetcher stockFetcher) {
        clear();
        mMarketSenseStockFetcher = stockFetcher;
        mMarketSenseStockFetcher.makeRequest();
    }

    public void clear() {
        if(mStockArrayList != null) {
            mStockArrayList.clear();
            mStockArrayList = null;
        }
        if(mMarketSenseStockFetcher != null) {
            mMarketSenseStockFetcher.destroy();
            mMarketSenseStockFetcher = null;
        }
    }

    public void generateDefaultStockList() {
        if(mStockArrayList == null) {
            mStockArrayList = new ArrayList<Stock>();
        }
        for(int i = 0; i < Constants.HOT_STOCKS_KEYWORDS.length; i++) {
            Stock stock = new Stock();
            stock.setName(Constants.HOT_STOCKS_KEYWORDS[i]);
            mStockArrayList.add(stock);
        }
    }

    public int getItemCount() {
        if(mStockArrayList != null) {
            return mStockArrayList.size();
        } else {
            return 0;
        }
    }

    public Stock getStockData(int position) {
        if(mStockArrayList == null || position > mStockArrayList.size() || position < 0) {
            return null;
        }
        return mStockArrayList.get(position);
    }

}
