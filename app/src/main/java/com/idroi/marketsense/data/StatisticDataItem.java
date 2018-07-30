package com.idroi.marketsense.data;

import android.content.Context;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by daniel.hsieh on 2018/7/24.
 */

public class StatisticDataItem {

    public static final int TYPE_TITLE = 1;
    public static final int TYPE_NORMAL = 2;
    public static final int TYPE_5_COLUMNS = 3;

    public static class Builder {
        private int mType;
        private String mTitle;
        private String mPrefixKeyName;
        private String mKeyName;
        private boolean mColorful;

        private ArrayList<String> mKeyNames;
        private ArrayList<String> mValues;

        public Builder(String title, String keyName) {
            mType = TYPE_NORMAL;
            mTitle = title;
            mKeyName = keyName;
            mColorful = false;
        }

        public Builder keyNames(String... keyNames) {
            mKeyNames = new ArrayList<>();
            mKeyNames.addAll(Arrays.asList(keyNames));
            return this;
        }

        public Builder values(String... values) {
            mValues = new ArrayList<>();
            mValues.addAll(Arrays.asList(values));
            return this;
        }

        public Builder prefixKeyName(String prefixKeyName) {
            mPrefixKeyName = prefixKeyName;
            return this;
        }

        public Builder type(int type) {
            mType = type;
            return this;
        }

        public Builder colorful(boolean isColorful) {
            mColorful = isColorful;
            return this;
        }

        public StatisticDataItem build() {
            return new StatisticDataItem(this);
        }
    }

    private int mType;
    private String mTitle;
    private String mPrefixKeyName;
    private String mKeyName;
    private String mValue;
    private boolean mColorful;

    private ArrayList<String> mKeyNames;
    private ArrayList<String> mValues;

    public StatisticDataItem(final Builder builder) {
        mType = builder.mType;
        mTitle = builder.mTitle;
        mPrefixKeyName = builder.mPrefixKeyName;
        mKeyName = builder.mKeyName;
        mColorful = builder.mColorful;
        mKeyNames = builder.mKeyNames;
        mValues = builder.mValues;
    }

    private void setValue(JSONObject dataJsonObject) {

        if(mKeyNames != null && mValues == null) {
            mValues = new ArrayList<>();
            for(String key : mKeyNames) {
                mValues.add(dataJsonObject.optString(key, "--"));
            }
        } else {
            mValue = dataJsonObject.optString(mKeyName, "--");
            if(mValue.isEmpty()) {
                mValue = "--";
            }
        }

        if(mPrefixKeyName != null) {
            mTitle = String.format(mTitle, dataJsonObject.optString(mPrefixKeyName));
        }
    }

    public int getType() {
        return mType;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPrefixKeyName() {
        return mPrefixKeyName;
    }

    public String getKeyName() {
        return mKeyName;
    }

    public String getValue() {
        return mValue;
    }

    public String getValueInIndex(int index) {
        try {
            return mValues.get(index);
        } catch (Exception e) {
            return "--";
        }
    }

    public void changeColor(Context context, TextView valueTextView, String value) {
        if(mColorful && value != null) {
            value = value.replace("%", "");
            float valueFloat = Float.valueOf(value);
            if(valueFloat > 0) {
                valueTextView.setTextColor(context.getResources().getColor(R.color.colorTrendUp));
            } else if(valueFloat < 0) {
                valueTextView.setTextColor(context.getResources().getColor(R.color.colorTrendDown));
            } else {
                valueTextView.setTextColor(context.getResources().getColor(R.color.colorTrendFlat));
            }
        }
    }

    public static void initStatisticData(ArrayList<StatisticDataItem> statisticDataItemArrayList,
                                         JSONObject dataJsonObject) {
        for(StatisticDataItem statisticDataItem : statisticDataItemArrayList) {
            statisticDataItem.setValue(dataJsonObject);
        }
    }
}
