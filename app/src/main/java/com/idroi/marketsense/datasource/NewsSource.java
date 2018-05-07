package com.idroi.marketsense.datasource;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.request.NewsRequest;

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

    private MarketSenseNewsFetcher mNewsFetcher;
    private MarketSenseNewsFetcher.MarketSenseNewsNetworkListener mMarketSenseNewsNetworkListener;

    private static final int MAXIMUM_RETRY_TIME_MILLISECONDS = 5 * 60 * 1000; // 5 minutes.
    private static final int[] RETRY_TIME_ARRAY_MILLISECONDS = new int[]{1000, 3000, 5000, 25000, 60000, MAXIMUM_RETRY_TIME_MILLISECONDS};

    private WeakReference<Activity> mActivity;
//    private Handler mReplenishCacheHandler;
//    private Runnable mReplenishCacheRunnable;
    private boolean mRequestInFlight;
    private boolean mRetryInFlight;
    private int mCurrentRetries = 0;
    private boolean mFirstTimeNewsAvailable;
    private NewsSourceListener mNewsSourceListener;

    private String mUrl;
    private int mSequenceNumber;
    private boolean mShouldReadFromCache;
    private boolean mHasShowNoMore = false;

    NewsSource(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
        mNewsCache = new ArrayList<News>();
//        mReplenishCacheHandler = new Handler();
//        mReplenishCacheRunnable = new Runnable() {
//            @Override
//            public void run() {
//                mRetryInFlight = false;
//                replenishCache();
//            }
//        };

        mMarketSenseNewsNetworkListener = new MarketSenseNewsFetcher.MarketSenseNewsNetworkListener() {
            @Override
            public void onNewsLoad(ArrayList<News> newsArray, boolean isCache) {

                if(mIsCache && isCache) {
                    return;
                }

                if(mIsCache && !isCache) {
                     // mIsCache = true;  last data is from cache
                     // isCache = false;  recent data is from network
                     // we have to clear cache data if the data is from network
                    mNewsCache.clear();
                    mNewsSourceListener.onNotifyRemove();
                    mIsCache = false;
                }

                boolean moreFlag = false;

                if(mNewsFetcher == null || mActivity.get() == null) {
                    return;
                }

                mRequestInFlight = false;
                mSequenceNumber++;
                resetRetryTime();

                Comparator<News> comparator = genComparator();
                Collections.sort(newsArray, comparator);
                for(int i = 0; i < newsArray.size(); i++) {
                    if(!mNewsCache.contains(newsArray.get(i))) {
                        mNewsCache.add(newsArray.get(i));
                        moreFlag = true;
                        mHasShowNoMore = false;
                    }
                }

                if(!mFirstTimeNewsAvailable) {
                    mShouldReadFromCache = false;
                    mFirstTimeNewsAvailable = true;
                    if(mNewsSourceListener != null) {
                        mNewsSourceListener.onNewsAvailable();
                    }
                }

                // we only query one time
//                if(moreFlag) {
//                    replenishCache();
//                } else if(mActivity.get() != null && !mHasShowNoMore) {
//                    mHasShowNoMore = true;
//                    MSLog.w("no more data, maybe duplicate");
//                    Toast.makeText(mActivity.get(), "暫時沒有新聞資料，請稍候...", Toast.LENGTH_SHORT).show();
//                }
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
                MSLog.w("Wait for " + getRetryTime() + " milliseconds. (Do nothing in this version)");
//                mReplenishCacheHandler.postDelayed(mReplenishCacheRunnable, getRetryTime());
                updateRetryTime();
            }
        };

        mRequestInFlight = false;
        mFirstTimeNewsAvailable = false;
        mShouldReadFromCache = true;
        mSequenceNumber = 0;
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
        mShouldReadFromCache = true;
        mSequenceNumber = 0;
//        mReplenishCacheHandler.removeCallbacks(mReplenishCacheRunnable);
    }

    private void replenishCache() {
        if(!mRequestInFlight && mNewsFetcher != null && mNewsCache.size() < DEFAULT_CACHE_LIMIT) {
            mRequestInFlight = true;
//            mNewsFetcher.makeRequest(NewsRequest.appendMagicString(mUrl, mSequenceNumber), mShouldReadFromCache);
            mNewsFetcher.makeRequest(mUrl, mShouldReadFromCache);
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
}
