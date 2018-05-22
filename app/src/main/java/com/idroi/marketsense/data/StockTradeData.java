package com.idroi.marketsense.data;

import com.idroi.marketsense.Logging.MSLog;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/5/21.
 */

public class StockTradeData {

    private ArrayList<StockTickData> mStockTickDataArrayList;
    private float mYesterdayPrice;
    private float mMinPrice, mMaxPrice;
    private float mMaxVolume;

    public StockTradeData() {

    }

    public void setStockTickData(ArrayList<StockTickData> data) {
        mStockTickDataArrayList = data;

        int maxVolume = -1;
        for(int i = 0; i < data.size(); i++) {
            StockTickData tickData = data.get(i);
            if(tickData.getVolume() > maxVolume) {
                maxVolume = tickData.getVolume();
            }
        }

        int length = String.valueOf(maxVolume).length();
        if(length == 1) {
            mMaxVolume = 10;
        } else {
            int tmp = (int) Math.pow(10, length - 2);
            mMaxVolume = (maxVolume / tmp + 1) * tmp;
        }
    }

    public ArrayList<StockTickData> getStockTickData() {
        return mStockTickDataArrayList;
    }

    public void setYesterdayPrice(float yesterdayPrice) {
        mYesterdayPrice = yesterdayPrice;
    }

    public void setMinPrice(float minPrice) {
        mMinPrice = minPrice;
    }

    public void setMaxPrice(float maxPrice) {
        mMaxPrice = maxPrice;
    }

    public float getYesterdayPrice() {
        return mYesterdayPrice;
    }

    public float getMinPrice() {
        return mMinPrice;
    }

    public float getMaxPrice() {
        return mMaxPrice;
    }

    public float getMaxVolume() {
        return mMaxVolume;
    }
}
