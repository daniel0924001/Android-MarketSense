package com.idroi.marketsense.datasource;

import android.content.Context;
import android.os.Handler;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.StatisticDataItem;
import com.idroi.marketsense.request.CriticalStatisticsRequest;
import com.idroi.marketsense.util.MarketSenseUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/7/24.
 */

public class YahooCriticalStatisticsFetcher {

    public interface YahooCriticalStatisticsNetworkListener {
        void onCriticalStatisticsLoaded(ArrayList<StatisticDataItem> statisticDataItems);
        void onCriticalStatisticsFailed(final MarketSenseError marketSenseError);
    }

    private static final YahooCriticalStatisticsNetworkListener EMPTY_NETWORK_LISTENER = new YahooCriticalStatisticsNetworkListener() {
        @Override
        public void onCriticalStatisticsLoaded(ArrayList<StatisticDataItem> statisticDataItems) {

        }

        @Override
        public void onCriticalStatisticsFailed(final MarketSenseError marketSenseError) {

        }
    };

    private Handler mTimeoutHandler;
    private Runnable mTimeoutRunnable;

    private WeakReference<Context> mContext;
    private CriticalStatisticsRequest mCriticalStatisticsRequest;
    private YahooCriticalStatisticsNetworkListener mYahooCriticalStatisticsNetworkListener;

    public YahooCriticalStatisticsFetcher(Context context) {
        mContext = new WeakReference<Context>(context);
        mTimeoutHandler = new Handler();
        mTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if(mCriticalStatisticsRequest != null) {
                    mCriticalStatisticsRequest.cancel();
                    mCriticalStatisticsRequest = null;
                }
                if(mYahooCriticalStatisticsNetworkListener != null) {
                    mYahooCriticalStatisticsNetworkListener.onCriticalStatisticsFailed(MarketSenseError.NETWORK_CONNECTION_TIMEOUT);
                }
            }
        };
    }

    public void setYahooCriticalStatisticsNetworkListener(
            YahooCriticalStatisticsNetworkListener listener) {
        mYahooCriticalStatisticsNetworkListener = listener;
    }

    public void makeRequest(String url) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(!MarketSenseUtils.isNetworkAvailable(context)) {
            mYahooCriticalStatisticsNetworkListener.onCriticalStatisticsFailed(MarketSenseError.NETWORK_CONNECTION_TIMEOUT);
            return;
        }

        requestCriticalStatics(url);
    }

    private void requestCriticalStatics(String url) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        mCriticalStatisticsRequest = new CriticalStatisticsRequest(url,
                CriticalStatisticDataSource.getStockStatisticDataList(),
                new Response.Listener<ArrayList<StatisticDataItem>>() {
                    @Override
                    public void onResponse(ArrayList<StatisticDataItem> response) {
                        final Context context = getContextOrDestroy();
                        if(context == null) {
                            return;
                        }

                        mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                        mYahooCriticalStatisticsNetworkListener.onCriticalStatisticsLoaded(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        final Context context = getContextOrDestroy();
                        if(context == null) {
                            return;
                        }

                        MSLog.e("Critical Statistics Request error: " + error.getMessage(), error);
                        if(error.networkResponse != null) {
                            MSLog.e("Critical Statistics Request error: " + new String(error.networkResponse.data), error);
                        }
                        mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                        if(error instanceof MarketSenseNetworkError) {
                            MarketSenseNetworkError networkError = (MarketSenseNetworkError) error;
                            mYahooCriticalStatisticsNetworkListener.onCriticalStatisticsFailed(networkError.getReason());
                        } else {
                            mYahooCriticalStatisticsNetworkListener.onCriticalStatisticsFailed(MarketSenseError.NETWORK_VOLLEY_ERROR);
                        }
                    }
                });

        mTimeoutHandler.postDelayed(mTimeoutRunnable, 3000);
        Networking.getRequestQueue(context).add(mCriticalStatisticsRequest);
    }

    public void destroy() {
        mContext.clear();
        if(mCriticalStatisticsRequest != null) {
            mCriticalStatisticsRequest.cancel();
            mCriticalStatisticsRequest = null;
        }
        mYahooCriticalStatisticsNetworkListener = EMPTY_NETWORK_LISTENER;
        if(mTimeoutHandler != null) {
            mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
        }
    }

    private Context getContextOrDestroy() {
        final Context context = mContext.get();
        if(context == null) {
            destroy();
            MSLog.d("Weak reference to Context in YahooCriticalStatisticsFetcher became null. " +
                    "This instance of MarketSenseCommentsFetcher is destroyed and " +
                    "no more requests will be processed.");
        }
        return context;
    }
}
