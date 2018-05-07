package com.idroi.marketsense.data;

import android.util.Log;

import com.idroi.marketsense.Logging.MSLog;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public class News {

    private static final int BEST_RISING_LEVEL = 3;
    private static final int BEST_FALLING_LEVEL = -3;

    private static final String TITLE = "title";
    private static final String SOURCE_NAME = "source_name";
    private static final String PAGE_LINK = "page_link";
    private static final String ORIGIN_LINK = "link";
    private static final String IMAGE_URL_ARRAY = "image_url_array";
    private static final String SOURCE_DATE_INT = "source_date_int";
    private static final String PREDICTION = "prediction";

    private String mTitle;
    private String mSourceName;
    private String mUrlImage;
    private String mUrlNewsPage;
    private String mUrlOriginPage;
    private String mDate;
    private int mSourceDateInt;
    private boolean mImportant;
    private int mLevel;

    public News() {
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setSourceName(String sourceName) {
        mSourceName = sourceName;
    }

    public void setPageLink(String pageLink) {
        mUrlNewsPage = pageLink;
    }

    public void setOriginLink(String originLink) {
        mUrlOriginPage = originLink;
    }

    public void setUrlImage(String urlImage) {
        mUrlImage = urlImage;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setLevel(int level) {
        mLevel = level;
        mImportant = (level == BEST_FALLING_LEVEL || level == BEST_RISING_LEVEL);
    }

    public void setSourceDateInt(int sourceDateInt) {
        mSourceDateInt = sourceDateInt;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrlImage() {
        return mUrlImage;
    }

    public String getOriginLink() {
        return mUrlOriginPage;
    }

    public String getPageLink() {
        return mUrlNewsPage;
    }

    public String getDate() {
        return mDate;
    }

    public int getSourceDateInt() {
        return mSourceDateInt;
    }

    public boolean getImportant() {
        return mImportant;
    }

    public int getLevel() {
        return mLevel;
    }

    public boolean isOptimistic() {
        return mLevel > 0;
    }

    public boolean isPessimistic() {
        return mLevel < 0;
    }

    public static News JsonObjectToNews(JSONObject jsonObject) {
        News news = new News();
        Iterator<String> iterator = jsonObject.keys();
        while(iterator.hasNext()) {
            String key = iterator.next();
            try {
                switch (key) {
                    case TITLE:
                        news.setTitle(jsonObject.optString(key));
                        break;
                    case SOURCE_NAME:
                        news.setSourceName(jsonObject.optString(key));
                        break;
                    case PAGE_LINK:
                        news.setPageLink(jsonObject.optString(key));
                        break;
                    case ORIGIN_LINK:
                        news.setOriginLink(jsonObject.optString(key));
                        break;
                    case IMAGE_URL_ARRAY:
                        JSONArray imageArray = jsonObject.optJSONArray(key);
                        if(imageArray != null && imageArray.length() > 0) {
                            news.setUrlImage(imageArray.optString(0));
                        }
                        break;
                    case SOURCE_DATE_INT:
                        news.setDate(convertToDate(jsonObject.optInt(key)));
                        news.setSourceDateInt(jsonObject.optInt(key));
                        break;
                    case PREDICTION:
                        news.setLevel(jsonObject.optInt(key));
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
        return news;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;
        News tmp = (News) obj;
        return new EqualsBuilder()
                .append(getOriginLink(), tmp.getOriginLink())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getOriginLink())
                .toHashCode();
    }

    private static String convertToDate(int sourceDate) {
        long difference = (System.currentTimeMillis() / 1000) - sourceDate;
        if(difference < 3600) {
            long lastMinutes = difference / 60;
            return lastMinutes + "分前";
        } else if(difference >= 3600 && difference < 24 * 3600){
            long lastHours = difference / 3600;
            return lastHours + "小時前";
        } else {
            Calendar cal = Calendar.getInstance(Locale.CHINESE);
            cal.setTimeInMillis(sourceDate * 1000L);
            SimpleDateFormat df = new SimpleDateFormat("MM/dd", Locale.CHINESE);
            return df.format(cal.getTime());
        }
    }
}
