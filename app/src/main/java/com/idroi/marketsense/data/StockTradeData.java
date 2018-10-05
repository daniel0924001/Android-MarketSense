package com.idroi.marketsense.data;

import com.idroi.marketsense.common.StockVolumeFormatter;
import com.idroi.marketsense.request.StockChartDataRequest;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/5/21.
 */

public class StockTradeData {

    public static final String STOCK_TRADE_DATA_TYPE_TICK = StockChartDataRequest.PARAM_TICK;
    public static final String STOCK_TRADE_DATA_TYPE_TA = StockChartDataRequest.PARAM_TA;

    private String mType;
    private ArrayList<StockBaseData> mStockDataArrayList;
    private float mOpenPrice, mHighPrice, mLowPrice, mYesterdayPrice;
    private float mMinPrice, mMaxPrice;
    private float mMaxVolume;
    private float mRealPrice, mDiffPrice, mDiffPercentage;

    private float mTaHighPrice, mTaLowPrice;
    private float mBuyPrice, mSellPrice;
    private int mYesterdayVolume;
    private float mTradeMoney;

    private String mTradeDay;
    private int mTotalVolume;
    private BestPriceRow[] mBestPriceRow;

    public static class BestPriceRow {
        private float mBuyPrice;
        private float mSellPrice;
        private int mBuyVolume;
        private int mSellVolume;

        public BestPriceRow(float buyPrice, int buyVolume, float sellPrice, int sellVolume) {
            mBuyPrice = buyPrice;
            mBuyVolume = buyVolume;
            mSellPrice = sellPrice;
            mSellVolume = sellVolume;
        }

        public float getBuyPrice() {
            return mBuyPrice;
        }

        public float getSellPrice() {
            return mSellPrice;
        }

        public int getBuyVolume() {
            return mBuyVolume;
        }

        public int getSellVolume() {
            return mSellVolume;
        }
    }

    public StockTradeData(String type) {
        mType = type;
    }

    public void setStockTransactionData(ArrayList<StockBaseData> data) {
        mStockDataArrayList = data;

        int maxVolume = -1;
        for(int i = 0; i < data.size(); i++) {
            StockBaseData tickData = data.get(i);
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

    public int size() {
        if(mStockDataArrayList != null) {
            return mStockDataArrayList.size();
        } else {
            return 0;
        }
    }

    public ArrayList<StockBaseData> getStockData() {
        return mStockDataArrayList;
    }

    public void setOpenPrice(float openPrice) {
        mOpenPrice = openPrice;
    }

    public void setHighPrice(float highPrice) {
        mHighPrice = highPrice;
    }

    public void setLowPrice(float lowPrice) {
        mLowPrice = lowPrice;
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

    public void setRealPrice(float realPrice) {
        mRealPrice = realPrice;
    }

    public void setDiffPrice(float diffPrice) {
        mDiffPrice = diffPrice;
    }

    public void setDiffPercentage(float diffPercentage) {
        mDiffPercentage = diffPercentage;
    }

    public float getOpenPrice() {
        return mOpenPrice;
    }

    public float getHighPrice() {
        return mHighPrice;
    }

    public float getLowPrice() {
        return mLowPrice;
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

    public float getRealPrice() {
        return mRealPrice;
    }

    public float getDiffPrice() {
        return mDiffPrice;
    }

    public float getDiffPercentage() {
        return mDiffPercentage;
    }

    public String getType() {
        return mType;
    }

    /* ta part */
    public StockTaData getLastStockTaData(int lastOrder) {
        try {
            if (mStockDataArrayList != null) {
                return (StockTaData) mStockDataArrayList.get(mStockDataArrayList.size() - 1 - lastOrder);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String getTaTradeDate(String date) {
        try {
            return date.substring(0, 4) + "/" + date.substring(4, 6) + "/" + date.substring(6, 8);
        } catch (Exception e) {
            return "";
        }
    }

    public void setTaHighPrice(float taMaxPrice) {
        mTaHighPrice = taMaxPrice;
    }

    public void setTaLowPrice(float taMinPrice) {
        mTaLowPrice = taMinPrice;
    }

    public float getTaHighPrice() {
        return mTaHighPrice;
    }

    public float getTaLowPrice() {
        return mTaLowPrice;
    }

    /* tick part */
    public void setTickTradeDate(String date) {
        if(date != null) {
            mTradeDay = date.substring(4, 6) + "/" + date.substring(6, 8);
        }
    }

    public String getTickTradeDay() {
        return mTradeDay;
    }

    public void setTickTotalVolume(float totalVolume) {
        mTotalVolume = (int) totalVolume;
    }

    public String getTickTotalVolume() {
        return StockVolumeFormatter.getFormattedValue(mTotalVolume);
    }

    public void setTickBuyPrice(float buyPrice) {
        mBuyPrice = buyPrice;
    }

    public float getTickBuyPrice() {
        return mBuyPrice;
    }

    public void setTickSellPrice(float sellPrice) {
        mSellPrice = sellPrice;
    }

    public float getTickSellPrice() {
        return mSellPrice;
    }

    public void setYesterdayVolume(float yesterdayVolume) {
        mYesterdayVolume = (int) yesterdayVolume;
    }

    public int getYesterdayVolume() {
        return mYesterdayVolume;
    }

    public void setTradeMoney(float tradeMoney) {
        mTradeMoney = tradeMoney / 100000;
    }

    public float getTradeMoney() {
        return mTradeMoney;
    }

    public void setFiveBestPrice(int index, float buyPrice, float sellPrice, int buyVolume, int sellVolume) {
        if(mBestPriceRow == null) {
            mBestPriceRow = new BestPriceRow[5];
        }

        if(index < 0 || index >= 5) {
            return;
        }

        mBestPriceRow[index] = new BestPriceRow(buyPrice, buyVolume, sellPrice, sellVolume);
    }

    public BestPriceRow[] getFiveBestPrice() {
        return mBestPriceRow;
    }
}
