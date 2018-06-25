package com.idroi.marketsense.datasource;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;

import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.data.News;

/**
 * Created by daniel.hsieh on 2018/4/19.
 */

public class NewsStreamPlacer {

    private ArrayList<News> mNewsArrayList;

    private NewsSource mNewsSource;
    private Activity mActivity;


    public NewsStreamPlacer(Activity activity, int taskId, Bundle bundle) {
        mActivity = activity;
        mNewsArrayList = new ArrayList<News>();
        mNewsSource = new NewsSource(mActivity, taskId, bundle);
    }

    public void setNewsSourceListener(NewsSource.NewsSourceListener listener) {
        mNewsSource.setNewsSourceListener(listener);
    }

    public int clearNews() {
        if(mNewsArrayList != null) {
            int size = mNewsArrayList.size();
            mNewsArrayList.clear();
            return size;
        }
        return 0;
    }

    public void loadNews(ArrayList<String> networkUrl, ArrayList<String> cacheUrl) {
        mNewsSource.loadNews(mActivity, networkUrl, cacheUrl);
    }

    public void expandNews(final int number, final NewsRecyclerAdapter.NewsExpandListener listener) {
        if(mNewsArrayList == null) {
            return;
        }

        int increaseAmount = 0;
        int start = mNewsArrayList.size();
        for(int i = 0; i < number; i++) {
            News news = mNewsSource.dequeueNews();
            if(news != null) {
                if(!mNewsArrayList.contains(news)) {
                    mNewsArrayList.add(news);
                    increaseAmount++;
                }
            } else {
                break;
            }
        }

        if(listener != null) {
            if (increaseAmount > 0) {
                listener.onExpandSuccess(start, increaseAmount);
            } else {
                listener.onExpandFailed();
            }
        }
    }

    public boolean isEmpty() {
        return (mNewsArrayList == null || mNewsArrayList.isEmpty())
                && (mNewsSource == null || mNewsSource.isCacheEmpty());
    }

    public int getItemCount() {
        if(mNewsArrayList != null) {
            return mNewsArrayList.size();
        } else {
            return 0;
        }
    }

    public void clear() {
        if(mNewsArrayList != null) {
            mNewsArrayList.clear();
            mNewsArrayList = null;
        }
        if(mNewsSource != null) {
            mNewsSource.clear();
            mNewsSource = null;
        }
    }

    public News getNewsData(int position) {
        if(mNewsArrayList == null || position >= mNewsArrayList.size() || position < 0) {
            return null;
        }
        return mNewsArrayList.get(position);
    }

    public int getNewsTotalCount() {
        return mNewsSource.getNewsTotalCount();
    }

}
