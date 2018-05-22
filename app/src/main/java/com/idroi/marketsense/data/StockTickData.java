package com.idroi.marketsense.data;

/**
 * Created by daniel.hsieh on 2018/5/21.
 */

public class StockTickData {
    private Long mTime;
    private float mPrice;
    private int mVolume;
    private int mMinute;

    public StockTickData(Long time, double price, int volume, int minute) {
        mTime = time;
        mPrice = (float) price;
        mVolume = volume;
        mMinute = minute;
    }

    public Long getTime() {
        return mTime;
    }

    public float getPrice() {
        return mPrice;
    }

    public int getVolume() {
        return mVolume;
    }

    public int getMinute() {
        return mMinute;
    }

    @Override
    public String toString() {
        return "Time: " + mTime + ", Price: " + mPrice + ", Volume: " + mVolume + ", Minute: " + mMinute;
    }
}
