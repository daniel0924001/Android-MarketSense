package com.idroi.marketsense.datasource;

import android.app.Activity;

import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.data.Knowledge;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by daniel.hsieh on 2018/9/19.
 */

public class KnowledgePlacer {

    public interface KnowledgeListListener {
        void onKnowledgeListLoaded();
    }

    private static final int RETRY_TIME_CONST = 2;
    private int mCurrentRetries = 0;

    private WeakReference<Activity> mActivity;
    private KnowledgeFetcher.KnowledgeNetworkListener mKnowledgeNetworkListener;

    private ArrayList<Knowledge> mKnowledgeArrayList;
    private HashMap<String, ArrayList<Knowledge>> mKnowledgeHashMap;

    private KnowledgeListListener mKnowledgeListListener;
    private KnowledgeFetcher mKnowledgeFetcher;

    private String mNetworkUrl;
    private String mCacheUrl;

    public KnowledgePlacer(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
        mKnowledgeNetworkListener = new KnowledgeFetcher.KnowledgeNetworkListener() {
            @Override
            public void onKnowledgeNetworkLoad(HashMap<String, ArrayList<Knowledge>> knowledgeHashMap) {

                if(mActivity.get() == null) {
                    return;
                }

                // initialize the category
                mKnowledgeHashMap = knowledgeHashMap;
                setKnowledgeCategoryList("入門");
                if(isEmpty()) {
                    // we do not want to refresh again
                    if (mKnowledgeListListener != null) {
                        mKnowledgeListListener.onKnowledgeListLoaded();
                    }
                }
            }

            @Override
            public void onKnowledgeNetworkFail(MarketSenseError marketSenseError) {
                if(mActivity.get() == null) {
                    return;
                }

                increaseRetryTime();
                if(isRetry()) {
                    if(mKnowledgeFetcher != null) {
                        mKnowledgeFetcher.makeRequest(mNetworkUrl, mCacheUrl);
                    }
                } else {
                    if(mKnowledgeListListener != null) {
                        mKnowledgeListListener.onKnowledgeListLoaded();
                    }
                }
            }
        };
        resetRetryTime();
    }

    private boolean isRetry() {
        return mCurrentRetries <= RETRY_TIME_CONST;
    }

    private void increaseRetryTime() {
        if(mCurrentRetries <= RETRY_TIME_CONST) {
            mCurrentRetries++;
        }
    }

    private void resetRetryTime() {
        mCurrentRetries = 0;
    }

    public void setKnowledgeList(ArrayList<Knowledge> knowledgeList) {
        mKnowledgeArrayList = knowledgeList;
    }

    public void setKnowledgeCategoryList(String category) {
        if(mKnowledgeHashMap != null) {
            mKnowledgeArrayList = mKnowledgeHashMap.get(category);
        }
    }

    public void setKnowledgeListListener(KnowledgeListListener listListener) {
        mKnowledgeListListener = listListener;
    }

    public boolean isEmpty() {
        return mKnowledgeArrayList == null || mKnowledgeArrayList.size() == 0;
    }

    public void loadKnowledgeList(String networkUrl, String cacheUrl) {
        Activity activity = mActivity.get();
        if(activity == null) {
            return;
        }

        mNetworkUrl = networkUrl;
        mCacheUrl = cacheUrl;
        loadKnowledgeList(new KnowledgeFetcher(activity, mKnowledgeNetworkListener));
    }

    private void loadKnowledgeList(KnowledgeFetcher knowledgeFetcher) {
        clear();
        mKnowledgeFetcher = knowledgeFetcher;
        mKnowledgeFetcher.makeRequest(mNetworkUrl, mCacheUrl);
    }

    public Knowledge getKnowledge(int position) {
        if(mKnowledgeArrayList == null || position >= mKnowledgeArrayList.size() || position < 0) {
            return null;
        }
        return mKnowledgeArrayList.get(position);
    }

    public void clear() {
        if(mKnowledgeArrayList != null) {
            mKnowledgeArrayList.clear();
            mKnowledgeArrayList = null;
        }
        if(mKnowledgeFetcher != null) {
            mKnowledgeFetcher.destroy();
            mKnowledgeFetcher = null;
        }
        if(mKnowledgeHashMap != null) {
            mKnowledgeHashMap.clear();
            mKnowledgeHashMap = null;
        }
    }

    public int getItemCount() {
        if(mKnowledgeArrayList != null) {
            return mKnowledgeArrayList.size();
        } else {
            return 0;
        }
    }
}
