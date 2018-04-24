package com.idroi.marketsense.datasource;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.util.DeviceUtils;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public class MarketSenseNewsFetcher {

    public interface MarketSenseNewsNetworkListener {
        void onNewsLoad(final ArrayList<News> newsArray);
        void onNewsFail(final MarketSenseError marketSenseError);
    }

    private Handler mTimeoutHandler;
    private Runnable mTimeoutRunnable;

    private static final MarketSenseNewsNetworkListener EMPTY_NETWORK_LISTENER = new MarketSenseNewsNetworkListener() {
        @Override
        public void onNewsLoad(ArrayList<News> newsArray) {

        }

        @Override
        public void onNewsFail(MarketSenseError marketSenseError) {

        }
    };

    private WeakReference<Context> mContext;
    private MarketSenseNewsNetworkListener mMarketSenseNewsNetworkListener;
    private NewsRequest mNewsRequest;

    MarketSenseNewsFetcher(Context context,
            MarketSenseNewsNetworkListener marketSenseNewsNetworkListener) {
        mContext = new WeakReference<Context>(context);
        mMarketSenseNewsNetworkListener = marketSenseNewsNetworkListener;
        mTimeoutHandler = new Handler();
        mTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if(mNewsRequest != null) {
                    mNewsRequest.cancel();
                    mNewsRequest = null;
                }
                mMarketSenseNewsNetworkListener.onNewsFail(MarketSenseError.NETWROK_CONNECTION_TIMEOUT);
            }
        };
    }

    void makeRequest() {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(!DeviceUtils.isNetworkAvailable(context)) {
            mMarketSenseNewsNetworkListener.onNewsFail(MarketSenseError.NETWORK_CONNECTION_FAILED);
            return;
        }

        requestNews();
    }

    private void requestNews() {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        String url = urlBuilder("business", "TW", "zh");

        mNewsRequest = new NewsRequest(Request.Method.GET, url, null, new Response.Listener<ArrayList<News>>() {
            @Override
            public void onResponse(ArrayList<News> response) {
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                mMarketSenseNewsNetworkListener.onNewsLoad(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                if(error instanceof MarketSenseNetworkError) {
                    MarketSenseNetworkError networkError = (MarketSenseNetworkError) error;
                    mMarketSenseNewsNetworkListener.onNewsFail(networkError.getReason());
                }
            }
        });

        mTimeoutHandler.postDelayed(mTimeoutRunnable, 3000);
        Networking.getRequestQueue(context).add(mNewsRequest);
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
        if(mNewsRequest != null) {
            mNewsRequest.cancel();
            mNewsRequest = null;
        }
        mMarketSenseNewsNetworkListener = EMPTY_NETWORK_LISTENER;
        if(mTimeoutHandler != null) {
            mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
        }
    }

    private static final String API_URL = "http://adzodiac.droi.com:8888/get_news?";
    private static final String PARAM_LANG = "&language=";
    private static final String PARAM_CATEGORY = "&category=";
    private static final String PARAM_COUNTRY = "&country=";

    public static String urlBuilder(String category,
                                    String country,
                                    String language) {

        StringBuilder builder = new StringBuilder(API_URL);

        builder.append(PARAM_CATEGORY).append(category);
        builder.append(PARAM_COUNTRY).append(country);
        builder.append(PARAM_LANG).append(language);

        return builder.toString();
    }

}
