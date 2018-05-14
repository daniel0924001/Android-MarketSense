package com.idroi.marketsense.datasource;

import android.content.Context;
import android.os.Handler;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.util.MarketSenseUtils;

import static com.idroi.marketsense.request.NewsRequest.PARAM_KEYWORD_ARRAY;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public class MarketSenseNewsFetcher {

    public interface MarketSenseNewsNetworkListener {
        void onNewsLoad(final ArrayList<News> newsArray, boolean isCache);
        void onNewsFail(final MarketSenseError marketSenseError);
    }

    private Handler mTimeoutHandler;
    private Runnable mTimeoutRunnable;

    private static final MarketSenseNewsNetworkListener EMPTY_NETWORK_LISTENER = new MarketSenseNewsNetworkListener() {
        @Override
        public void onNewsLoad(ArrayList<News> newsArray, boolean isCache) {

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

    void makeRequest(String url, boolean shouldReadFromCache) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(!MarketSenseUtils.isNetworkAvailable(context)) {
            mMarketSenseNewsNetworkListener.onNewsFail(MarketSenseError.NETWORK_CONNECTION_FAILED);
            return;
        }

        requestNews(url, shouldReadFromCache);
    }

    private void requestNews(String url, boolean shouldReadFromCache) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        MSLog.i("Loading news...: " + url);

        if(shouldReadFromCache) {
            MSLog.i("Loading news...(cache): " + url);
            Cache cache = Networking.getRequestQueue(context).getCache();
            Cache.Entry entry = cache.get(url);
            if(entry != null) {
                try {

                    ArrayList<News> newsArrayList = null;
                    if(url.contains(PARAM_KEYWORD_ARRAY)) {
                        newsArrayList = NewsRequest.multipleNewsParseResponse(entry.data);
                    } else {
                        newsArrayList = NewsRequest.newsParseResponse(entry.data);
                    }

                    MSLog.i("Loading news list...(cache hit): " + new String(entry.data));
                    mMarketSenseNewsNetworkListener.onNewsLoad(newsArrayList, true);
                    // if it is cached, we do not need to do network query.
                    return;
                } catch (JSONException e) {
                    MSLog.e("Loading news list...(cache failed JSONException)");
                }
            } else {
                MSLog.i("Loading news...(cache miss)");
            }
        }


        mNewsRequest = new NewsRequest(Request.Method.GET, url, null, new Response.Listener<ArrayList<News>>() {
            @Override
            public void onResponse(ArrayList<News> response) {
                MSLog.i("News Request success: " + response);
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                mMarketSenseNewsNetworkListener.onNewsLoad(response, false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MSLog.e("News Request error: " + error.getMessage(), error);
                if(error.networkResponse != null) {
                    MSLog.e("News Request error: " + new String(error.networkResponse.data), error);
                }
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                if(error instanceof MarketSenseNetworkError) {
                    MarketSenseNetworkError networkError = (MarketSenseNetworkError) error;
                    mMarketSenseNewsNetworkListener.onNewsFail(networkError.getReason());
                } else {
                    mMarketSenseNewsNetworkListener.onNewsFail(MarketSenseError.NETWORK_VOLLEY_ERROR);
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

    void destroy() {
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
}
