package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.datasource.NewsSource;
import com.idroi.marketsense.datasource.NewsStreamPlacer;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public class NewsRecyclerAdapter extends RecyclerView.Adapter {

    public static final String TAG = "NewsRecyclerAdapter";

    public interface NewsExpandListener {
        void onExpandSuccess(int start, int amount);
        void onExpandFailed();
    }

    private Activity mActivity;
    private NewsStreamPlacer mNewsStreamPlacer;
    private Handler mHandler;

    // TODO: maybe multiple renderer in someday
    private NewsRenderer mNewsRenderer;
    private NewsExpandListener mNewsExpandListener;

    public NewsRecyclerAdapter(final Activity activity) {
        mActivity = activity;
        mHandler = new Handler();
        mNewsStreamPlacer = new NewsStreamPlacer(activity);
        mNewsRenderer = new NewsRenderer();
        mNewsStreamPlacer.setNewsSourceListener(new NewsSource.NewsSourceListener() {
            @Override
            public void onNewsAvailable() {
                expand(15);
            }
        });

        mNewsExpandListener = new NewsExpandListener() {
            @Override
            public void onExpandSuccess(final int start, final int amount) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemRangeInserted(start, amount);
                    }
                });
            }

            @Override
            public void onExpandFailed() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        expand(15);
                    }
                }, 1000);
            }
        };
    }

    public void loadNews(String url) {
        mNewsStreamPlacer.loadNews(url);
    }

    public void expand(int number) {
        mNewsStreamPlacer.expandNews(number, mNewsExpandListener);
    }

    public void setNewsExpandListener(NewsExpandListener listener) {
        mNewsExpandListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mNewsRenderer.createView(mActivity, parent));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        News news = mNewsStreamPlacer.getNewsData(position);
        if(news != null) {
            mNewsRenderer.renderView(holder.itemView, news);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mNewsStreamPlacer.getItemCount();
    }

    public void destroy() {
        mNewsRenderer.clear();
        mNewsStreamPlacer.clear();
    }
}
