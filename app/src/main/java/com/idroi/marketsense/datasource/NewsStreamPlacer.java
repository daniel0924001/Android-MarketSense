package com.idroi.marketsense.datasource;

import android.app.Activity;

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


    public NewsStreamPlacer(Activity activity) {
        mActivity = activity;
        mNewsArrayList = new ArrayList<News>();
        mNewsSource = new NewsSource(mActivity);
    }

    public void setNewsSourceListener(NewsSource.NewsSourceListener listener) {
        mNewsSource.setNewsSourceListener(listener);
    }

    public void loadNews() {
        mNewsSource.loadNews(mActivity);
    }

    public void expandNews(final int number, final NewsRecyclerAdapter.NewsExpandListener listener) {
        int increaseAmount = 0;
        int start = mNewsArrayList.size();
        for(int i = 0; i < number; i++) {
            News news = mNewsSource.dequeueNews();
            if(news != null) {
                mNewsArrayList.add(news);
                increaseAmount++;
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
    }

    public News getNewsData(int position) {
        if(mNewsArrayList == null || position > mNewsArrayList.size() || position < 0) {
            return null;
        }
        return mNewsArrayList.get(position);
    }

}
