package com.idroi.marketsense.common;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.idroi.marketsense.Logging.MSLog;

import java.text.DecimalFormat;
import java.util.Locale;

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
}
