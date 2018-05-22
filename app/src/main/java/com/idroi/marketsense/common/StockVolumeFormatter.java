package com.idroi.marketsense.common;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by daniel.hsieh on 2018/5/22.
 */

public class StockVolumeFormatter implements IAxisValueFormatter {

    private static final DecimalFormat mPriceFormat = new DecimalFormat("#,###,###");

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if(value == 0) {
            return "張";
        }
        return mPriceFormat.format(value);
    }
}
