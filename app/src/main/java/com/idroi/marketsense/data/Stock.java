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

import java.util.Iterator;
import java.util.Locale;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class Stock {

    private static boolean sPredictionStateIsToday;

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
    private double mPredictionError;

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
    }

    public boolean isHitPredictionDirection(boolean isCountZero) {
        return (isCountZero && mDiffDirection == TREND_FLAT)
                || mTodayPredictionDiffDirection == mDiffDirection;
    }

    public static void initializeRightPartValue() {
        sPredictionStateIsToday = false;
    }

    public static void changeRightPartValue() {
        sPredictionStateIsToday = !sPredictionStateIsToday;
    }

    public double getEarnInPrediction(boolean simple) {
        if(mPredictionDiffDirection == TREND_FLAT) {
            return 0;
        }
        double weight;
        if(simple) {
            weight = 1;
        } else {
            weight = 1 + (mPredictionDiffPercentage / 10);
        }
        if(mPredictionDiffDirection == mDiffDirection) {
            return weight * (1 + Math.abs(mDiffPercentage/100));
        } else {
            return weight * (1 - Math.abs(mDiffPercentage/100));
        }
    }

    public double getPredictionSortScore() {
        ClientData clientData = ClientData.getInstance();
        if(clientData.isWorkDayBeforeStockClosed() ||
                clientData.isWorkDayAfterStockClosedBeforeAnswerDisclosure()) {
            return mPredictionError;
        } else {
            if(isHitPredictionDirection(false)) {
                return 10 + mPredictionDiffDirection * mPredictionDiffPercentage;
            } else {
                return mPredictionDiffDirection * mPredictionDiffPercentage;
            }
        }
    }

    public void setRightPredictionBlock(Context context,
                                        ConstraintLayout predictionBlock,
                                        TextView titleTextView,
                                        TextView valueTextView,
                                        ImageView isHitImageView) {
        Resources resources = context.getResources();
        ClientData clientData = ClientData.getInstance(context);
        isHitImageView.setVisibility(View.GONE);
        predictionBlock.setOnClickListener(null);

        int direction = mPredictionDiffDirection;
        double diffPercentage = mPredictionDiffPercentage;

        if(clientData.isWorkDayBeforeStockClosed()) {
            titleTextView.setText(resources.getString(R.string.title_predict_today));
        } else if(clientData.isWorkDayAfterStockClosedBeforeAnswerDisclosure()) {
            titleTextView.setText(resources.getString(R.string.title_predict_today_answer_disclosure));
            if(isHitPredictionDirection(false)) {
                isHitImageView.setVisibility(View.VISIBLE);
            }
        } else {
            if(sPredictionStateIsToday) {
                titleTextView.setText(resources.getString(R.string.title_predict_today_answer_disclosure));
                direction = mTodayPredictionDiffDirection;
                diffPercentage = mTodayPredictionDiffPercentage;
            } else {
                titleTextView.setText(resources.getString(R.string.title_predict_tomorrow));
                direction = mTomorrowPredictionDiffDirection;
                diffPercentage = mTomorrowPredictionDiffPercentage;
            }
            if(isHitPredictionDirection(false)) {
                isHitImageView.setVisibility(View.VISIBLE);
            }
            predictionBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Stock.changeRightPartValue();
                    ClientData clientData = ClientData.getInstance();
                    clientData.getUserProfile().globalBroadcast(UserProfile.NOTIFY_ID_RIGHT_PART_CHANGE);
                }
            });
        }

        valueTextView.setTextColor(resources.getColor(R.color.text_white));
        titleTextView.setTextColor(resources.getColor(R.color.text_white));
        switch (direction) {
            case TREND_UP:
                predictionBlock.setBackground(resources.getDrawable(R.drawable.block_predict_up_background));
                valueTextView.setText(String.format(Locale.US, "+%.2f%%", diffPercentage));
                break;
            case TREND_FLAT:
                predictionBlock.setBackgroundColor(resources.getColor(R.color.card_gray_background));
                valueTextView.setText(String.format(Locale.US, "%.2f%%", diffPercentage));
                valueTextView.setTextColor(resources.getColor(R.color.text_dark_gray));
                titleTextView.setTextColor(resources.getColor(R.color.text_dark_gray));
                break;
            case TREND_DOWN:
                predictionBlock.setBackground(resources.getDrawable(R.drawable.block_predict_down_background));
                valueTextView.setText(String.format(Locale.US, "-%.2f%%", diffPercentage));
                break;
            default:
                predictionBlock.setBackgroundColor(resources.getColor(R.color.card_gray_background));
                valueTextView.setText(String.format(Locale.US, "%.2f%%", diffPercentage));
                valueTextView.setTextColor(resources.getColor(R.color.text_dark_gray));
                titleTextView.setTextColor(resources.getColor(R.color.text_dark_gray));
                break;
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
