package com.idroi.marketsense.data;

import com.idroi.marketsense.Logging.MSLog;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class Stock {

    private static final String CODE = "code";
    private static final String NAME = "name";

    public static final int TREND_UP = 1;
    public static final int TREND_DOWN = 2;
    public static final int TREND_FLAT = 3;

    private String mCode;
    private String mName;
    private int mRankTrend = 3;
    private int mTrend;
    private int mConfidence = 75;
    private int mConfidenceDirection;

    public Stock() {

    }

    public Stock(String code, String name) {
        mCode = code;
        mName = name;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setCode(String id) {
        mCode = id;
    }

    public void setRankTrend(int rankTrend) {
        mRankTrend = rankTrend;
    }

    public void setTrend(int trend) {
        mTrend = trend;
    }

    public void setConfidence(int confidence) {
        mConfidence = confidence;
    }

    public void setConfidenceDirection(int direction) {
        mConfidenceDirection = direction;
    }

    public String getName() {
        return mName;
    }

    public String getCode() {
        return mCode;
    }

    public int getRankTrend() {
        return mRankTrend;
    }

    public int getTrend() {
        return mTrend;
    }

    public int getConfidence() {
        return mConfidence;
    }

    public int getConfidenceDirection() {
        return mConfidenceDirection;
    }

    public static Stock jsonObjectToStock(JSONObject jsonObject) {
        Stock stock = new Stock();
        Iterator<String> iterator = jsonObject.keys();
        while(iterator.hasNext()) {
            String key = iterator.next();
            try {
                switch (key) {
                    case CODE:
                        stock.setCode((String) jsonObject.opt(CODE));
                        break;
                    case NAME:
                        stock.setName((String) jsonObject.opt(NAME));
                        break;
                    default:
                        break;
                }
            } catch (ClassCastException e) {
                MSLog.e(e.toString());
            } catch (Exception e) {
                MSLog.e(e.toString());
            }
        }
        return stock;
    }
}
