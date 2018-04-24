package com.idroi.marketsense.datasource;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.data.News;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public class NewsSource {

    public interface NewsSourceListener {
        void onNewsAvailable();
    }

    private static final int DEFAULT_CACHE_LIMIT = 20;

    private final ArrayList<News> mNewsCache;

    private MarketSenseNewsFetcher mNewsFetcher;
    private MarketSenseNewsFetcher.MarketSenseNewsNetworkListener mMarketSenseNewsNetworkListener;

    private static final int MAXIMUM_RETRY_TIME_MILLISECONDS = 5 * 60 * 1000; // 5 minutes.
    private static final int[] RETRY_TIME_ARRAY_MILLISECONDS = new int[]{1000, 3000, 5000, 25000, 60000, MAXIMUM_RETRY_TIME_MILLISECONDS};

    private WeakReference<Activity> mActivity;
    private Handler mReplenishCacheHandler;
    private Runnable mReplenishCacheRunnable;
    private boolean mRequestInFlight;
    private boolean mRetryInFlight;
    private int mCurrentRetries = 0;
    private boolean mFirstTimeNewsAvailable;
    private NewsSourceListener mNewsSourceListener;

    private String mUrl;

    NewsSource(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
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
            public void onNewsLoad(ArrayList<News> newsArray) {

                boolean moreFlag = false;

                if(mNewsFetcher == null) {
                    return;
                }

                mRequestInFlight = false;
                resetRetryTime();

                for(int i = 0; i < newsArray.size(); i++) {
                    if(!mNewsCache.contains(newsArray.get(i))) {
                        mNewsCache.add(newsArray.get(i));
                        moreFlag = true;
                    }
                }

                if(!mFirstTimeNewsAvailable) {
                    mFirstTimeNewsAvailable = true;
                    if(mNewsSourceListener != null) {
                        mNewsSourceListener.onNewsAvailable();
                    }
                }

                if(moreFlag) {
                    replenishCache();
                } else if(mActivity.get() != null) {
                    Toast.makeText(mActivity.get(), "No more data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNewsFail(MarketSenseError marketSenseError) {
                MSLog.e(marketSenseError.toString());

                mRequestInFlight = false;

                if (mCurrentRetries >= RETRY_TIME_ARRAY_MILLISECONDS.length - 1) {
                    MSLog.w("Stopping requests after the max retry count.");
                    resetRetryTime();
                    return;
                }

                mRetryInFlight = true;
                MSLog.w("Wait for " + getRetryTime() + " milliseconds.");
                mReplenishCacheHandler.postDelayed(mReplenishCacheRunnable, getRetryTime());
                updateRetryTime();
            }
        };

        mRequestInFlight = false;
        mFirstTimeNewsAvailable = false;
        resetRetryTime();
    }

    public void setNewsSourceListener(NewsSourceListener listener) {
        mNewsSourceListener = listener;
    }

    News dequeueNews() {
        if (!mRequestInFlight && !mRetryInFlight) {
            mReplenishCacheHandler.post(mReplenishCacheRunnable);
        }

        if(!mNewsCache.isEmpty()) {
            return mNewsCache.remove(0);
        }

        MSLog.w("News data is empty. Please wait for seconds");
        return null;
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

    public void loadNews(Activity activity, String url) {
        mUrl = url;
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
        mRequestInFlight = false;
        mFirstTimeNewsAvailable = false;
        mReplenishCacheHandler.removeCallbacks(mReplenishCacheRunnable);
    }

    private void replenishCache() {
        if(!mRequestInFlight && mNewsFetcher != null && mNewsCache.size() < DEFAULT_CACHE_LIMIT) {
            mRequestInFlight = true;
            mNewsFetcher.makeRequest(mUrl);
        }
    }
}
