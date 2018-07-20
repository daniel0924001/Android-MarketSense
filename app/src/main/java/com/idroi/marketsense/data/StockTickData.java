package com.idroi.marketsense.data;

/**
 * Created by daniel.hsieh on 2018/5/21.
 */

public class StockTickData extends StockBaseData {

    private float mPrice;
    private int mMinute;

    public StockTickData(Long time, double price, int volume, int minute) {
        super(time, volume);
        mPrice = (float) price;
        mMinute = minute;
    }

    public float getPrice() {
        return mPrice;
    }

    public int getMinute() {
        return mMinute;
    }

    @Override
    public int getType() {
        return TYPE_TICK;
    }

    @Override
    public String toString() {
        return "Time: " + mTime + ", Price: " + mPrice + ", Volume: " + mVolume + ", Minute: " + mMinute;
    }
}
