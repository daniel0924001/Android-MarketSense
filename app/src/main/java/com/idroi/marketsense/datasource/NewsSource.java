package com.idroi.marketsense.datasource;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.SharedPreferencesCompat;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.request.NewsRequest;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_REQUEST_NAME;
import static com.idroi.marketsense.common.MarketSenseError.JSON_PARSED_NO_DATA;
import static com.idroi.marketsense.fragments.NewsFragment.GENERAL_TASK_ID;
import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_ARRAY_TASK_ID;
import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_NAME;
import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_TASK_ID;
import static com.idroi.marketsense.request.NewsRequest.PARAM_GTS;
import static com.idroi.marketsense.request.NewsRequest.PARAM_LEVEL;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public class NewsSource {

    public interface NewsSourceListener {
        void onNewsAvailable();
        void onNotifyRemove();
    }

    private static final int DEFAULT_CACHE_LIMIT = 20;

    private final ArrayList<News> mNewsCache;
    private boolean mIsCache = false;
    private int mNewsTotalCount;

    private MarketSenseNewsFetcher mNewsFetcher;
    private MarketSenseNewsFetcher.MarketSenseNewsNetworkListener mMarketSenseNewsNetworkListener;

    private static final int MAXIMUM_RETRY_TIME_MILLISECONDS = 5 * 1000; // 5 minutes.
    private static final int[] RETRY_TIME_ARRAY_MILLISECONDS = new int[]{1000, 1000, MAXIMUM_RETRY_TIME_MILLISECONDS};

    private WeakReference<Activity> mActivity;
    private Handler mReplenishCacheHandler;
    private Runnable mReplenishCacheRunnable;
    private boolean mRequestInFlight;
    private boolean mRetryInFlight;
    private int mCurrentRetries = 0;
    private boolean mFirstTimeNewsAvailable;
    private NewsSourceListener mNewsSourceListener;

    private ArrayList<String> mNetworkUrl, mCacheUrl;
    private int mSequenceNumber;
    private boolean mShouldReadFromCache;

    private int mTaskId;
    private Bundle mBundle;

    NewsSource(Activity activity, int taskId, Bundle bundle) {
        mActivity = new WeakReference<Activity>(activity);
        mTaskId = taskId;
        mBundle = bundle;
        mNewsCache = new ArrayList<News>();
        mReplenishCacheHandler = new Handler();
        mReplenishCacheRunnable = new Runnable() {
            @Override
            public void run() {
                mRetryInFlight = false;
                replenishCache();
            }
        };

        mMarketSenseNewsNetworkListener = new MarketSenseNewsFetcher.MarketSenseNewsNetworkListener() {
            @Override
            public void onNewsLoad(ArrayList<News> newsArray, boolean isCache) {
                MSLog.d("[news request]: loaded mIsCache (" + mIsCache + "), isCache (" + isCache + ").");

                if(mIsCache && isCache) {
                    return;
                }

                if(mNewsFetcher == null || mActivity.get() == null) {
                    return;
                }

                mRequestInFlight = false;
                mSequenceNumber++;
                resetRetryTime();

                if(!mFirstTimeNewsAvailable) {
                    int counter = 0;
                    if(newsArray != null) {
                        Comparator<News> comparator = genComparator();
                        Collections.sort(newsArray, comparator);
                        for (int i = 0; i < newsArray.size(); i++) {
                            if (!mNewsCache.contains(newsArray.get(i))) {
                                counter++;
                                mNewsCache.add(newsArray.get(i));
                            }
                        }
                        MSLog.d("[news request]: add " + counter + " news.");
                        mNewsTotalCount += counter;
                    }

                    mShouldReadFromCache = false;
                    mFirstTimeNewsAvailable = true;
                    if(mNewsSourceListener != null) {
                        mNewsSourceListener.onNewsAvailable();
                        if(!mIsCache) {
                            mIsCache = isCache;
                        }
                    }
                }

                if(!isCache) {
                    MSLog.d("[news request]: write to shared preference.");
                    writeToSharedPreference();
                }
            }

            @Override
            public void onNewsFail(MarketSenseError marketSenseError) {
                MSLog.e(marketSenseError.toString());

                mRequestInFlight = false;

                if (JSON_PARSED_NO_DATA.isEqual(marketSenseError) || mCurrentRetries >= RETRY_TIME_ARRAY_MILLISECONDS.length - 1) {
                    MSLog.w("Json is null or stop requests after the max retry count.");
                    resetRetryTime();
                    mNewsSourceListener.onNewsAvailable();
                    return;
                }

                mRetryInFlight = true;
                MSLog.w("Wait for " + getRetryTime() + " milliseconds. (Do nothing in this version)");
                if(mIsCache || !mFirstTimeNewsAvailable) {
                    mReplenishCacheHandler.postDelayed(mReplenishCacheRunnable, getRetryTime());
                }
                updateRetryTime();
            }
        };

        mIsCache = false;
        mRequestInFlight = false;
        mFirstTimeNewsAvailable = false;
        mShouldReadFromCache = true;
        mSequenceNumber = 0;
        mNewsTotalCount = 0;
        resetRetryTime();
    }

    public void setNewsSourceListener(NewsSourceListener listener) {
        mNewsSourceListener = listener;
    }

    News dequeueNews() {
        if (!mRequestInFlight && !mRetryInFlight) {
//            mReplenishCacheHandler.post(mReplenishCacheRunnable);
        }

        if(!mNewsCache.isEmpty()) {
            return mNewsCache.remove(0);
        }

        MSLog.w("News data is empty. Please wait for seconds");
        return null;
    }

    public boolean isCacheEmpty() {
        return mNewsCache == null || mNewsCache.isEmpty();
    }

    private int getRetryTime() {
        if (mCurrentRetries >= RETRY_TIME_ARRAY_MILLISECONDS.length) {
            mCurrentRetries = RETRY_TIME_ARRAY_MILLISECONDS.length - 1;
        }
        return RETRY_TIME_ARRAY_MILLISECONDS[mCurrentRetries];
    }

    private void updateRetryTime() {
        if (mCurrentRetries < RETRY_TIME_ARRAY_MILLISECONDS.length - 1) {
            mCurrentRetries++;
        }
    }

    private void resetRetryTime() {
        mCurrentRetries = 0;
    }

    public void loadNews(Activity activity, ArrayList<String> networkUrl, ArrayList<String> cacheUrl) {
        mNetworkUrl = networkUrl;
        mCacheUrl = cacheUrl;
        loadNews(new MarketSenseNewsFetcher(activity, mMarketSenseNewsNetworkListener));
    }

    private void loadNews(MarketSenseNewsFetcher newsFetcher) {
        clear();
        mNewsFetcher = newsFetcher;
        replenishCache();
    }

    public void clear() {
        mNewsCache.clear();
        if(mNewsFetcher != null) {
            mNewsFetcher.destroy();
            mNewsFetcher = null;
        }
        mIsCache = false;
        mRequestInFlight = false;
        mFirstTimeNewsAvailable = false;
        mShouldReadFromCache = true;
        mSequenceNumber = 0;
        mNewsTotalCount = 0;
        mReplenishCacheHandler.removeCallbacks(mReplenishCacheRunnable);
    }

    private void replenishCache() {
        if(!mRequestInFlight && mNewsFetcher != null && mNewsCache.size() < DEFAULT_CACHE_LIMIT) {
            mRequestInFlight = true;
//            mNewsFetcher.makeRequest(NewsRequest.appendMagicString(mUrl, mSequenceNumber), mShouldReadFromCache);
            mNewsFetcher.makeRequest(mNetworkUrl, mCacheUrl, mShouldReadFromCache);
        }
    }

    private Comparator<News> genComparator() {
        return new Comparator<News>() {
            @Override
            public int compare(News news1, News news2) {
                return news2.getSourceDateInt() - news1.getSourceDateInt();
            }
        };
    }

    public void writeToSharedPreference() {
        Activity activity = mActivity.get();
        if(activity == null) {
            return;
        }

        if(mBundle == null) {
            return;
        }

        String key = null;
        SharedPreferences.Editor editor =
                activity.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE).edit();
        switch (mTaskId) {
            case GENERAL_TASK_ID:
                ArrayList<String> statusArrayList = mBundle.getStringArrayList(PARAM_STATUS);
                ArrayList<Integer> levelArrayList = mBundle.getIntegerArrayList(PARAM_LEVEL);
                String gts = mBundle.getString(PARAM_GTS);
                if(statusArrayList == null || levelArrayList == null || mNetworkUrl == null ||
                        statusArrayList.size() != levelArrayList.size() ||
                        statusArrayList.size() != mNetworkUrl.size()) {
                    MSLog.e("size of statusArrayList and levelArrayList and mNetworkUrl is not equal.");
                    return;
                }
                for(int i = 0; i < statusArrayList.size(); i++) {
                    key = NewsRequest.queryNewsUrlPrefix(statusArrayList.get(i), levelArrayList.get(i), gts);
                    editor.putString(key, mNetworkUrl.get(i));
                    MSLog.d("News network query success, so we save this network url to cache: " + key + ", " + mNetworkUrl.get(i));
                }
                break;
            case KEYWORD_TASK_ID:
                key = NewsRequest.queryKeywordNewsUrlPrefix(mBundle.getString(KEYWORD_NAME));
                if(mNetworkUrl != null && mNetworkUrl.size() > 0 && mNetworkUrl.get(0) != null) {
                    editor.putString(key, mNetworkUrl.get(0));
                    MSLog.d("Keyword news network query success, so we save this network url to cache: " + key + ", " + mNetworkUrl.get(0));
                }
                break;
            case KEYWORD_ARRAY_TASK_ID:
                key = NewsRequest.queryKeywordArrayNewsUrlPrefix();
                if(mNetworkUrl != null && mNetworkUrl.size() > 0 && mNetworkUrl.get(0) != null) {
                    editor.putString(key, mNetworkUrl.get(0));
                    MSLog.d("Keyword array news network query success, so we save this network url to cache: " + key + ", " + mNetworkUrl.get(0));
                }
                break;
        }
        SharedPreferencesCompat.apply(editor);
    }

    public int getNewsTotalCount() {
        return mNewsTotalCount;
    }
}
