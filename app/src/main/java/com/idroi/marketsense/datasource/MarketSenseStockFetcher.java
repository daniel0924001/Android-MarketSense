package com.idroi.marketsense.datasource;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.common.SharedPreferencesCompat;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.request.StockRequest;
import com.idroi.marketsense.util.MarketSenseUtils;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_REQUEST_NAME;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class MarketSenseStockFetcher {

    public interface MarketSenseStockNetworkListener {
        void onStockListLoad(final ArrayList<Stock> stockArrayList, boolean isAutoRefresh);
        void onStockListFail(final MarketSenseError marketSenseError);
    }

    private Handler mTimeoutHandler;
    private Runnable mTimeoutRunnable;

    private static final MarketSenseStockNetworkListener EMPTY_NETWORK_LISTENER = new MarketSenseStockNetworkListener() {
        @Override
        public void onStockListLoad(ArrayList<Stock> newsArray, boolean isAutoRefresh) {

        }

        @Override
        public void onStockListFail(MarketSenseError marketSenseError) {

        }
    };

    private WeakReference<Context> mContext;
    private MarketSenseStockNetworkListener mMarketSenseStockNetworkListener;
    private StockRequest mStockRequest;
    private String mMode;

    MarketSenseStockFetcher(Context context,
                            MarketSenseStockNetworkListener marketSenseStockNetworkListener,
                            @Nullable String mode) {
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
                MSLog.w("Stock price request is timeout.");
                mMarketSenseStockNetworkListener.onStockListFail(MarketSenseError.NETWORK_CONNECTION_TIMEOUT);
            }
        };
        mMode = mode;
    }

    void makeRequest(@NonNull String networkUrl, @Nullable String cacheUrl) {
        makeRequest(networkUrl, cacheUrl, false);
    }

    void makeRequest(@NonNull String networkUrl, @Nullable String cacheUrl, boolean isAutoRefresh) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(!MarketSenseUtils.isNetworkAvailable(context)) {
            mMarketSenseStockNetworkListener.onStockListFail(MarketSenseError.NETWORK_CONNECTION_FAILED);
            return;
        }

        requestStock(networkUrl, cacheUrl, isAutoRefresh);
    }

    private void requestStock(@NonNull final String networkUrl, @Nullable String cacheUrl, final boolean isAutoRefresh) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        MSLog.i("Loading stock list...: " + networkUrl);

        if(cacheUrl == null) {
            MSLog.d("cacheUrl is null, so we set networkUrl to cacheUrl");
            cacheUrl = networkUrl;
        }

        MSLog.i("Loading stock list...(cache): " + cacheUrl);
        final Cache cache = Networking.getRequestQueue(context).getCache();
        Cache.Entry entry = cache.get(cacheUrl);
        if(entry != null && !entry.isExpired()) {
            try {
                ArrayList<Stock> stockArrayList = StockRequest.stockParseResponse(entry.data);
                MSLog.i("Loading stock list...(cache hit): " + new String(entry.data));
                mMarketSenseStockNetworkListener.onStockListLoad(stockArrayList, isAutoRefresh);
            } catch (JSONException e) {
                MSLog.e("Loading stock list...(cache failed JSONException)");
            }
        } else {
            MSLog.i("Loading stock list...(cache miss or expired)");
        }

        mStockRequest = new StockRequest(Request.Method.GET, networkUrl, null, new Response.Listener<ArrayList<Stock>>() {
            @Override
            public void onResponse(ArrayList<Stock> response) {

                SharedPreferences.Editor editor =
                        context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE).edit();
                if(mMode == null) {
                    editor.putString(StockRequest.API_URL, networkUrl);
                    MSLog.d("Stock price network query success, so we save this network url to cache: " + StockRequest.API_URL + " " + networkUrl);
                } else {
                    String key = String.format(StockRequest.API_URL_WITH_MODE, mMode);
                    editor.putString(key, networkUrl);
                    MSLog.d("Stock price network query success, so we save this network url to cache: " + key + " " + networkUrl);
                }
                SharedPreferencesCompat.apply(editor);

                final Context context = getContextOrDestroy();
                if(context == null) {
                    return;
                }
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                mMarketSenseStockNetworkListener.onStockListLoad(response, isAutoRefresh);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final Context context = getContextOrDestroy();
                if(context == null) {
                    return;
                }

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

        mTimeoutHandler.postDelayed(mTimeoutRunnable, 5000);
        Networking.getRequestQueue(context).add(mStockRequest);
    }

    private Context getContextOrDestroy() {
        final Context context = mContext.get();
        if (context == null) {
            destroy();
            MSLog.d("Weak reference to Context in MarketSenseStockFetcher became null. " +
                    "This instance of MarketSenseStockFetcher is destroyed and " +
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

    public static void prefetchWPCTStockList(final Context context) {
        final Context applicationContext = context.getApplicationContext();
        final String networkUrl = StockRequest.queryStockListWithMode(applicationContext, true, StockRequest.MODE_WPCT);
        StockRequest stockRequest = new StockRequest(Request.Method.GET, networkUrl, null, new Response.Listener<ArrayList<Stock>>() {
            @Override
            public void onResponse(ArrayList<Stock> response) {
                if(applicationContext != null) {
                    SharedPreferences.Editor editor =
                            applicationContext.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE).edit();
                    String key = String.format(StockRequest.API_URL_WITH_MODE, StockRequest.MODE_WPCT);
                    editor.putString(key, networkUrl);
                    SharedPreferencesCompat.apply(editor);
                    MSLog.d("Prefetch WPCT network query success, so we save this network url to cache: " +  key + ", " + networkUrl);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MSLog.e("Prefetch WPCT error: " + error.getMessage(), error);
            }
        });

        stockRequest.setShouldCache(true);
        Networking.getRequestQueue(applicationContext).add(stockRequest);
    }
}
