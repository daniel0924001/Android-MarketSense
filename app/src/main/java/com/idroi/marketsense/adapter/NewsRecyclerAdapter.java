package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.Stock;
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

    public interface NewsAvailableListener {
        void onNewsAvailable();
    }

    public interface OnItemClickListener {
        void onItemClick(News stock);
    }

    private Activity mActivity;
    private NewsStreamPlacer mNewsStreamPlacer;
    private Handler mHandler;

    // TODO: maybe multiple renderer in someday
    private NewsRenderer mNewsRenderer;
    private NewsExpandListener mNewsExpandListener;
    private OnItemClickListener mOnItemClickListener;
    private NewsAvailableListener mNewsAvailableListener;

    public NewsRecyclerAdapter(final Activity activity) {
        mActivity = activity;
        mHandler = new Handler();
        mNewsStreamPlacer = new NewsStreamPlacer(activity);
        mNewsRenderer = new NewsRenderer();
        mNewsStreamPlacer.setNewsSourceListener(new NewsSource.NewsSourceListener() {
            @Override
            public void onNewsAvailable() {
                if(mNewsAvailableListener != null) {
                    mNewsAvailableListener.onNewsAvailable();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        expand(15);
                    }
                });
            }

            @Override
            public void onNotifyRemove() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int size = mNewsStreamPlacer.clearNews();
                        notifyItemRangeRemoved(0, size);
                    }
                });

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
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        expand(15);
                    }
                }, 1000);
            }
        };
    }

    public void setNewsAvailableListener(NewsAvailableListener listener) {
        mNewsAvailableListener = listener;
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mNewsRenderer.createView(mActivity, parent));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        News news = mNewsStreamPlacer.getNewsData(position);
        if(news != null) {
            mNewsRenderer.renderView(holder.itemView, news);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(mNewsStreamPlacer.getNewsData(holder.getAdapterPosition()));
                }
            });
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
