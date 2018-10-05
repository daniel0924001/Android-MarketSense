package com.idroi.marketsense.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.data.StockBaseData;
import com.idroi.marketsense.data.StockTaData;
import com.idroi.marketsense.data.StockTradeData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by daniel.hsieh on 2018/7/18.
 */

public class YahooStxChartTaRenderer {

    private String mCode;
    private String mName;

    private CandleStickChart mCandleStickChart;
    private BarChart mVolumeBarChart;

    private StockTradeData mStockTradeData;

    private TextView mPriceTextView, mDiffTextView;
    private TextView mOpenTextView, mHighTextView, mLowTextView, mYesterdayCloseTextView;
    private TextView mDateTextView, mVolumeTextView;

    public YahooStxChartTaRenderer(String name, String code, CandleStickChart candleStickChart, BarChart barChart) {
        mName = name;
        mCode = code;
        mCandleStickChart = candleStickChart;
        mVolumeBarChart = barChart;
    }

    public void render(Context context, StockTradeData stockTradeData) {
        mStockTradeData = stockTradeData;

        if(mStockTradeData == null ||
                mStockTradeData.getStockData() == null ||
                mStockTradeData.getStockData().size() == 0) {
            MSLog.e("Stock trade data is not available.");
            renderStockChartDataFailed(context);
            return;
        }

        clear();

        try {
            setData(context);
            setDescription(context);
            setXAxis(context);
            setYAxis(context);
            setTouchMarker(context);
            refreshTextView(context, stockTradeData.getLastStockTaData(0));

            mCandleStickChart.invalidate();
            mVolumeBarChart.invalidate();
        } catch (Exception e) {
            MSLog.e("renderStockChartData exception: " + e.toString());
            renderStockChartDataFailed(context);
        }
    }

    private void setData(Context context) {
        ArrayList<CandleEntry> entries = new ArrayList<>();
        List<BarEntry> yVolume = new ArrayList<>();

        ArrayList<StockBaseData> ta = mStockTradeData.getStockData();
        ArrayList<Integer> colorsArray = new ArrayList<>();
        Resources resources = context.getResources();
        for(int x = 0; x < ta.size(); x++) {
            StockTaData stockTaData = (StockTaData) ta.get(x);
            float openPrice = stockTaData.getOpen();
            float closePrice = stockTaData.getClose();
            CandleEntry candleEntry = new CandleEntry(
                x,
                stockTaData.getShadowHigh(),
                stockTaData.getShadowLow(),
                openPrice,
                closePrice);
            entries.add(candleEntry);
            candleEntry.setData(stockTaData);

            BarEntry volume = new BarEntry(x, stockTaData.getVolume());
            yVolume.add(volume);
            volume.setData(stockTaData);

            if(closePrice > openPrice) {
                colorsArray.add(resources.getColor(R.color.trend_red));
            } else if(closePrice < openPrice) {
                colorsArray.add(resources.getColor(R.color.trend_green));
            } else {
                colorsArray.add(resources.getColor(R.color.draw_grey));
            }
        }
        CandleDataSet candleDataSet = new CandleDataSet(entries, null);
        candleDataSet.setDrawValues(false);
        candleDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        candleDataSet.setColor(Color.rgb(80, 80, 80));
        candleDataSet.setShadowColor(Color.DKGRAY);
        candleDataSet.setShadowWidth(0.7f);
        candleDataSet.setDecreasingColor(resources.getColor(R.color.trend_green));
        candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);
        candleDataSet.setIncreasingColor(resources.getColor(R.color.trend_red));
        candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);
        candleDataSet.setNeutralColor(resources.getColor(R.color.draw_grey));
        CandleData data = new CandleData(candleDataSet);

        mCandleStickChart.setData(data);
        mCandleStickChart.setBackgroundColor(context.getResources().getColor(R.color.marketsense_text_white));
        mCandleStickChart.getLegend().setEnabled(false);

        BarDataSet barDataSet = new BarDataSet(yVolume, null);
        barDataSet.setColors(colorsArray);
        barDataSet.setDrawValues(false);
        barDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        BarData barData = new BarData(barDataSet);

        mVolumeBarChart.setData(barData);
        mVolumeBarChart.setBackgroundColor(context.getResources().getColor(R.color.marketsense_text_white));
        mVolumeBarChart.getLegend().setEnabled(false);
    }

    private void setDescription(Context context) {
        Description description = mCandleStickChart.getDescription();
        description.setText(String.format(Locale.US,
                context.getResources().getString(R.string.title_company_name_code_format),
                mName, mCode));
        description.setTextSize(16);
        description.setTextColor(context.getResources().getColor(R.color.text_first));
        description.setEnabled(false);

        Description description1 = mVolumeBarChart.getDescription();
        description1.setEnabled(false);
    }

    private void setXAxis(Context context) {

        int count = 4;

        XAxis xAxis = mCandleStickChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(context.getResources().getColor(R.color.text_second));
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.enableGridDashedLine(10f, 10f, 10f);
        xAxis.setValueFormatter(new StockMinuteFormatter());
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(mStockTradeData.size());
        xAxis.setLabelCount(count, true);
        xAxis.setDrawLabels(false);

        XAxis xAxisVolume = mVolumeBarChart.getXAxis();
        xAxisVolume.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisVolume.setTextColor(context.getResources().getColor(R.color.text_second));
        xAxisVolume.setTextSize(10f);
        xAxisVolume.setDrawAxisLine(false);
        xAxisVolume.setDrawGridLines(true);
        xAxisVolume.enableGridDashedLine(10f, 10f, 10f);
        xAxisVolume.setValueFormatter(new StockDateFormatter(mStockTradeData, count));
        xAxisVolume.setAxisMinimum(0);
        xAxisVolume.setAxisMaximum(mStockTradeData.size());
        xAxisVolume.setLabelCount(count, true);
    }

    private void setYAxis(Context context) {

        YAxis yRightAxis = mCandleStickChart.getAxisRight();
        yRightAxis.setTextColor(context.getResources().getColor(R.color.text_second));
        yRightAxis.setTextSize(10f);
        yRightAxis.setDrawAxisLine(false);
        yRightAxis.setDrawGridLines(true);
        yRightAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yRightAxis.enableGridDashedLine(10f, 10f, 10f);
        yRightAxis.setValueFormatter(new StockPriceFormatter(true));
        yRightAxis.setAxisMaximum(mStockTradeData.getTaHighPrice());
        yRightAxis.setAxisMinimum(mStockTradeData.getTaLowPrice());
        yRightAxis.setLabelCount(11, true);
        yRightAxis.setYOffset(-7);

        YAxis yRightAxisVolume = mVolumeBarChart.getAxisRight();
        yRightAxisVolume.setTextColor(context.getResources().getColor(R.color.trend_red));
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

        YAxis yLeftAxis = mCandleStickChart.getAxisLeft();
        yLeftAxis.setEnabled(false);
        YAxis yLeftAxisVolume = mVolumeBarChart.getAxisLeft();
        yLeftAxisVolume.setEnabled(false);
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

    private void setTouchMarker(final Context context) {
        mCandleStickChart.setTouchEnabled(true);
        mCandleStickChart.setScaleEnabled(false);
        mCandleStickChart.setDoubleTapToZoomEnabled(false);
        mVolumeBarChart.setTouchEnabled(true);
        mVolumeBarChart.setScaleEnabled(false);
        mVolumeBarChart.setDoubleTapToZoomEnabled(false);
        mCandleStickChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                mVolumeBarChart.highlightValue(h);
                refreshTextView(context, (StockTaData) e.getData());
            }

            @Override
            public void onNothingSelected() {
                mCandleStickChart.highlightValue(null);
            }
        });
        mVolumeBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                mCandleStickChart.highlightValue(h);
                refreshTextView(context, (StockTaData) e.getData());
            }

            @Override
            public void onNothingSelected() {
                mVolumeBarChart.highlightValue(null);
            }
        });
    }

    public void refreshTextView(Context context, StockTaData stockTaData) {
        ArrayList<StockBaseData> ta = mStockTradeData.getStockData();

        float todayClose = stockTaData.getClose();
        MarketSenseRendererHelper.addTextView(mPriceTextView, String.valueOf(todayClose));
        MarketSenseRendererHelper.addTextView(mDateTextView, StockTradeData.getTaTradeDate(stockTaData.getTime().toString()));
        MarketSenseRendererHelper.addTextView(mVolumeTextView, StockVolumeFormatter.getFormattedValue(stockTaData.getVolume()));
        MarketSenseRendererHelper.addTextViewWithColor(mOpenTextView, String.valueOf(stockTaData.getOpen()), R.color.text_first);
        MarketSenseRendererHelper.addTextViewWithColor(mHighTextView, String.valueOf(stockTaData.getShadowHigh()), R.color.text_first);
        MarketSenseRendererHelper.addTextViewWithColor(mLowTextView, String.valueOf(stockTaData.getShadowLow()), R.color.text_first);

        try {
            int index = ta.indexOf(stockTaData);
            StockTaData stockTaDataYesterday = (StockTaData) ta.get(index - 1);
            float yesterdayClose = stockTaDataYesterday.getClose();
            MarketSenseRendererHelper.addTextViewWithColor(mYesterdayCloseTextView, String.valueOf(yesterdayClose), R.color.text_first);

            float diffNumber = todayClose - yesterdayClose;
            float diffPercentage = (diffNumber / yesterdayClose) * 100;
            String format = context.getResources().getString(R.string.title_diff_format);
            if(diffNumber > 0) {
                MarketSenseRendererHelper.addTextViewWithIcon(
                        mDiffTextView,
                        String.format(format, String.format(Locale.US,"+%.2f", diffNumber), String.format(Locale.US, "+%.2f%%", diffPercentage)),
                        R.mipmap.ic_trend_arrow_up);
            } else if(diffNumber < 0) {
                MarketSenseRendererHelper.addTextViewWithIcon(
                        mDiffTextView,
                        String.format(format, String.format(Locale.US,"%.2f", diffNumber), String.format(Locale.US, "%.2f%%", diffPercentage)),
                        R.mipmap.ic_trend_arrow_down);
            } else {
                MarketSenseRendererHelper.addTextViewWithIcon(
                        mDiffTextView,
                        String.format(format, String.format(Locale.US,"%.2f", diffNumber), String.format(Locale.US, "%.2f%%", diffPercentage)),
                        R.mipmap.ic_trend_arrow_draw_white);
            }

            mDiffTextView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            MarketSenseRendererHelper.addTextView(mYesterdayCloseTextView, String.valueOf("--"));
            mDiffTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void renderStockChartDataFailed(Context context) {
        mCandleStickChart.setNoDataText(context.getResources().getString(R.string.no_data));
        Typeface typeface = Typeface.create("sans-serif", Typeface.NORMAL);
        mCandleStickChart.setNoDataTextTypeface(typeface);
        Paint paint = mCandleStickChart.getPaint(Chart.PAINT_INFO);
        paint.setTextSize(100);
        mCandleStickChart.invalidate();
        mVolumeBarChart.setNoDataText("");
        mVolumeBarChart.invalidate();
    }

    private void clear() {
        mCandleStickChart.setOnChartValueSelectedListener(null);
        mVolumeBarChart.setOnChartValueSelectedListener(null);
    }
}
