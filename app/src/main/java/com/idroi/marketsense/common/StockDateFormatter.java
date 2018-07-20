package com.idroi.marketsense.common;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.StockBaseData;
import com.idroi.marketsense.data.StockTradeData;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/7/19.
 */

public class StockDateFormatter implements IAxisValueFormatter {

    private ArrayList<StockBaseData> mStockTradeData;

    public StockDateFormatter(StockTradeData stockTradeData, int total) {
        mStockTradeData = stockTradeData.getStockData();
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int index = (int) value;
        boolean margin = false;
        if(index <= 0) {
            index = 0;
            margin = true;
        }
        if(index >= mStockTradeData.size() - 1) {
            index = mStockTradeData.size() - 1;
            margin = true;
        }
        try {
            String t = mStockTradeData.get(index).getTime().toString();
            if(margin) {
                return t.substring(2,4) + "/" + t.substring(4,6);
            } else {
                return t.substring(0,4) + "/" + t.substring(4,6);
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static String getMarkerFormattedValue(String date) {
        return date.substring(0,4) + "/" + date.substring(4,6) + "/" + date.substring(6,8);
    }
}
