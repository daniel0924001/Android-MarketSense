package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.datasource.NewsSource;
import com.idroi.marketsense.datasource.NewsStreamPlacer;

import java.util.Locale;

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
        void onNewsEmpty();
    }

    public interface OnItemClickListener {
        void onItemClick(News stock);
    }

    public enum ITEM_TYPE {
        ITEM_FIRST_ROW,
        ITEM_SECOND_ROW,
        ITEM_ELSE
    }

    public static final int NEWS_SINGLE_LAYOUT = 1;
    public static final int NEWS_MULTIPLE_LAYOUT = 2;
    private int mInitLayoutType;

    private Activity mActivity;
    private NewsStreamPlacer mNewsStreamPlacer;
    private Handler mHandler;

    // TODO: maybe multiple renderer in someday
    private NewsRenderer mNewsRenderer;
    private NewsFirstRowRenderer mNewsFirstRowRenderer;
    private NewsSecondRowRenderer mNewsSecondRowRenderer;
    private NewsExpandListener mNewsExpandListener;
    private OnItemClickListener mOnItemClickListener;
    private NewsAvailableListener mNewsAvailableListener;

    private static final int MAXIMUM_RETRY_TIME_MILLISECONDS = 5 * 60 * 1000; // 5 minutes.
    private static final int[] RETRY_TIME_ARRAY_MILLISECONDS = new int[]{1000, 3000, 5000, 25000, 60000, MAXIMUM_RETRY_TIME_MILLISECONDS};
    private int mCurrentRetries = 0;

    public NewsRecyclerAdapter(final Activity activity, int taskId, Bundle bundle) {
        mActivity = activity;
        mHandler = new Handler();
        mNewsStreamPlacer = new NewsStreamPlacer(activity, taskId, bundle);

        mNewsRenderer = new NewsRenderer();
        mNewsFirstRowRenderer = new NewsFirstRowRenderer();
        mNewsSecondRowRenderer = new NewsSecondRowRenderer();

        mNewsStreamPlacer.setNewsSourceListener(new NewsSource.NewsSourceListener() {
            @Override
            public void onNewsAvailable() {
                if(mNewsAvailableListener != null) {
                    if(mNewsStreamPlacer.isEmpty()) {
                        mNewsAvailableListener.onNewsEmpty();
                    } else {
                        mNewsAvailableListener.onNewsAvailable();
                    }
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
                final int size = mNewsStreamPlacer.clearNews();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mInitLayoutType == NEWS_MULTIPLE_LAYOUT) {
                            if(size >= 4) {
                                notifyItemRangeRemoved(0, size - 1);
                            }
                        } else {
                            notifyItemRangeRemoved(0, size);
                        }
//                        notifyDataSetChanged();
                        MSLog.d(String.format(Locale.US, "clear %d data since there are some network data", size));
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                expand(13);
                            }
                        });
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
                        if(mInitLayoutType == NEWS_MULTIPLE_LAYOUT) {
                            if(start >= 4) {
                                notifyItemRangeInserted(start - 1, amount);
                            } else {
                                notifyItemRangeInserted(start, amount - 1);
                            }
                        } else {
                            notifyItemRangeInserted(start, amount);
                        }
                    }
                });
                resetRetryTime();
            }

            @Override
            public void onExpandFailed() {

                // we only query one time
//                if (mCurrentRetries >= RETRY_TIME_ARRAY_MILLISECONDS.length - 1) {
//                    MSLog.w("Stopping expand after the max retry count.");
//                    resetRetryTime();
//                    return;
//                }

//                MSLog.w("Wait for " + getRetryTime() + " milliseconds to expand.");
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        expand(15);
//                    }
//                }, getRetryTime());
//                updateRetryTime();
            }
        };
        mInitLayoutType = -1;
    }

    public void setNewsLayoutType(int type) {
        mInitLayoutType = type;
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

    public void setNewsAvailableListener(NewsAvailableListener listener) {
        mNewsAvailableListener = listener;
    }

    public void loadNews(String networkUrl, String cacheUrl) {
        mNewsStreamPlacer.loadNews(networkUrl, cacheUrl);
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
        if(viewType == ITEM_TYPE.ITEM_FIRST_ROW.ordinal()) {
            return new MarketSenseViewHolder(mNewsFirstRowRenderer.createView(mActivity, parent));
        } else if(viewType == ITEM_TYPE.ITEM_SECOND_ROW.ordinal()) {
            return new MarketSenseViewHolder(mNewsSecondRowRenderer.createView(mActivity, parent));
        } else {
            return new MarketSenseViewHolder(mNewsRenderer.createView(mActivity, parent));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        News news = null;
        final int type = getItemViewType(position);

        if(mInitLayoutType == NEWS_SINGLE_LAYOUT) {
            news = mNewsStreamPlacer.getNewsData(position);
        } else {
            if(type == ITEM_TYPE.ITEM_FIRST_ROW.ordinal()) {
                news = mNewsStreamPlacer.getNewsData(position);
            } else if(type == ITEM_TYPE.ITEM_SECOND_ROW.ordinal()) {
                news = mNewsStreamPlacer.getNewsData(position);
                News nextNews = mNewsStreamPlacer.getNewsData(position + 1);
                news.setNextNews(nextNews);
            } else {
                news = mNewsStreamPlacer.getNewsData(position + 1);
            }
        }

        if(news != null) {
            if(type == ITEM_TYPE.ITEM_FIRST_ROW.ordinal()) {
                mNewsFirstRowRenderer.renderView(holder.itemView, news);
            } else if(type == ITEM_TYPE.ITEM_SECOND_ROW.ordinal()) {
                mNewsSecondRowRenderer.renderView(holder.itemView, news);
            } else {
                mNewsRenderer.renderView(holder.itemView, news);
            }

            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (mInitLayoutType == NEWS_SINGLE_LAYOUT) {
                            mOnItemClickListener.onItemClick(mNewsStreamPlacer.getNewsData(holder.getAdapterPosition()));
                        } else {
                            if (type == ITEM_TYPE.ITEM_FIRST_ROW.ordinal()) {
                                mOnItemClickListener.onItemClick(mNewsStreamPlacer.getNewsData(holder.getAdapterPosition()));
                            } else if (type == ITEM_TYPE.ITEM_SECOND_ROW.ordinal()) {
                                if (motionEvent.getX() < ClientData.getInstance().getScreenWidthPixels() - motionEvent.getX()) {
                                    // left part
                                    mOnItemClickListener.onItemClick(mNewsStreamPlacer.getNewsData(holder.getAdapterPosition()));
                                } else {
                                    // right part
                                    mOnItemClickListener.onItemClick(mNewsStreamPlacer.getNewsData(holder.getAdapterPosition() + 1));
                                }
                            } else {
                                mOnItemClickListener.onItemClick(mNewsStreamPlacer.getNewsData(holder.getAdapterPosition() + 1));
                            }
                        }
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mInitLayoutType < 0) {
            if(mNewsStreamPlacer.getItemCount() > 4) {
                mInitLayoutType = NEWS_MULTIPLE_LAYOUT;
            } else {
                mInitLayoutType = NEWS_SINGLE_LAYOUT;
            }
        }

        if(mInitLayoutType == NEWS_MULTIPLE_LAYOUT) {
            if (position == 0) {
                return ITEM_TYPE.ITEM_FIRST_ROW.ordinal();
            } else if (position == 1) {
                return ITEM_TYPE.ITEM_SECOND_ROW.ordinal();
            } else {
                return ITEM_TYPE.ITEM_ELSE.ordinal();
            }
        } else {
            return ITEM_TYPE.ITEM_ELSE.ordinal();
        }
    }

    @Override
    public int getItemCount() {
        if(mInitLayoutType == NEWS_MULTIPLE_LAYOUT) {
            // there must be bigger than 4
            return mNewsStreamPlacer.getItemCount() - 1;
        } else {
            return mNewsStreamPlacer.getItemCount();
        }
    }

    public int getNewsTotalCount() {
        return mNewsStreamPlacer.getNewsTotalCount();
    }

    public void clearNews() {
        mNewsStreamPlacer.clearNews();
        notifyDataSetChanged();
    }

    public void destroy() {
        mNewsRenderer.clear();
        mNewsFirstRowRenderer.clear();
        mNewsStreamPlacer.clear();
    }
}
