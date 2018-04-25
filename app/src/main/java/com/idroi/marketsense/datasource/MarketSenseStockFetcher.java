package com.idroi.marketsense.datasource;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.request.StockRequest;
import com.idroi.marketsense.util.DeviceUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class MarketSenseStockFetcher {

    public interface MarketSenseStockNetworkListener {
        void onStockListLoad(final ArrayList<Stock> stockArrayList);
        void onStockListFail(final MarketSenseError marketSenseError);
    }

    private Handler mTimeoutHandler;
    private Runnable mTimeoutRunnable;

    private static final MarketSenseStockNetworkListener EMPTY_NETWORK_LISTENER = new MarketSenseStockNetworkListener() {
        @Override
        public void onStockListLoad(ArrayList<Stock> newsArray) {

        }

        @Override
        public void onStockListFail(MarketSenseError marketSenseError) {

        }
    };

    private WeakReference<Context> mContext;
    private MarketSenseStockNetworkListener mMarketSenseStockNetworkListener;
    private StockRequest mStockRequest;

    MarketSenseStockFetcher(Context context,
            MarketSenseStockNetworkListener marketSenseStockNetworkListener) {
        mContext = new WeakReference<Context>(context);
        mMarketSenseStockNetworkListener = marketSenseStockNetworkListener;
        mTimeoutHandler = new Handler();
        mTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if(mStockRequest != null) {
                    mStockRequest.cancel();
                    mStockRequest = null;
                }
                mMarketSenseStockNetworkListener.onStockListFail(MarketSenseError.NETWROK_CONNECTION_TIMEOUT);
            }
        };
    }

    void makeRequest() {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(!DeviceUtils.isNetworkAvailable(context)) {
            mMarketSenseStockNetworkListener.onStockListFail(MarketSenseError.NETWORK_CONNECTION_FAILED);
            return;
        }

        requestStock();
    }

    private void requestStock() {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        // TODO
        String fakeURL = "http://apiv2.infohubapp.com/v1/keyword?keyword=%E9%B4%BB%E6%B5%B7";
        MSLog.i("Loading stock list...");
        mStockRequest = new StockRequest(Request.Method.GET, fakeURL, null, new Response.Listener<ArrayList<Stock>>() {
            @Override
            public void onResponse(ArrayList<Stock> response) {
                MSLog.e("Stock Request success: ");
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                mMarketSenseStockNetworkListener.onStockListLoad(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MSLog.e("Stock Request error: " + error.getMessage(), error);
                MSLog.e("Stock Request error: " + new String(error.networkResponse.data), error);
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                if(error instanceof MarketSenseNetworkError) {
                    MarketSenseNetworkError networkError = (MarketSenseNetworkError) error;
                    mMarketSenseStockNetworkListener.onStockListFail(networkError.getReason());
                }
            }
        });

        mTimeoutHandler.postDelayed(mTimeoutRunnable, 3000);
        Networking.getRequestQueue(context).add(mStockRequest);
    }

    private Context getContextOrDestroy() {
        final Context context = mContext.get();
        if (context == null) {
            destroy();
            MSLog.d("Weak reference to Context in MarketSenseNewsFetcher became null. " +
                    "This instance of MarketSenseNewsFetcher is destroyed and " +
                    "no more requests will be processed.");
        }
        return context;
    }

    public void destroy() {
        mContext.clear();
        if(mStockRequest != null) {
            mStockRequest.cancel();
            mStockRequest = null;
        }
        mMarketSenseStockNetworkListener = EMPTY_NETWORK_LISTENER;
        if(mTimeoutHandler != null) {
            mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
        }
    }
}
