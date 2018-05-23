package com.idroi.marketsense.common;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.data.StockTickData;
import com.idroi.marketsense.data.StockTradeData;
import com.idroi.marketsense.datasource.Networking;
import com.idroi.marketsense.request.StockChartDataRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by daniel.hsieh on 2018/5/21.
 */

public class YahooStxChartCrawler {

    public interface YahooStxChartListener {
        void onStxChartDataLoad();
        void onStxChartDataFail(final MarketSenseError marketSenseError);
    }

    private LineChart mPriceLineChart;
    private BarChart mVolumeBarChart;
    private StockTradeData mStockTradeData;
    private StockChartDataRequest mStockChartDataRequest;

    private Context mContext;
    private String mCode;
    private String mName;
    private String mUrl;
    private YahooStxChartListener mYahooStxChartListener;

    private int mCurrentRetryTime = 1;
    private static final int MAX_RETRY_TIMES = 3;
    private Handler mHandler;
    private Runnable mRetryRunnable, mTimeoutRunnable;

    public YahooStxChartCrawler(Context context, String name, String code, LineChart lineChart, BarChart barChart) {
        mContext = context;
        mCode = code;
        mName = name;
        mPriceLineChart = lineChart;
        mVolumeBarChart = barChart;
        mHandler = new Handler();
        mRetryRunnable = new Runnable() {
            @Override
            public void run() {
                makeRequest(mUrl);
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
                    mHandler.post(mRetryRunnable);
                }
            }
        };
    }

    public void setYahooStxChartListener(YahooStxChartListener listener) {
        mYahooStxChartListener = listener;
    }

    public void loadStockChartData() {
        mUrl = StockChartDataRequest.getYahooStockPriceUrl(mCode);
        makeRequest(mUrl);
    }

    public void renderStockChartData() {
        if(mStockTradeData == null ||
                mStockTradeData.getStockTickData() == null ||
                mStockTradeData.getStockTickData().size() == 0) {
            MSLog.e("Stock trade data is not available.");
            mPriceLineChart.setNoDataText(mContext.getResources().getString(R.string.no_data));
            Typeface typeface = Typeface.create("sans-serif", Typeface.NORMAL);
            mPriceLineChart.setNoDataTextTypeface(typeface);
            Paint paint = mPriceLineChart.getPaint(Chart.PAINT_INFO);
            paint.setTextSize(100);
            mPriceLineChart.invalidate();
            mVolumeBarChart.setNoDataText("");
            mVolumeBarChart.invalidate();
            return;
        }

        setData();
        setDescription();
        setXAxis();
        setYAxis();

        mPriceLineChart.invalidate();
    }

    private void setData() {
        List<Entry> yPrices = new ArrayList<>();
        List<BarEntry> yVolume = new ArrayList<>();

        ArrayList<StockTickData> ticks = mStockTradeData.getStockTickData();
        for(int i = 0; i < ticks.size(); i++) {
            StockTickData stockTickData = ticks.get(i);
            Entry entry = new Entry(stockTickData.getMinute(), stockTickData.getPrice());
            yPrices.add(entry);
            BarEntry volume = new BarEntry(stockTickData.getMinute(), stockTickData.getVolume());
            yVolume.add(volume);
        }

        LineDataSet lineDataSet = new LineDataSet(yPrices, null);
        lineDataSet.setColor(mContext.getResources().getColor(R.color.color_price_line));
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setLineWidth(1.6f);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        LineData lineData = new LineData(lineDataSet);

        mPriceLineChart.setData(lineData);
        mPriceLineChart.setBackgroundColor(mContext.getResources().getColor(R.color.marketsense_text_white));
        mPriceLineChart.getLegend().setEnabled(false);

        BarDataSet barDataSet = new BarDataSet(yVolume, null);
        barDataSet.setColor(mContext.getResources().getColor(R.color.color_volume_line));
        barDataSet.setDrawValues(false);
        barDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        BarData barData = new BarData(barDataSet);

        mVolumeBarChart.setData(barData);
        mVolumeBarChart.setBackgroundColor(mContext.getResources().getColor(R.color.marketsense_text_white));
        mVolumeBarChart.getLegend().setEnabled(false);

    }

    private void setDescription() {
        Description description = mPriceLineChart.getDescription();
        description.setText(String.format(Locale.US,
                mContext.getResources().getString(R.string.title_company_name_code_format),
                mName, mCode));
        description.setTextSize(16);
        description.setTextColor(mContext.getResources().getColor(R.color.marketsense_text_black));
        description.setEnabled(false);

        Description description1 = mVolumeBarChart.getDescription();
        description1.setEnabled(false);
    }

    private void setXAxis() {
        XAxis xAxis = mPriceLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(mContext.getResources().getColor(R.color.marketsense_text_black));
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.enableGridDashedLine(10f, 10f, 10f);
        xAxis.setValueFormatter(new StockMinuteFormatter());
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(270);
        xAxis.setLabelCount(10, true);
        xAxis.setDrawLabels(false);

        XAxis xAxisVolume = mVolumeBarChart.getXAxis();
        xAxisVolume.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisVolume.setTextColor(mContext.getResources().getColor(R.color.marketsense_text_black));
        xAxisVolume.setTextSize(10f);
        xAxisVolume.setDrawAxisLine(false);
        xAxisVolume.setDrawGridLines(true);
        xAxisVolume.enableGridDashedLine(10f, 10f, 10f);
        xAxisVolume.setValueFormatter(new StockMinuteFormatter());
        xAxisVolume.setAxisMinimum(0);
        xAxisVolume.setAxisMaximum(270);
        xAxisVolume.setLabelCount(10, true);
    }

    private void setYAxis() {
        YAxis yRightAxis = mPriceLineChart.getAxisRight();
        yRightAxis.setTextColor(mContext.getResources().getColor(R.color.marketsense_text_black));
        yRightAxis.setTextSize(10f);
        yRightAxis.setDrawAxisLine(false);
        yRightAxis.setDrawGridLines(true);
        yRightAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yRightAxis.enableGridDashedLine(10f, 10f, 10f);
        yRightAxis.setValueFormatter(new StockPriceFormatter());
        yRightAxis.setAxisMaximum(mStockTradeData.getMaxPrice());
        yRightAxis.setAxisMinimum(mStockTradeData.getMinPrice());
        yRightAxis.setLabelCount(11, true);
        yRightAxis.setYOffset(-7);

        LimitLine limitLine = new LimitLine(mStockTradeData.getYesterdayPrice());
        limitLine.setLineColor(mContext.getResources().getColor(R.color.marketsense_text_gray));
        limitLine.setLineWidth(0.8f);
        yRightAxis.addLimitLine(limitLine);
        yRightAxis.setDrawLimitLinesBehindData(true);

        YAxis yRightAxisVolume = mVolumeBarChart.getAxisRight();
        yRightAxisVolume.setTextColor(mContext.getResources().getColor(R.color.colorTrendUp));
        yRightAxisVolume.setTextSize(10f);
        yRightAxisVolume.setDrawAxisLine(false);
        yRightAxisVolume.setDrawGridLines(true);
        yRightAxisVolume.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yRightAxisVolume.enableGridDashedLine(10f, 10f, 10f);
        yRightAxisVolume.setValueFormatter(new StockVolumeFormatter());
        yRightAxisVolume.setAxisMinimum(0);
        yRightAxisVolume.setAxisMaximum(mStockTradeData.getMaxVolume());
        yRightAxisVolume.setLabelCount(5, true);
        yRightAxisVolume.setYOffset(-8);

        YAxis yLeftAxis = mPriceLineChart.getAxisLeft();
        yLeftAxis.setEnabled(false);
        YAxis yLeftAxisVolume = mVolumeBarChart.getAxisLeft();
        yLeftAxisVolume.setEnabled(false);
    }

    private boolean isRetry() {
        if(mCurrentRetryTime < MAX_RETRY_TIMES) {
            mCurrentRetryTime++;
            return true;
        }
        return false;
    }

    private void makeRequest(String url) {
        mStockChartDataRequest = new StockChartDataRequest(url,
                new Response.Listener<StockTradeData>() {
                    @Override
                    public void onResponse(StockTradeData response) {
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
                        MSLog.e("StockChartData Request error: " + error.getMessage(), error);
                        if(error.networkResponse != null) {
                            MSLog.e("StockChartData Request error: " + new String(error.networkResponse.data), error);
                        }
                        mHandler.removeCallbacks(mTimeoutRunnable);
                        if(isRetry()) {
                            mHandler.post(mRetryRunnable);
                        }

                        if(mYahooStxChartListener != null) {
                            if (error instanceof MarketSenseNetworkError) {
                                MarketSenseNetworkError networkError = (MarketSenseNetworkError) error;
                                mYahooStxChartListener.onStxChartDataFail(networkError.getReason());
                            } else {
                                mYahooStxChartListener.onStxChartDataFail(MarketSenseError.NETWORK_VOLLEY_ERROR);
                            }
                        }
                    }
                });
        mHandler.postDelayed(mTimeoutRunnable, 3000);
        Networking.getRequestQueue(mContext).add(mStockChartDataRequest);
    }
}
