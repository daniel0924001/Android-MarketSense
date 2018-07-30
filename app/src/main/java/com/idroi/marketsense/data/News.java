package com.idroi.marketsense.data;

import android.support.annotation.Nullable;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.DateConverter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public class News {

    private static final int BEST_RISING_LEVEL = 3;
    private static final int BEST_FALLING_LEVEL = -3;

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String SOURCE_NAME = "source_name";
    private static final String PAGE_LINK = "page_link";
    private static final String ORIGIN_LINK = "link";
    private static final String IMAGE_URL_ARRAY = "image_url_array";
    private static final String IMAGE_URL = "image_url";
    private static final String SOURCE_DATE_INT = "source_date_int";
    private static final String SOURCE_DATE_STR = "source_date_str";
    private static final String PREDICTION = "prediction";
    private static final String RAISE = "raise";
    private static final String FALL = "fall";
    private static final String HIGHLIGHT_SENTENCE = "highlight_sens";
    private static final String STOCK_KEYWORDS = "stock_keywords";

    private String mId;
    private String mTitle;
    private String mDescription;
    private String mSourceName;
    private String mUrlImage;
    private String mUrlNewsPage;
    private String mUrlOriginPage;
    private String mDate;
    private int mSourceDateInt;
    private boolean mImportant;
    private int mLevel;
    private int mVoteRaiseNum;
    private int mVoteFallNum;

    @Nullable private ArrayList<HighLightSentence> mHighLightSentences;
    @Nullable private News mNextNews;
    @Nullable private String[] mStockKeywords;

    public News() {
    }

    public void setId(String id) {
        mId = id;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setDescription(String description) {
        mDescription = description;
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

    public void setVoteRaiseNum(int num) {
        mVoteRaiseNum = num;
    }

    public void setVoteFallNum(int num) {
        mVoteFallNum = num;
    }

    public void setNextNews(News news) {
        mNextNews = news;
    }

    public void setHighlightSentence(JSONArray jsonArray) {
        if(jsonArray != null) {
            mHighLightSentences = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    HighLightSentence sentence =
                            HighLightSentence.jsonObjectToHighLightSentence(jsonArray.getJSONObject(i));
                    mHighLightSentences.add(sentence);
                } catch (JSONException e) {
                    MSLog.e("JSONException in setHighlightSentence: " + e.toString());
                }
            }
        }
    }

    public void setStockKeywords(JSONArray jsonArray) {
        if(jsonArray != null) {
            mStockKeywords = new String[jsonArray.length()];
            for(int i = 0; i < jsonArray.length(); i++) {
                mStockKeywords[i] = jsonArray.optString(i);
            }
        }
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
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

    public int getVoteRaiseNum() {
        return mVoteRaiseNum;
    }

    public int getVoteFallNum() {
        return mVoteFallNum;
    }

    public boolean isOptimistic() {
        return mLevel > 0;
    }

    public boolean isPessimistic() {
        return mLevel < 0;
    }

    public News getNextNews() {
        return mNextNews;
    }

    public String[] getStockKeywords() {
        return mStockKeywords;
    }

    @Nullable
    public ArrayList<HighLightSentence> getHighLightSentence() {
        return mHighLightSentences;
    }

    public static News jsonObjectToNews(JSONObject jsonObject) {
        News news = new News();
        Iterator<String> iterator = jsonObject.keys();
        while(iterator.hasNext()) {
            String key = iterator.next();
            try {
                switch (key) {
                    case ID:
                        news.setId(jsonObject.optString(key));
                        break;
                    case TITLE:
                        news.setTitle(jsonObject.optString(key));
                        break;
                    case DESCRIPTION:
                        news.setDescription(jsonObject.optString(key));
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
                    case IMAGE_URL:
                        news.setUrlImage(jsonObject.optString(key));
                        break;
                    case SOURCE_DATE_INT:
                        news.setDate(DateConverter.convertToDate(jsonObject.optInt(key)));
                        news.setSourceDateInt(jsonObject.optInt(key));
                        break;
                    case SOURCE_DATE_STR:
                        int source_date_int = Integer.parseInt(jsonObject.optString(key));
                        news.setDate(DateConverter.convertToDate(source_date_int));
                        news.setSourceDateInt(source_date_int);
                    case PREDICTION:
                        news.setLevel(jsonObject.optInt(key));
                        break;
                    case RAISE:
                        news.setVoteRaiseNum(jsonObject.optInt(key));
                        break;
                    case FALL:
                        news.setVoteFallNum(jsonObject.optInt(key));
                        break;
                    case HIGHLIGHT_SENTENCE:
                        news.setHighlightSentence(jsonObject.optJSONArray(HIGHLIGHT_SENTENCE));
                        break;
                    case STOCK_KEYWORDS:
                        news.setStockKeywords(jsonObject.optJSONArray(STOCK_KEYWORDS));
                        break;
                    default:
                        break;
                }
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
}
