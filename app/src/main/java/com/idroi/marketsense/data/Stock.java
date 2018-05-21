package com.idroi.marketsense.data;

import android.support.annotation.Nullable;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class Stock {

    private static final String CODE = "code";
    private static final String NAME = "name";
    private static final String DIFF = "diff";
    private static final String PRED = "prediction";
    private static final String VOTING = "voting";
    private static final String PRICE = "price";
    private static final String RAISE = "raise";
    private static final String FALL = "fall";

    public static final int TREND_UP = 1;
    public static final int TREND_DOWN = -1;
    public static final int TREND_FLAT = 0;

    private String mCode;
    private String mName;
    private int mDiffDirection = 3;
    private double mConfidence = 75;
    private int mConfidenceDirection;
    private int mRaiseNum, mFallNum;

    private double mPrice, mDiffNumber, mDiffPercentage;
    private double mVoting;
    private int mVotingDirection;

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

    public void setPrice(double price) {
        mPrice = price;
    }

    public void setDiffNumber(double diff) {
        mDiffNumber = Math.abs(diff);
    }

    public void setDiffPercentage(double diff) {
        mDiffPercentage = Math.abs(diff);
    }

    public void setDiffDirection(double diff) {
        if(diff > 0) {
            mDiffDirection = Stock.TREND_UP;
        } else if(diff == 0) {
            mDiffDirection = Stock.TREND_FLAT;
        } else {
            mDiffDirection = Stock.TREND_DOWN;
        }
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

    public void setVotingDirection(double direction) {
        if(direction > 0) {
            mVotingDirection = Stock.TREND_UP;
        } else if(direction == 0) {
            mVotingDirection = Stock.TREND_FLAT;
        } else {
            mVotingDirection = Stock.TREND_DOWN;
        }
    }

    public void setVoting(double voting) {
        mVoting = Math.abs(voting);
    }

    public void setRaiseNum(int number) {
        mRaiseNum = number;
    }

    public void setFallNum(int number) {
        mFallNum = number;
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

    public int getDiffColorResourceId() {
        switch (mDiffDirection) {
            case TREND_UP:
                return R.color.colorTrendUp;
            case TREND_FLAT:
                return R.color.colorTrendFlat;
            case TREND_DOWN:
                return R.color.colorTrendDown;
            default:
                return R.color.colorTrendFlat;
        }
    }

    public int getNewsColorResourceId() {
        switch (mConfidenceDirection) {
            case TREND_UP:
                return R.color.colorTrendUp;
            case TREND_FLAT:
                return R.color.colorTrendFlat;
            case TREND_DOWN:
                return R.color.colorTrendDown;
            default:
                return R.color.colorTrendFlat;
        }
    }

    public int getPeopleColorResourceId() {
        switch (mVotingDirection) {
            case TREND_UP:
                return R.color.colorTrendUp;
            case TREND_FLAT:
                return R.color.colorTrendFlat;
            case TREND_DOWN:
                return R.color.colorTrendDown;
            default:
                return R.color.colorTrendFlat;
        }
    }

    public int getOurColorResourceId() {
        switch (mVotingDirection) {
            case TREND_UP:
                return R.color.colorTrendUp;
            case TREND_FLAT:
                return R.color.colorTrendFlat;
            case TREND_DOWN:
                return R.color.colorTrendDown;
            default:
                return R.color.colorTrendFlat;
        }
    }


    public double getDiffPercentageDouble() {
        return mDiffPercentage;
    }

    public String getDiffNumber() {
        switch (mDiffDirection) {
            case TREND_UP:
                return String.format(Locale.US, "▲ %.2f", mDiffNumber);
            case TREND_FLAT:
                return String.format(Locale.US, "%.2f", mDiffNumber);
            case TREND_DOWN:
                return String.format(Locale.US, "▼ %.2f", mDiffNumber);
            default:
                return String.format(Locale.US, "%.2f", mDiffNumber);
        }
    }

    public String getDiffPercentage() {
        return String.format(Locale.US, "(%.2f%%)", mDiffPercentage);
    }

    public String getPrice() {
        return String.format(Locale.US, "%.2f", mPrice);
    }

    public double getConfidence() {
        return mConfidence;
    }

    public int getConfidenceDirection() {
        return mConfidenceDirection;
    }

    public int getRaiseNum() {
        return mRaiseNum;
    }

    public int getFallNum() {
        return mFallNum;
    }

    public String getPredictNewsString() {
        float score = getPredictNewsScore();
        switch (mConfidenceDirection) {
            case TREND_UP:
                return String.format(Locale.US, "▲ %.1f", score);
            case TREND_FLAT:
                return String.format(Locale.US, "%.1f", score);
            case TREND_DOWN:
                return String.format(Locale.US, "▼ %.1f", score);
            default:
                return String.format(Locale.US, "%.1f", score);
        }
    }

    public float getPredictNewsScore() {
        return (float) mConfidence * 3 / 100;
    }

    public String getPredictPeopleString() {
        float score = getPredictPeopleScore();
        switch (mVotingDirection) {
            case TREND_UP:
                return String.format(Locale.US, "▲ %.1f", score);
            case TREND_FLAT:
                return String.format(Locale.US, "%.1f", score);
            case TREND_DOWN:
                return String.format(Locale.US, "▼ %.1f", score);
            default:
                return String.format(Locale.US, "%.1f", score);
        }
    }

    public float getPredictPeopleScore() {
        return (float) mVoting * 3 / 100;
    }

    public String getPredictOurString() {
        float score = getPredictOurScore();
        switch (mVotingDirection) {
            case TREND_UP:
                return String.format(Locale.US, "▲ %.1f", score);
            case TREND_FLAT:
                return String.format(Locale.US, "%.1f", score);
            case TREND_DOWN:
                return String.format(Locale.US, "▼ %.1f", score);
            default:
                return String.format(Locale.US, "%.1f", score);
        }
    }

    public float getPredictOurScore() {
        return (float) mVoting * 3 / 100;
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
                    case VOTING:
                        double voting = jsonObject.optDouble(VOTING);
                        stock.setVoting(voting);
                        stock.setVotingDirection(voting);
                    case DIFF:
                        double diff = jsonObject.optDouble(DIFF);
                        double price = jsonObject.optDouble(PRICE);
                        double yesterdayPrice = price - diff;
                        double diffPercentage = 100 * diff / yesterdayPrice;
                        if(removeNan && (jsonObject.optDouble(PRICE) == 0 || Double.isNaN(yesterdayPrice))) {
                            return null;
                        }
                        stock.setPrice(price);
                        stock.setDiffNumber(diff);
                        stock.setDiffPercentage(diffPercentage);
                        stock.setDiffDirection(diff);
                        break;
                    case RAISE:
                        stock.setRaiseNum(jsonObject.optInt(RAISE));
                        break;
                    case FALL:
                        stock.setFallNum(jsonObject.optInt(FALL));
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
