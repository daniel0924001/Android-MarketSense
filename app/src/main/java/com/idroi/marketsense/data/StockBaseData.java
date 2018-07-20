package com.idroi.marketsense.data;

/**
 * Created by daniel.hsieh on 2018/7/18.
 */

abstract public class StockBaseData {

    public static final int TYPE_TICK = 1;
    public static final int TYPE_TA = 2;

    protected Long mTime;
    protected int mVolume;

    public StockBaseData(Long time, int volume) {
        mTime = time;
        mVolume = volume;
    }

    public Long getTime() {
        return mTime;
    }

    public int getVolume() {
        return mVolume;
    }

    abstract public int getType();
}
