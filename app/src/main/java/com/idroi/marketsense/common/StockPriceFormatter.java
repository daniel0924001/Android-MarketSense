package com.idroi.marketsense.common;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by daniel.hsieh on 2018/5/22.
 */

public class StockPriceFormatter implements IAxisValueFormatter {

    private DecimalFormat mPriceFormat;

    public StockPriceFormatter() {
        mPriceFormat = new DecimalFormat("#,###,###.###");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mPriceFormat.format(value);
    }

    public String getMarkerFormattedValue(float value) {
        return "價: " + getFormattedValue(value, null);
    }
}
