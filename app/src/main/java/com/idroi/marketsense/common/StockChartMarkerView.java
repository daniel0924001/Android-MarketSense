package com.idroi.marketsense.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.data.StockTickData;

/**
 * Created by daniel.hsieh on 2018/5/24.
 */

@SuppressLint("ViewConstructor")
public class StockChartMarkerView extends MarkerView {

    private TextView mTimeTextView;
    private TextView mPriceTextView;
    private TextView mVolumeTextView;
    private StockMinuteFormatter mStockMinuteFormatter;
    private StockPriceFormatter mStockPriceFormatter;
    private StockVolumeFormatter mStockVolumeFormatter;

    private MPPointF mOffset;

    public StockChartMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        mTimeTextView = findViewById(R.id.marker_chart_time);
        mPriceTextView = findViewById(R.id.marker_chart_price);
        mVolumeTextView = findViewById(R.id.marker_chart_volume);

        mStockMinuteFormatter = new StockMinuteFormatter();
        mStockPriceFormatter = new StockPriceFormatter();
        mStockVolumeFormatter = new StockVolumeFormatter();
    }

    @Override
    public void refreshContent(Entry entry, Highlight highlight) {

        try {
            StockTickData data = (StockTickData) entry.getData();
            String timeString = mStockMinuteFormatter.getFormattedValue((float)data.getMinute(), null);
            String priceString = mStockPriceFormatter.getMarkerFormattedValue(data.getPrice());
            String volumeString = mStockVolumeFormatter.getMarkerFormattedValue((float)data.getVolume());
            mTimeTextView.setText(timeString);
            mPriceTextView.setText(priceString);
            mVolumeTextView.setText(volumeString);
        } catch (ClassCastException e) {
            MSLog.e("refreshContent exception: " + e.toString());
            return;
        }

        super.refreshContent(entry, highlight);
    }

    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            mOffset = new MPPointF(0, -100 * getHeight());
        }

        return mOffset;
    }
}
