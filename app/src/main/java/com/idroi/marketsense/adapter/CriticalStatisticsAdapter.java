package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.data.StatisticDataItem;
import com.idroi.marketsense.datasource.YahooCriticalStatisticsFetcher;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/7/23.
 */

public class CriticalStatisticsAdapter extends RecyclerView.Adapter {

    public interface CriticalStatisticsAvailableListener {
        void onCriticalStatisticsAvailable();
    }

    private Activity mActivity;

    private CriticalStatisticsNormalRenderer mCriticalStatisticsNormalRenderer;
    private CriticalStatisticsTitleRenderer mCriticalStatisticsTitleRenderer;
    private CriticalStatistics5ColumnsRenderer mCriticalStatistics5ColumnsRenderer;

    private YahooCriticalStatisticsFetcher mYahooCriticalStatisticsFetcher;
    private ArrayList<StatisticDataItem> mStatisticDataItems;
    private CriticalStatisticsAvailableListener mCriticalStatisticsAvailableListener;

    public CriticalStatisticsAdapter(final Activity activity) {
        mActivity = activity;

        mCriticalStatisticsNormalRenderer = new CriticalStatisticsNormalRenderer();
        mCriticalStatisticsTitleRenderer = new CriticalStatisticsTitleRenderer();
        mCriticalStatistics5ColumnsRenderer = new CriticalStatistics5ColumnsRenderer();

        mYahooCriticalStatisticsFetcher = new YahooCriticalStatisticsFetcher(mActivity);
        mYahooCriticalStatisticsFetcher.setYahooCriticalStatisticsNetworkListener(new YahooCriticalStatisticsFetcher.YahooCriticalStatisticsNetworkListener() {
            @Override
            public void onCriticalStatisticsLoaded(ArrayList<StatisticDataItem> statisticDataItems) {
                mStatisticDataItems = statisticDataItems;
                if(mCriticalStatisticsAvailableListener != null) {
                    mCriticalStatisticsAvailableListener.onCriticalStatisticsAvailable();
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCriticalStatisticsFailed(MarketSenseError marketSenseError) {

            }
        });
    }

    public void setCriticalStatisticsAvailableListener(CriticalStatisticsAvailableListener listener) {
        mCriticalStatisticsAvailableListener = listener;
    }

    public void loadCriticalStatistics(String url) {
        mYahooCriticalStatisticsFetcher.makeRequest(url);
    }

    @Override
    public int getItemViewType(int position) {
        if(mStatisticDataItems != null) {
            StatisticDataItem item = mStatisticDataItems.get(position);
            return item.getType();
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == StatisticDataItem.TYPE_TITLE) {
            return new MarketSenseViewHolder(mCriticalStatisticsTitleRenderer.createView(mActivity, parent));
        } else if(viewType == StatisticDataItem.TYPE_NORMAL) {
            return new MarketSenseViewHolder(mCriticalStatisticsNormalRenderer.createView(mActivity, parent));
        } else if(viewType == StatisticDataItem.TYPE_5_COLUMNS) {
            return new MarketSenseViewHolder(mCriticalStatistics5ColumnsRenderer.createView(mActivity, parent));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(mStatisticDataItems != null) {
            StatisticDataItem data = mStatisticDataItems.get(position);
            if (data != null) {
                int viewType = data.getType();
                if(viewType == StatisticDataItem.TYPE_TITLE) {
                    mCriticalStatisticsTitleRenderer.renderView(holder.itemView, data);
                } else if(viewType == StatisticDataItem.TYPE_NORMAL) {
                    mCriticalStatisticsNormalRenderer.renderView(holder.itemView, data);
                } else if(viewType == StatisticDataItem.TYPE_5_COLUMNS) {
                    mCriticalStatistics5ColumnsRenderer.renderView(holder.itemView, data);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if(mStatisticDataItems != null) {
            return mStatisticDataItems.size();
        } else {
            return 0;
        }
    }

    public void destroy() {
        mCriticalStatisticsNormalRenderer.clear();
        mStatisticDataItems.clear();
        mYahooCriticalStatisticsFetcher.destroy();
        mCriticalStatisticsAvailableListener = null;
    }
}
