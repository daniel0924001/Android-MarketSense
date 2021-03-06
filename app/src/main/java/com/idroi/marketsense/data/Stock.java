package com.idroi.marketsense.data;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.ClientData;

import org.json.JSONObject;

import java.util.Calendar;
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
    private static final String FOUND_PREDICT = "found_predict";
    private static final String TECH_PREDICT = "tech_predict";
    private static final String PRICE = "price";
    private static final String RAISE = "raise";
    private static final String FALL = "fall";
    private static final String TODAY_DIFF_PRED = "today_diff_predict";
    private static final String NEXT_DIFF_PRED = "next_diff_predict";
    private static final String NEXT_1D_DIFF_PRED = "next_1d_diff_predict";
    private static final String NEXT_5D_DIFF_PRED = "next_5d_diff_predict";
    private static final String NEXT_20D_DIFF_PRED = "next_20d_diff_predict";
    private static final String YESTERDAY_VOLUME = "y_vol";

    public static final int LEVEL_HIGHEST = 2;
    public static final int LEVEL_HIGH = 1;
    public static final int LEVEL_LOW = 0;
    public static final int LEVEL_INVALID = 0;

    public static final int TREND_UP = 1;
    public static final int TREND_DOWN = -1;
    public static final int TREND_FLAT = 0;

    private String mCode;
    private String mName;

    private int mRaiseNum, mFallNum;

    private int mDiffDirection = 3;
    private double mPrice, mDiffNumber, mDiffPercentage;
    private int mYesterdayVolume;

    private double mConfidence = 75;
    private int mConfidenceDirection;
    private double mVoting;
    private int mVotingDirection;
    private double mFoundation;
    private int mFoundationDirection;
    private double mTech;
    private int mTechDirection;

    private int mTodayPredictionDiffDirection, mTomorrowPredictionDiffDirection, mPredictionDiffDirection;
    private double mTodayPredictionDiffPercentage, mTomorrowPredictionDiffPercentage, mPredictionDiffPercentage;
    private double mPredictionError, mPredictionErrorWhenClosed;

    private int m1DPredictionDirection, m5DPredictionDirection, m20DPredictionDirection;

    private boolean mIsUpOrDownStop;

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

    public void checkIsStop(double yesterdayPrice, double price) {
        double maxThreshold = yesterdayPrice * 1.1;
        double minThreshold = yesterdayPrice * 0.9;
        double step = 0;
        if(yesterdayPrice < 10) {
            step = 0.01;
        } else if(yesterdayPrice < 50) {
            step = 0.05;
        } else if(yesterdayPrice < 100) {
            step = 0.1;
        } else if(yesterdayPrice < 500) {
            step = 0.5;
        } else if(yesterdayPrice < 1000) {
            step = 1;
        } else {
            step = 5;
        }

        if(price + step >= maxThreshold || price - step <= minThreshold) {
            mIsUpOrDownStop = true;
        } else {
            mIsUpOrDownStop = false;
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

    public void setFoundationDirection(double direction) {
        if(direction > 0) {
            mFoundationDirection = Stock.TREND_UP;
        } else if(direction == 0) {
            mFoundationDirection = Stock.TREND_FLAT;
        } else {
            mFoundationDirection = Stock.TREND_DOWN;
        }
    }

    public void setFoundation(double foundation) {
        mFoundation = Math.abs(foundation);
    }

    public void setTech(double tech) {
        mTech = Math.abs(tech);
    }

    public void setTechDirection(double tech) {
        if(tech > 0) {
            mTechDirection = Stock.TREND_UP;
        } else if(tech == 0) {
            mTechDirection = Stock.TREND_FLAT;
        } else {
            mTechDirection = Stock.TREND_DOWN;
        }
    }

    public void setRaiseNum(int number) {
        mRaiseNum = number;
    }

    public void setFallNum(int number) {
        mFallNum = number;
    }

    public void setTodayPrediction(double prediction) {
        mTodayPredictionDiffPercentage = Math.abs(prediction) * 100;
        if(prediction > 0) {
            mTodayPredictionDiffDirection = Stock.TREND_UP;
        } else if(prediction == 0) {
            mTodayPredictionDiffDirection = Stock.TREND_FLAT;
        } else {
            mTodayPredictionDiffDirection = Stock.TREND_DOWN;
        }
        ClientData clientData = ClientData.getInstance();
        if(clientData != null && clientData.doesUseTodayPredictionValue()) {
            mPredictionDiffPercentage = mTodayPredictionDiffPercentage;
            mPredictionDiffDirection = mTodayPredictionDiffDirection;
        }
    }

    public void setTomorrowPrediction(double prediction) {
        mTomorrowPredictionDiffPercentage = Math.abs(prediction) * 100;
        if(prediction > 0) {
            mTomorrowPredictionDiffDirection = Stock.TREND_UP;
        } else if(prediction == 0) {
            mTomorrowPredictionDiffDirection = Stock.TREND_FLAT;
        } else {
            mTomorrowPredictionDiffDirection = Stock.TREND_DOWN;
        }
        ClientData clientData = ClientData.getInstance();
        if(clientData != null &&!ClientData.getInstance().doesUseTodayPredictionValue()) {
            mPredictionDiffPercentage = mTomorrowPredictionDiffPercentage;
            mPredictionDiffDirection = mTomorrowPredictionDiffDirection;
        }
    }

    public void set1DPrediction(double prediction) {
        if(prediction > 0) {
            m1DPredictionDirection = TREND_UP;
        } else if(prediction == 0) {
            m1DPredictionDirection = TREND_FLAT;
        } else {
            m1DPredictionDirection = TREND_DOWN;
        }
    }

    public void set5DPrediction(double prediction) {
        if(prediction > 0) {
            m5DPredictionDirection = TREND_UP;
        } else if(prediction == 0) {
            m5DPredictionDirection = TREND_FLAT;
        } else {
            m5DPredictionDirection = TREND_DOWN;
        }
    }

    public void set20DPrediction(double prediction) {
        if(prediction > 0) {
            m20DPredictionDirection = TREND_UP;
        } else if(prediction == 0) {
            m20DPredictionDirection = TREND_FLAT;
        } else {
            m20DPredictionDirection = TREND_DOWN;
        }
    }

    public void setYesterdayVolume(int yesterdayVolume) {
        mYesterdayVolume = yesterdayVolume;
    }

    public int getYesterdayVolume() {
        return mYesterdayVolume;
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
                return R.color.trend_red;
            case TREND_FLAT:
                return R.color.draw_grey;
            case TREND_DOWN:
                return R.color.trend_green;
            default:
                return R.color.draw_grey;
        }
    }

    public double getDiffPercentageDouble() {
        return mDiffPercentage;
    }

    public String getDiffNumber() {
        switch (mDiffDirection) {
            case TREND_UP:
                return String.format(Locale.US, "+%.2f", mDiffNumber);
            case TREND_FLAT:
                return String.format(Locale.US, "%.2f", mDiffNumber);
            case TREND_DOWN:
                return String.format(Locale.US, "-%.2f", mDiffNumber);
            default:
                return String.format(Locale.US, "%.2f", mDiffNumber);
        }
    }

    public String getDiffPercentage() {
        switch (mDiffDirection) {
            case TREND_UP:
                return String.format(Locale.US, "+%.2f%%", mDiffPercentage);
            case TREND_FLAT:
                return String.format(Locale.US, "%.2f%%", mDiffPercentage);
            case TREND_DOWN:
                return String.format(Locale.US, "-%.2f%%", mDiffPercentage);
            default:
                return String.format(Locale.US, "%.2f%%", mDiffPercentage);
        }
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

    public int get1DPredictionDirection() {
        return m1DPredictionDirection;
    }

    public int get5DPredictionDirection() {
        return m5DPredictionDirection;
    }

    public int get20DPredictionDirection() {
        return m20DPredictionDirection;
    }

    public int getPredictNewsLevel() {
        if(mConfidence >= 0 && mConfidence <= 1) {
            return LEVEL_LOW;
        } else if(mConfidence > 1 && mConfidence <= 2) {
            return LEVEL_HIGH;
        } else if(mConfidence > 2) {
            return LEVEL_HIGHEST;
        } else {
            MSLog.e("invalid value: " + mConfidence);
            return LEVEL_INVALID;
        }
    }

    public float getPredictNewsScore() {
//        return (float) mConfidence * 3 / 100;
        return (float) mConfidence;
    }

    public int getPredictPeopleLevel() {
        if(mVoting >= 0 && mVoting <= 1) {
            return LEVEL_LOW;
        } else if(mVoting > 1 && mVoting <= 2) {
            return LEVEL_HIGH;
        } else if(mVoting > 2) {
            return LEVEL_HIGHEST;
        } else {
            MSLog.e("invalid value: " + mVoting);
            return LEVEL_INVALID;
        }
    }

    public float getPredictPeopleScore() {
//        return (float) mVoting * 3 / 100;
        return (float) mVoting;
    }

    public int getPredictPeopleDirection() {
        return mVotingDirection;
    }

    public int getPredictFoundationLevel() {
        if(mFoundation >= 0 && mFoundation <= 1) {
            return LEVEL_LOW;
        } else if(mFoundation > 1 && mFoundation <= 2) {
            return LEVEL_HIGH;
        } else if(mFoundation > 2) {
            return LEVEL_HIGHEST;
        } else {
            MSLog.e("invalid value: " + mFoundation);
            return LEVEL_INVALID;
        }
    }

    public int getPredictFoundationDirection() {
        return mFoundationDirection;
    }

    public int getPredictTechLevel() {
        if(mTech >= 0 && mTech <= 1) {
            return LEVEL_LOW;
        } else if(mTech > 1 && mTech <= 2) {
            return LEVEL_HIGH;
        } else if(mTech > 2) {
            return LEVEL_HIGHEST;
        } else {
            MSLog.e("invalid value: " + mTech);
            return LEVEL_INVALID;
        }
    }

    public float getPredictionTechScore() {
        return (float) mTech;
    }

    public int getPredictTechDirection() {
        return mTechDirection;
    }

    private void computeCustomizeError() {
        // we user today prediction
        double error = (mTodayPredictionDiffDirection != mDiffDirection) ? 10 : 0;
        if(mTodayPredictionDiffDirection == TREND_FLAT && mDiffDirection == TREND_FLAT) {
            error += 1;
        }
        error += Math.abs(mTodayPredictionDiffDirection * mTodayPredictionDiffPercentage - mDiffDirection * mDiffPercentage);
        mPredictionError = -error;

        if(isHitPredictionDirection(false)) {
            mPredictionErrorWhenClosed = 10 + mPredictionDiffDirection * mPredictionDiffPercentage;
        } else {
            mPredictionErrorWhenClosed = mPredictionDiffDirection * mPredictionDiffPercentage;
        }
    }

    public int getTomorrowPredictionDiffDirection() {
        return mTomorrowPredictionDiffDirection;
    }

    public int getTodayPredictionDiffDirection() {
        return mTodayPredictionDiffDirection;
    }

    public double getTomorrowPredictionDiffPercentage() {
        return mTomorrowPredictionDiffPercentage;
    }

    public double getTodayPredictionDiffPercentage() {
        return mTodayPredictionDiffPercentage;
    }

    public boolean isHitPredictionDirection(boolean isCountZero) {
        return (isCountZero && mDiffDirection == TREND_FLAT)
                || mTodayPredictionDiffDirection == mDiffDirection;
    }

    public double getPredictionSortScore() {
        ClientData clientData = ClientData.getInstance();
        if(clientData.isWorkDayAndStockMarketIsOpen() || clientData.isWorkDayAfterStockClosedBeforeAnswerDisclosure()) {
            return mPredictionError;
        } else {
            return mPredictionErrorWhenClosed;
        }
    }

    public void renderDiffColor(Context context, TextView textView) {
        if(mIsUpOrDownStop) {
            textView.setTextColor(context.getResources().getColor(R.color.white));
            switch (mDiffDirection) {
                case TREND_UP:
                    textView.setBackgroundColor(context.getResources().getColor(R.color.trend_red));
                    break;
                case TREND_DOWN:
                    textView.setBackgroundColor(context.getResources().getColor(R.color.trend_green));
                    break;
            }
        } else {
            textView.setBackgroundColor(context.getResources().getColor(R.color.white));
            switch (mDiffDirection) {
                case TREND_UP:
                    textView.setTextColor(context.getResources().getColor(R.color.trend_red));
                    break;
                case TREND_FLAT:
                    textView.setTextColor(context.getResources().getColor(R.color.draw_grey));
                    break;
                case TREND_DOWN:
                    textView.setTextColor(context.getResources().getColor(R.color.trend_green));
                    break;
                default:
                    textView.setTextColor(context.getResources().getColor(R.color.draw_grey));
                    break;
            }
        }
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
                        break;
                    case FOUND_PREDICT:
                        double foundPrediction = jsonObject.optDouble(FOUND_PREDICT);
                        stock.setFoundation(foundPrediction);
                        stock.setFoundationDirection(foundPrediction);
                        break;
                    case TECH_PREDICT:
                        double techPrediction = jsonObject.optDouble(TECH_PREDICT);
                        stock.setTech(techPrediction);
                        stock.setTechDirection(techPrediction);
                        break;
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
                        stock.checkIsStop(yesterdayPrice, price);
                        break;
                    case RAISE:
                        stock.setRaiseNum(jsonObject.optInt(RAISE));
                        break;
                    case FALL:
                        stock.setFallNum(jsonObject.optInt(FALL));
                        break;
                    case TODAY_DIFF_PRED:
                        stock.setTodayPrediction(jsonObject.optDouble(TODAY_DIFF_PRED));
                        break;
                    case NEXT_DIFF_PRED:
                        stock.setTomorrowPrediction(jsonObject.optDouble(NEXT_DIFF_PRED));
                        break;
                    case YESTERDAY_VOLUME:
                        stock.setYesterdayVolume(jsonObject.optInt(YESTERDAY_VOLUME));
                        break;
                    case NEXT_1D_DIFF_PRED:
                        stock.set1DPrediction(jsonObject.optDouble(NEXT_1D_DIFF_PRED));
                        break;
                    case NEXT_5D_DIFF_PRED:
                        stock.set5DPrediction(jsonObject.optDouble(NEXT_5D_DIFF_PRED));
                        break;
                    case NEXT_20D_DIFF_PRED:
                        stock.set20DPrediction(jsonObject.optDouble(NEXT_20D_DIFF_PRED));
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                MSLog.e(e.toString());
            }
        }
        stock.computeCustomizeError();
        return stock;
    }
}
