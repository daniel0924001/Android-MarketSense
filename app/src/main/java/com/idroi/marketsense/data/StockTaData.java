package com.idroi.marketsense.data;

import com.idroi.marketsense.Logging.MSLog;

/**
 * Created by daniel.hsieh on 2018/7/18.
 */

public class StockTaData extends StockBaseData {

    private float mHighPrice;
    private float mLowPrice;
    private float mOpenPrice;
    private float mClosePrice;
    private float mDiffNumber;

    public StockTaData(Long time, int volume, double high, double low, double open, double close, Double yesterdayClose) {
        super(time, volume);
        mHighPrice = (float) high;
        mLowPrice = (float) low;
        mOpenPrice = (float) open;
        mClosePrice = (float) close;
        if(yesterdayClose != null) {
            mDiffNumber = (float) (close - yesterdayClose);
        } else {
            mDiffNumber = 0;
        }
    }

    public float getShadowHigh() {
        return mHighPrice;
    }

    public float getShadowLow() {
        return mLowPrice;
    }

    public float getOpen() {
        return mOpenPrice;
    }

    public float getClose() {
        return mClosePrice;
    }

    public float getDiffNumber() {
        return mDiffNumber;
    }

    @Override
    public int getType() {
        return TYPE_TA;
    }

    @Override
    public String toString() {
        return String.format("t: %s, v: %s, o: %s, c: %s, h: %s, l: %s",
                mTime, mVolume, mOpenPrice, mClosePrice, mHighPrice, mLowPrice);
    }
}
