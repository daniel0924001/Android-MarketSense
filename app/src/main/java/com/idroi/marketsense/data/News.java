package com.idroi.marketsense.data;

import android.util.Log;

import com.idroi.marketsense.Logging.MSLog;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public class News {

    private static final String TITLE = "title";
    private static final String SOURCE_NAME = "source_name";
    private static final String PAGE_LINK = "page_link";
    private static final String ORIGIN_LINK = "link";
    private static final String IMAGE_URL_ARRAY = "image_url_array";

    private String mTitle;
    private String mSourceName;
    private String mUrlImage;
    private String mUrlNewsPage;
    private String mUrlOriginPage;

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

    public String getTitle() {
        return mTitle;
    }

    public String getUrlImage() {
        return mUrlImage;
    }

    public String getOriginLink() {
        return mUrlOriginPage;
    }

    public static News JsonObjectToNews(JSONObject jsonObject) {
        News news = new News();
        Iterator<String> iterator = jsonObject.keys();
        while(iterator.hasNext()) {
            String key = iterator.next();
            try {
                switch (key) {
                    case TITLE:
                        news.setTitle((String) jsonObject.opt(TITLE));
                        break;
                    case SOURCE_NAME:
                        news.setSourceName((String) jsonObject.opt(SOURCE_NAME));
                        break;
                    case PAGE_LINK:
                        news.setPageLink((String) jsonObject.opt(PAGE_LINK));
                        break;
                    case ORIGIN_LINK:
                        news.setOriginLink((String) jsonObject.opt(ORIGIN_LINK));
                        break;
                    case IMAGE_URL_ARRAY:
                        JSONArray imageArray = jsonObject.optJSONArray(IMAGE_URL_ARRAY);
                        if(imageArray != null && imageArray.length() > 0) {
                            news.setUrlImage(imageArray.optString(0));
                        }
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
}
