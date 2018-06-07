package com.idroi.marketsense.datasource;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ArrayList<NewsRequest> mNewsRequests;

    MarketSenseNewsFetcher(Context context,
            MarketSenseNewsNetworkListener marketSenseNewsNetworkListener) {
        mContext = new WeakReference<Context>(context);
        mMarketSenseNewsNetworkListener = marketSenseNewsNetworkListener;
        mNewsRequests = new ArrayList<>();
        mTimeoutHandler = new Handler();
        mTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                for(NewsRequest newsRequest : mNewsRequests) {
                    if (newsRequest != null) {
                        newsRequest.cancel();
                        newsRequest = null;
                    }
                }
                mNewsRequests.clear();
                mMarketSenseNewsNetworkListener.onNewsFail(MarketSenseError.NETWORK_CONNECTION_TIMEOUT);
            }
        };
    }

    void makeRequest(@NonNull ArrayList<String> networkUrl, @Nullable ArrayList<String> cacheUrl, boolean shouldReadFromCache) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(!MarketSenseUtils.isNetworkAvailable(context)) {
            mMarketSenseNewsNetworkListener.onNewsFail(MarketSenseError.NETWORK_CONNECTION_FAILED);
            return;
        }

        requestNews(networkUrl, cacheUrl, shouldReadFromCache);
    }

    private void requestNews(@NonNull ArrayList<String> networkUrls, @Nullable ArrayList<String> cacheUrls, boolean shouldReadFromCache) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        MSLog.i("Loading news...");

        if(cacheUrls == null) {
            MSLog.d("cacheUrl is null, so we set networkUrl to cacheUrl");
            cacheUrls = networkUrls;
        }

        // These Atomics are only accessed on the main thread.
        // We use Atomics here so we can change their values while keeping a reference for the inner class.
        final AtomicInteger networkCounter = new AtomicInteger(networkUrls.size());
        final AtomicBoolean networkAnyFailures = new AtomicBoolean(false);
        int cacheCounter = cacheUrls.size();

        if(shouldReadFromCache) {
            Cache cache = Networking.getRequestQueue(context).getCache();
            ArrayList<News> newsArrayList = new ArrayList<>();
            for(String cacheUrl : cacheUrls) {
                MSLog.i("Loading news...(cache): " + cacheUrl);
                Cache.Entry entry = cache.get(cacheUrl);
                if (entry != null) {
                    try {

                        if (cacheUrl.contains(PARAM_KEYWORD_ARRAY)) {
                            newsArrayList.addAll(NewsRequest.multipleNewsParseResponse(entry.data));
                        } else {
                            newsArrayList.addAll(NewsRequest.newsParseResponse(entry.data));
                        }

                        MSLog.i("Loading news list...(cache hit): " + new String(entry.data));

                        cacheCounter--;
                        if(cacheCounter == 0) {
                            MSLog.i("Loading news list...(cache all hit)");
                            mMarketSenseNewsNetworkListener.onNewsLoad(newsArrayList, true);
                        }

                    } catch (JSONException e) {
                        MSLog.e("Loading news list...(cache failed JSONException)");
                        break;
                    }
                } else {
                    MSLog.i("Loading news...(cache miss)");
                    break;
                }
            }
        }

        final ArrayList<News> results = new ArrayList<>();
        Response.Listener<ArrayList<News>> responseListener = new Response.Listener<ArrayList<News>>() {
            @Override
            public void onResponse(ArrayList<News> response) {
                results.addAll(response);

                final int count = networkCounter.decrementAndGet();
                MSLog.i("News Request success and remain: " + count);

                if(count == 0 && !networkAnyFailures.get()){
                    mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                    mMarketSenseNewsNetworkListener.onNewsLoad(results, false);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onError(networkCounter, networkAnyFailures, mMarketSenseNewsNetworkListener, error);
            }
        };

        for(String networkUrl : networkUrls) {
            NewsRequest newsRequest = new NewsRequest(Request.Method.GET, networkUrl, null, responseListener, errorListener);
            mNewsRequests.add(newsRequest);
            Networking.getRequestQueue(context).add(newsRequest);
        }

        mTimeoutHandler.postDelayed(mTimeoutRunnable, 5000);
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
        for(NewsRequest newsRequest : mNewsRequests) {
            if (newsRequest != null) {
                newsRequest.cancel();
                newsRequest = null;
            }
        }
        mNewsRequests.clear();
        mMarketSenseNewsNetworkListener = EMPTY_NETWORK_LISTENER;
        if(mTimeoutHandler != null) {
            mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
        }
    }

    private void onError(final AtomicInteger imageCounter,
                                final AtomicBoolean anyFailures,
                                final MarketSenseNewsNetworkListener networkListener,
                                final VolleyError error){

        boolean anyPreviousErrors = anyFailures.getAndSet(true);
        imageCounter.decrementAndGet();
        if(!anyPreviousErrors){

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
    }
}
