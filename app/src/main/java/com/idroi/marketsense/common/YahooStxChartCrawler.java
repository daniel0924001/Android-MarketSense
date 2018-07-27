package com.idroi.marketsense.common;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.StockTaData;
import com.idroi.marketsense.data.StockTradeData;
import com.idroi.marketsense.datasource.Networking;
import com.idroi.marketsense.request.StockChartDataRequest;

import java.lang.ref.WeakReference;


/**
 * Created by daniel.hsieh on 2018/5/21.
 */

public class YahooStxChartCrawler {

    public interface YahooStxChartListener {
        void onStxChartDataLoad();
        void onStxChartDataFail(final MarketSenseError marketSenseError);
    }

    private static final YahooStxChartListener EMPTY_NETWORK_LISTENER = new YahooStxChartListener () {
        @Override
        public void onStxChartDataLoad() {

        }

        @Override
        public void onStxChartDataFail(final MarketSenseError marketSenseError) {

        }
    };

    private LineChart mPriceLineChart;
    private BarChart mVolumeBarChart;
    private CandleStickChart mCandleStickChart;
    private StockTradeData mStockTradeData;
    private StockChartDataRequest mStockChartDataRequest;

    private WeakReference<Context> mContext;
    private String mCode;
    private String mName;
    private String mUrl;
    private YahooStxChartListener mYahooStxChartListener;

    private int mCurrentRetryTime = 0;
    private static final int MAX_RETRY_TIMES = 3;
    private Handler mHandler;
    private Runnable mRetryRunnable, mTimeoutRunnable;

    private TextView mPriceTextView, mDiffTextView;
    private TextView mOpenTextView, mHighTextView, mLowTextView, mYesterdayCloseTextView;
    private TextView mDateTextView, mVolumeTextView;

    public YahooStxChartCrawler(Context context, String name, String code,
                                LineChart lineChart,
                                BarChart barChart,
                                CandleStickChart candleStickChart) {
        mContext = new WeakReference<Context>(context);
        mCode = code;
        mName = name;
        mPriceLineChart = lineChart;
        mVolumeBarChart = barChart;
        mCandleStickChart = candleStickChart;
        mHandler = new Handler();
        mRetryRunnable = new Runnable() {
            @Override
            public void run() {
                makeRequest(mUrl, false);
            }
        };
        mTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if(mStockChartDataRequest != null) {
                    mStockChartDataRequest.cancel();
                    mStockChartDataRequest = null;
                }
                if(isRetry()) {
                    makeRequest(mUrl, false);
                } else {
                    resetRetryTime();
                    mYahooStxChartListener.onStxChartDataFail(MarketSenseError.NETWORK_CONNECTION_TIMEOUT);
                }
            }
        };
    }

    public void setInformationTextView(TextView price, TextView diff, TextView open, TextView high, TextView low, TextView yesterdayClose, TextView date, TextView volume) {
        mPriceTextView = price;
        mDiffTextView = diff;
        mOpenTextView = open;
        mHighTextView = high;
        mLowTextView = low;
        mYesterdayCloseTextView = yesterdayClose;
        mDateTextView = date;
        mVolumeTextView = volume;
    }

    public void setYahooStxChartListener(YahooStxChartListener listener) {
        mYahooStxChartListener = listener;
    }

    public void loadStockChartData() {
        mUrl = StockChartDataRequest.getYahooTickStockPriceUrl(mCode);
        makeRequest(mUrl);
    }

    public void loadTaStockChartData(String type) {
        mUrl = StockChartDataRequest.getYahooTaStockPriceUrl(type, mCode);
        makeRequest(mUrl);
    }

    public void renderStockChartData() {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(mStockTradeData != null) {
            if (mStockTradeData.getType().equals(StockTradeData.STOCK_TRADE_DATA_TYPE_TICK)) {
                mPriceLineChart.setVisibility(View.VISIBLE);
                mCandleStickChart.setVisibility(View.INVISIBLE);
                YahooStxChartTickRenderer tickRenderer =
                        new YahooStxChartTickRenderer(mName, mCode, mPriceLineChart, mVolumeBarChart);
                tickRenderer.render(context, mStockTradeData);
                MarketSenseRendererHelper.addTextView(mDateTextView, mStockTradeData.getTickTradeDay());
                MarketSenseRendererHelper.addTextView(mVolumeTextView, mStockTradeData.getTickTotalVolume());
                MarketSenseRendererHelper.addTextView(mOpenTextView, String.valueOf(mStockTradeData.getOpenPrice()));
                MarketSenseRendererHelper.addTextView(mHighTextView, String.valueOf(mStockTradeData.getHighPrice()));
                MarketSenseRendererHelper.addTextView(mLowTextView, String.valueOf(mStockTradeData.getLowPrice()));
                MarketSenseRendererHelper.addTextView(mYesterdayCloseTextView, String.valueOf(mStockTradeData.getYesterdayPrice()));
            } else if (mStockTradeData.getType().equals(StockTradeData.STOCK_TRADE_DATA_TYPE_TA)) {
                mPriceLineChart.setVisibility(View.INVISIBLE);
                mCandleStickChart.setVisibility(View.VISIBLE);
                YahooStxChartTaRenderer taRenderer =
                        new YahooStxChartTaRenderer(mName, mCode, mCandleStickChart, mVolumeBarChart);
                taRenderer.setInformationTextView(mPriceTextView, mDiffTextView, mOpenTextView, mHighTextView, mLowTextView, mYesterdayCloseTextView, mDateTextView, mVolumeTextView);
                StockTaData stockTaData = mStockTradeData.getLastStockTaData(0);
                StockTaData stockTaDataYesterday = mStockTradeData.getLastStockTaData(1);
                MarketSenseRendererHelper.addTextView(mDateTextView, StockTradeData.getTaTradeDate(stockTaData.getTime().toString()));
                MarketSenseRendererHelper.addTextView(mVolumeTextView, StockVolumeFormatter.getFormattedValue(stockTaData.getVolume()));
                MarketSenseRendererHelper.addTextView(mOpenTextView, String.valueOf(stockTaData.getOpen()));
                MarketSenseRendererHelper.addTextView(mHighTextView, String.valueOf(stockTaData.getShadowHigh()));
                MarketSenseRendererHelper.addTextView(mLowTextView, String.valueOf(stockTaData.getShadowLow()));
                MarketSenseRendererHelper.addTextView(mYesterdayCloseTextView, String.valueOf(stockTaDataYesterday.getClose()));
                taRenderer.render(context, mStockTradeData);
            }
        }
    }

    public StockTradeData getStockTradeData() {
        return mStockTradeData;
    }

    private void resetRetryTime() {
        mCurrentRetryTime = 0;
    }

    private boolean isRetry() {
        if(mCurrentRetryTime < MAX_RETRY_TIMES) {
            mCurrentRetryTime++;
            return true;
        }
        return false;
    }

    private void makeRequest(String url) {
        makeRequest(url, true);
    }

    private void makeRequest(String url, boolean isClear) {

        if(isClear) {
            clear();
        }

        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        mStockChartDataRequest = new StockChartDataRequest(url,
                new Response.Listener<StockTradeData>() {
                    @Override
                    public void onResponse(StockTradeData response) {
                        final Context context = getContextOrDestroy();
                        if(context == null) {
                            return;
                        }
                        resetRetryTime();

                        mHandler.removeCallbacks(mTimeoutRunnable);
                        mStockTradeData = response;
                        if(mYahooStxChartListener != null) {
                            mYahooStxChartListener.onStxChartDataLoad();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        final Context context = getContextOrDestroy();
                        if(context == null) {
                            return;
                        }

                        MSLog.e("StockChartData Request error: " + error.getMessage(), error);
                        if(error.networkResponse != null) {
                            MSLog.e("StockChartData Request error: " + new String(error.networkResponse.data), error);
                        }
                        mHandler.removeCallbacks(mTimeoutRunnable);
                        if(isRetry()) {
                            mHandler.post(mRetryRunnable);
                        } else {
                            resetRetryTime();
                            if (mYahooStxChartListener != null) {
                                if (error instanceof MarketSenseNetworkError) {
                                    MarketSenseNetworkError networkError = (MarketSenseNetworkError) error;
                                    mYahooStxChartListener.onStxChartDataFail(networkError.getReason());
                                } else {
                                    mYahooStxChartListener.onStxChartDataFail(MarketSenseError.NETWORK_VOLLEY_ERROR);
                                }
                            }
                        }
                    }
                });
        mHandler.postDelayed(mTimeoutRunnable, 5000);
        Networking.getRequestQueue(context).add(mStockChartDataRequest);
    }

    private void clear() {
        resetRetryTime();
        if(mStockChartDataRequest != null) {
            mStockChartDataRequest.cancel();
            mStockChartDataRequest = null;
        }
        if(mHandler != null) {
            mHandler.removeCallbacks(mRetryRunnable);
            mHandler.removeCallbacks(mTimeoutRunnable);
        }
    }

    public void destroy() {
        clear();
        mContext.clear();
        mYahooStxChartListener = EMPTY_NETWORK_LISTENER;
    }

    private Context getContextOrDestroy() {
        final Context context = mContext.get();
        if (context == null) {
            destroy();
            MSLog.d("Weak reference to Context in YahooStxChartCrawler became null. " +
                    "This instance of YahooStxChartCrawler is destroyed and " +
                    "no more requests will be processed.");
        }
        return context;
    }
}
