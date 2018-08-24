package com.idroi.marketsense.common;

import android.content.Context;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.data.StockTradeData;
import com.idroi.marketsense.datasource.Networking;
import com.idroi.marketsense.request.StockChartDataRequest;
import com.idroi.marketsense.viewholders.ChartTaTopItemsViewHolder;
import com.idroi.marketsense.viewholders.ChartTickBottomItemsViewHolder;

import java.lang.ref.WeakReference;
import java.util.Locale;


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

//    private TextView mPriceTextView, mDiffTextView;
//    private TextView mOpenTextView, mHighTextView, mLowTextView, mYesterdayCloseTextView;
//    private TextView mDateTextView, mVolumeTextView;

    private ChartTaTopItemsViewHolder mChartTaTopItemsViewHolder;
    private ChartTickBottomItemsViewHolder mChartTickBottomItemsViewHolder;

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

    public void setInformationTextView(ChartTaTopItemsViewHolder chartTaTopItemsViewHolder,
                                       ChartTickBottomItemsViewHolder chartTickBottomItemsViewHolder) {
        mChartTaTopItemsViewHolder = chartTaTopItemsViewHolder;
        mChartTickBottomItemsViewHolder = chartTickBottomItemsViewHolder;
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
                mCandleStickChart.setVisibility(View.GONE);

                YahooStxChartTickRenderer tickRenderer =
                        new YahooStxChartTickRenderer(mName, mCode, mPriceLineChart, mVolumeBarChart);
                tickRenderer.render(context, mStockTradeData);
                MarketSenseRendererHelper.addTextView(mChartTaTopItemsViewHolder.tradeDateTextView, mStockTradeData.getTickTradeDay());
                MarketSenseRendererHelper.addTextView(mChartTaTopItemsViewHolder.tradeVolumeTextView, mStockTradeData.getTickTotalVolume());
                MarketSenseRendererHelper.addNumberStringToTextView(mChartTickBottomItemsViewHolder.openTextView,
                        String.valueOf(mStockTradeData.getOpenPrice()), "--", mStockTradeData.getOpenPrice(), mStockTradeData.getYesterdayPrice());
                MarketSenseRendererHelper.addNumberStringToTextView(mChartTickBottomItemsViewHolder.highTextView,
                        String.valueOf(mStockTradeData.getHighPrice()), "--", mStockTradeData.getHighPrice(), mStockTradeData.getYesterdayPrice());
                MarketSenseRendererHelper.addNumberStringToTextView(mChartTickBottomItemsViewHolder.lowTextView,
                        String.valueOf(mStockTradeData.getLowPrice()), "--", mStockTradeData.getLowPrice(), mStockTradeData.getYesterdayPrice());
                MarketSenseRendererHelper.addNumberStringToTextView(mChartTickBottomItemsViewHolder.yesterdayCloseTextView,
                        String.valueOf(mStockTradeData.getYesterdayPrice()), "--", mStockTradeData.getYesterdayPrice(), mStockTradeData.getYesterdayPrice());

                MarketSenseRendererHelper.addNumberStringToTextView(mChartTickBottomItemsViewHolder.buyTextView,
                        String.valueOf(mStockTradeData.getTickBuyPrice()), "--", mStockTradeData.getTickBuyPrice(), mStockTradeData.getYesterdayPrice());
                MarketSenseRendererHelper.addNumberStringToTextView(mChartTickBottomItemsViewHolder.sellTextView,
                        String.valueOf(mStockTradeData.getTickSellPrice()), "--", mStockTradeData.getTickSellPrice(), mStockTradeData.getYesterdayPrice());
                MarketSenseRendererHelper.addNumberStringToTextView(mChartTickBottomItemsViewHolder.moneyTextView,
                        String.format(Locale.US, "%.2f", mStockTradeData.getTradeMoney()), "--", R.color.stock_blue);
                MarketSenseRendererHelper.addNumberStringToTextView(mChartTickBottomItemsViewHolder.yesterdayVolumeTextView,
                        String.valueOf(mStockTradeData.getYesterdayVolume()), "--", R.color.stock_blue);

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mVolumeBarChart.getLayoutParams();
                params.topToBottom = mPriceLineChart.getId();
                mVolumeBarChart.setLayoutParams(params);

            } else if (mStockTradeData.getType().equals(StockTradeData.STOCK_TRADE_DATA_TYPE_TA)) {

                mPriceLineChart.setVisibility(View.GONE);
                mCandleStickChart.setVisibility(View.VISIBLE);

                YahooStxChartTaRenderer taRenderer =
                        new YahooStxChartTaRenderer(mName, mCode, mCandleStickChart, mVolumeBarChart);
                taRenderer.setInformationTextView(mChartTaTopItemsViewHolder.closePriceTextView,
                        mChartTaTopItemsViewHolder.diffTextView,
                        mChartTickBottomItemsViewHolder.openTextView,
                        mChartTickBottomItemsViewHolder.highTextView,
                        mChartTickBottomItemsViewHolder.lowTextView,
                        mChartTickBottomItemsViewHolder.yesterdayCloseTextView,
                        mChartTaTopItemsViewHolder.tradeDateTextView,
                        mChartTaTopItemsViewHolder.tradeVolumeTextView);
                taRenderer.render(context, mStockTradeData);

                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mVolumeBarChart.getLayoutParams();
                params.topToBottom = mCandleStickChart.getId();
                mVolumeBarChart.setLayoutParams(params);

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
