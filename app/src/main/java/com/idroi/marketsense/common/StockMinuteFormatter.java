package com.idroi.marketsense.common;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Locale;

/**
 * Created by daniel.hsieh on 2018/5/21.
 */

public class StockMinuteFormatter implements IAxisValueFormatter {

    private static final String mTimeFormat = "%02d:%02d";

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int hour = (int)value / 60 + 9;
        int minute = (int) value % 60;
        return String.format(Locale.US, mTimeFormat, hour, minute);
    }
}
