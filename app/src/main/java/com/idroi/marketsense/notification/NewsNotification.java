package com.idroi.marketsense.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.idroi.marketsense.MainActivity;
import com.idroi.marketsense.NewsWebViewActivity;
import com.idroi.marketsense.data.News;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by daniel.hsieh on 2018/5/28.
 */

public class NewsNotification extends BaseNotificationHandler {

    private News mNews;

    NewsNotification(Map<String, String> data) {
        super(data);

        JSONObject newsJsonObject = new JSONObject(data);
        mNews = News.jsonObjectToNews(newsJsonObject);
    }

    @Override
    protected String getTitle() {
        if(mNews != null) {
            return mNews.getTitle();
        } else {
            return "";
        }
    }

    @Override
    protected String getText() {
        if(mNews != null) {
            return mNews.getDescription();
        } else {
            return "";
        }
    }

    @Override
    protected int getId() {
        return djb2(mNews.getId());
    }

    @Override
    protected String getImageUrl() {
        if(mNews != null) {
            return mNews.getUrlImage();
        } else {
            return "";
        }
    }

    @Override
    protected PendingIntent generatePendingIntent(Context context, Map<String, String> data) {

        Intent intent = NewsWebViewActivity.generateNewsWebViewActivityIntent(context, mNews);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // if top activity isn't MarketSense main activity, then it will create new one when user clicks back
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);  //Create new activity to reuse old activity (onNewIntent())
        return PendingIntent.getActivities(context, getId(), new Intent[] {mainIntent, intent}, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
