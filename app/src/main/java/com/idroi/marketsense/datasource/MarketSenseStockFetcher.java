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

    void makeRequest(@NonNull String networkUrl, @Nullable String cacheUrl) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(!MarketSenseUtils.isNetworkAvailable(context)) {
            mMarketSenseStockNetworkListener.onStockListFail(MarketSenseError.NETWORK_CONNECTION_FAILED);
            return;
        }

        requestStock(networkUrl, cacheUrl);
    }

    private void requestStock(@NonNull final String networkUrl, @Nullable String cacheUrl) {
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

        mStockRequest = new StockRequest(Request.Method.GET, networkUrl, null, new Response.Listener<ArrayList<Stock>>() {
            @Override
            public void onResponse(ArrayList<Stock> response) {
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                mMarketSenseStockNetworkListener.onStockListLoad(response);

                SharedPreferences.Editor editor =
                        context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE).edit();
                editor.putString(StockRequest.API_URL, networkUrl);
                SharedPreferencesCompat.apply(editor);
                MSLog.d("Stock price network query success, so we save this network url to cache: " + networkUrl);
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
}
