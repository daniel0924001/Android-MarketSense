package com.idroi.marketsense.data;

import android.support.annotation.Nullable;

import com.idroi.marketsense.Logging.MSLog;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class Stock {

    private static final String CODE = "code";
    private static final String NAME = "name";
    private static final String DIFF = "diff";
    private static final String PRED = "prediction";
    private static final String PRICE = "price";

    public static final int TREND_UP = 1;
    public static final int TREND_DOWN = -1;
    public static final int TREND_FLAT = 0;

    private String mCode;
    private String mName;
    private double mDiff;
    private int mDiffDirection = 3;
    private double mConfidence = 75;
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

    public void setDiffDirection(double rankTrend) {
        if(rankTrend > 0) {
            mDiffDirection = Stock.TREND_UP;
        } else if(rankTrend == 0) {
            mDiffDirection = Stock.TREND_FLAT;
        } else {
            mDiffDirection = Stock.TREND_DOWN;
        }
    }

    public void setDiff(double trend) {
        mDiff = Math.abs(trend);
    }

    public void setConfidenceDirection(double direction) {
        if(direction > 0) {
            mConfidenceDirection = Stock.TREND_UP;
        } else if(direction == 0) {
            mConfidenceDirection = Stock.TREND_FLAT;
        } else {
            mConfidenceDirection = Stock.TREND_DOWN;
        }
    }

    public void setConfidence(double confidence) {
        mConfidence = Math.abs(confidence);
    }

    public String getName() {
        return mName;
    }

    public String getCode() {
        return mCode;
    }

    public int getDiffDirection() {
        return mDiffDirection;
    }

    public double getDiff() {
        return mDiff;
    }

    public double getConfidence() {
        return mConfidence;
    }

    public int getConfidenceDirection() {
        return mConfidenceDirection;
    }

    @Nullable
    public static Stock jsonObjectToStock(JSONObject jsonObject, boolean removeNan) {
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
                    case PRED:
                        double pred = jsonObject.optDouble(PRED);
                        stock.setConfidence(pred);
                        stock.setConfidenceDirection(pred);
                        break;
                    case DIFF:
                        double diff = jsonObject.optDouble(DIFF);
                        double price = jsonObject.optDouble(PRICE) - diff;
                        double value = 100 * diff / price;
                        if(removeNan && (jsonObject.optDouble(PRICE) == 0 || Double.isNaN(value))) {
                            return null;
                        }
                        stock.setDiff(value);
                        stock.setDiffDirection(value);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                MSLog.e(e.toString());
            }
        }
        return stock;
    }
}
