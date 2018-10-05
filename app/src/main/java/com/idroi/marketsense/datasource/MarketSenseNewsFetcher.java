package com.idroi.marketsense.datasource;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
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
import com.idroi.marketsense.common.SharedPreferencesCompat;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.util.MarketSenseUtils;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_REQUEST_NAME;
import static com.idroi.marketsense.request.NewsRequest.PARAM_GTS;
import static com.idroi.marketsense.request.NewsRequest.PARAM_KEYWORD_ARRAY;
import static com.idroi.marketsense.request.NewsRequest.PARAM_LEVEL;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS_FALLING;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS_RISING;

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

    private void requestNews(ArrayList<String> networkUrls, @Nullable ArrayList<String> cacheUrls, boolean shouldReadFromCache) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(networkUrls == null) {
            MSLog.e("[ERROR]: networkUrls should not be null." );
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
        final AtomicBoolean networkAnySuccesses = new AtomicBoolean(false);
        int cacheCounter = cacheUrls.size();

        if(shouldReadFromCache) {
            Cache cache = Networking.getRequestQueue(context).getCache();
            ArrayList<News> newsArrayList = new ArrayList<>();
            for(String cacheUrl : cacheUrls) {
                MSLog.i("Loading news...(cache): " + cacheUrl);
                Cache.Entry entry = cache.get(cacheUrl);
                if (entry != null && !entry.isExpired()) {
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
                    MSLog.i("Loading news...(cache miss or expired)");
                    break;
                }
            }
        }

        final ArrayList<News> results = new ArrayList<>();
        Response.Listener<ArrayList<News>> responseListener = new Response.Listener<ArrayList<News>>() {
            @Override
            public void onResponse(ArrayList<News> response) {
                final Context context = getContextOrDestroy();
                if(context == null) {
                    results.clear();
                    return;
                }

                results.addAll(response);

                final int count = networkCounter.decrementAndGet();
                networkAnySuccesses.set(true);
                MSLog.i("News Request success and remain: " + count);

                if(count == 0 && networkAnySuccesses.get()){
                    MSLog.i("News Request success");
                    mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                    mMarketSenseNewsNetworkListener.onNewsLoad(results, false);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final Context context = getContextOrDestroy();
                if(context == null) {
                    results.clear();
                    return;
                }
                onError(networkCounter, networkAnySuccesses, results, error);
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

    private void onError(final AtomicInteger networkCounter,
                                final AtomicBoolean anySuccesses,
                                ArrayList<News> results,
                                final VolleyError error){

        final int count = networkCounter.decrementAndGet();
        MSLog.i("News Request error and remain: " + count);
        if(count == 0){
            if(!anySuccesses.get()) {
                MSLog.e("News Request error: " + error.getMessage(), error);
                if (error.networkResponse != null) {
                    MSLog.e("News Request error: " + new String(error.networkResponse.data), error);
                }
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                if (error instanceof MarketSenseNetworkError) {
                    MarketSenseNetworkError networkError = (MarketSenseNetworkError) error;
                    mMarketSenseNewsNetworkListener.onNewsFail(networkError.getReason());
                } else {
                    mMarketSenseNewsNetworkListener.onNewsFail(MarketSenseError.NETWORK_VOLLEY_ERROR);
                }
            } else {
                MSLog.i("News Request success");
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                mMarketSenseNewsNetworkListener.onNewsLoad(results, false);
            }
        }
    }

    public static void prefetchNewsFirstPage(final Context context) {
        final Context applicationContext = context.getApplicationContext();
        final ArrayList<String> networkUrls = new ArrayList<>();

        final ArrayList<String> statusArrayList = new ArrayList<>();
        final ArrayList<Integer> levelArrayList = new ArrayList<>();
        statusArrayList.add(PARAM_STATUS_RISING);
        statusArrayList.add(PARAM_STATUS_FALLING);
        levelArrayList.add(3);
        levelArrayList.add(-3);

        long now = System.currentTimeMillis() / 1000;
        final String gts = String.valueOf(now - 86400);

        for(int i = 0; i < statusArrayList.size(); i++) {
            String temp = NewsRequest.queryNewsUrl(
                    applicationContext,
                    statusArrayList.get(i),
                    levelArrayList.get(i),
                    true,
                    gts);
            networkUrls.add(temp);
        }

        final AtomicInteger networkCounter = new AtomicInteger(networkUrls.size());
        final AtomicBoolean networkAnySuccesses = new AtomicBoolean(false);

        Response.Listener<ArrayList<News>> responseListener = new Response.Listener<ArrayList<News>>() {
            @Override
            public void onResponse(ArrayList<News> response) {

                final int count = networkCounter.decrementAndGet();
                networkAnySuccesses.set(true);
                MSLog.i("Prefetch News Request success and remain: " + count);

                if(count == 0 && networkAnySuccesses.get()){
                    MSLog.i("Prefetch News Request success");
                    prefetchNewsFirstPageDone(applicationContext, statusArrayList, levelArrayList, networkUrls, gts);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final int count = networkCounter.decrementAndGet();
                MSLog.i("Prefetch News Request error and remain: " + count);
                if(count == 0){
                    if(!networkAnySuccesses.get()) {
                        MSLog.e("Prefetch News Request error: " + error.getMessage(), error);
                        if (error.networkResponse != null) {
                            MSLog.e("Prefetch News Request error: " + new String(error.networkResponse.data), error);
                        }
                    } else {
                        MSLog.i("Prefetch News Request success");
                        prefetchNewsFirstPageDone(applicationContext, statusArrayList, levelArrayList, networkUrls, gts);
                    }
                }
            }
        };

        RequestQueue requestQueue = Networking.getRequestQueue(applicationContext);
        for(String networkUrl : networkUrls) {
            NewsRequest newsRequest = new NewsRequest(Request.Method.GET, networkUrl, null, responseListener, errorListener);
            requestQueue.add(newsRequest);
        }
    }

    private static void prefetchNewsFirstPageDone(Context context,
                                                  ArrayList<String> statusArrayList,
                                                  ArrayList<Integer> levelArrayList,
                                                  ArrayList<String> networkUrls,
                                                  String gts) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE).edit();
        for(int i = 0; i < statusArrayList.size(); i++) {
            String key = NewsRequest.queryNewsUrlPrefix(statusArrayList.get(i), levelArrayList.get(i), gts);
            editor.putString(key, networkUrls.get(i));
            MSLog.d("News network query success, so we save this network url to cache: " + key + ", " + networkUrls.get(i));
        }
        SharedPreferencesCompat.apply(editor);
    }
}
