package com.idroi.marketsense.datasource;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.Cache;
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

import org.json.JSONException;

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

    void makeRequest(String url) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(!DeviceUtils.isNetworkAvailable(context)) {
            mMarketSenseStockNetworkListener.onStockListFail(MarketSenseError.NETWORK_CONNECTION_FAILED);
            return;
        }

        requestStock(url);
    }

    private void requestStock(String url) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        MSLog.i("Loading stock list...: " + url);

        MSLog.i("Loading stock list...(cache): " + url);
        final Cache cache = Networking.getRequestQueue(context).getCache();
        Cache.Entry entry = cache.get(url);
        if(entry != null) {
            try {
                ArrayList<Stock> stockArrayList = StockRequest.stockParseResponse(entry.data);
                MSLog.i("Loading stock list...(cache hit): " + new String(entry.data));
                mMarketSenseStockNetworkListener.onStockListLoad(stockArrayList);
            } catch (JSONException e) {
                MSLog.e("Loading stock list...(cache failed JSONException)");
            }
        } else {
            MSLog.i("Loading stock list...(cache miss)");
        }

        mStockRequest = new StockRequest(Request.Method.GET, url, null, new Response.Listener<ArrayList<Stock>>() {
            @Override
            public void onResponse(ArrayList<Stock> response) {
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                mMarketSenseStockNetworkListener.onStockListLoad(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MSLog.e("Stock Request error: " + error.getMessage(), error);
                if(error.networkResponse != null) {
                    MSLog.e("Stock Request error: " + new String(error.networkResponse.data), error);
                }
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                if(error instanceof MarketSenseNetworkError) {
                    MarketSenseNetworkError networkError = (MarketSenseNetworkError) error;
                    mMarketSenseStockNetworkListener.onStockListFail(networkError.getReason());
                } else {
                    mMarketSenseStockNetworkListener.onStockListFail(MarketSenseError.NETWORK_VOLLEY_ERROR);
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
