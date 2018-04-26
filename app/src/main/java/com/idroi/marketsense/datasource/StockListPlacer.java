package com.idroi.marketsense.datasource;

import android.app.Activity;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.Constants;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.data.Stock;

import java.util.ArrayList;
import java.util.Arrays;

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
    private String mUrl;

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
                    mMarketSenseStockFetcher.makeRequest(mUrl);
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

    public void loadStockList(String url) {
        mUrl = url;
        loadStockList(new MarketSenseStockFetcher(mActivity, mMarketSenseStockNetworkListener));
    }

    private void loadStockList(MarketSenseStockFetcher stockFetcher) {
        clear();
        mMarketSenseStockFetcher = stockFetcher;
        mMarketSenseStockFetcher.makeRequest(mUrl);
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
        MSLog.e("Stock generate default stock list");
        if(mStockArrayList == null) {
            mStockArrayList = new ArrayList<Stock>();
        }
        mStockArrayList.addAll(Arrays.asList(Constants.HOT_STOCKS_KEYWORDS));
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
