package com.idroi.marketsense.data;

import android.os.SystemClock;

import com.idroi.marketsense.Logging.MSLog;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by daniel.hsieh on 2018/8/7.
 */

public class NewsReadRecord implements Serializable {
    private String mNewsId;
    private long mTimeStamp;

    public NewsReadRecord(String newsId) {
        mNewsId = newsId;
        mTimeStamp = System.currentTimeMillis() / 1000;
    }

    public String getNewsId() {
        return mNewsId;
    }

    public boolean isExpired() {
        long now = System.currentTimeMillis() / 1000;
        return now > mTimeStamp + 86400 * 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;
        NewsReadRecord tmp = (NewsReadRecord) obj;
        return new EqualsBuilder()
                .append(mNewsId, tmp.getNewsId())
                .append(false, tmp.isExpired())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(mNewsId)
                .toHashCode();
    }
}
