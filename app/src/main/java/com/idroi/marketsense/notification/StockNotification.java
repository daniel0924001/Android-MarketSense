package com.idroi.marketsense.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.idroi.marketsense.MainActivity;
import com.idroi.marketsense.R;
import com.idroi.marketsense.StockActivity;
import com.idroi.marketsense.data.Stock;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by daniel.hsieh on 2018/5/28.
 */

public class StockNotification extends BaseNotificationHandler {

    private Stock mStock;

    private String mTitle;
    private String mText;

    StockNotification(Map<String, String> data) {
        super(data);

        JSONObject stockJsonObject = new JSONObject(data);
        mStock = Stock.jsonObjectToStock(stockJsonObject, false);
        mTitle = stockJsonObject.optString(BaseNotificationHandler.TITLE_KEY);
        mText = stockJsonObject.optString(BaseNotificationHandler.DESCRIPTION_KEY);
    }

    @Override
    protected String getTitle() {
        if(mTitle != null) {
            return mTitle;
        } else {
            return "";
        }
    }

    @Override
    protected String getText() {
        if(mText != null) {
            return mText;
        } else {
            return "";
        }
    }

    @Override
    protected int getId() {
        return djb2(mStock.getCode());
    }

    @Override
    protected String getImageUrl() {
        return "";
    }

    @Override
    protected String getChannelId(Context context) {
        if(getImportanceScore() >= NotificationManager.IMPORTANCE_DEFAULT ||
                getPriority() >= NotificationCompat.PRIORITY_DEFAULT) {
            return context.getString(R.string.notification_channel_stock_price_id);
        } else {
            return context.getString(R.string.notification_channel_stock_price_priority_low_id);
        }
    }

    @Override
    protected String getChannelTitle(Context context) {
        if(getImportanceScore() >= NotificationManager.IMPORTANCE_DEFAULT ||
                getPriority() >= NotificationCompat.PRIORITY_DEFAULT) {
            return context.getString(R.string.notification_channel_stock_price_title);
        } else {
            return context.getString(R.string.notification_channel_stock_price_priority_low_title);
        }
    }

    @Override
    protected PendingIntent generatePendingIntent(Context context, Map<String, String> data) {

        Intent intent = StockActivity.generateStockActivityIntent(
                context, mStock.getName(), mStock.getCode(), 0, 0,
                mStock.getPrice(), mStock.getDiffNumber(), mStock.getDiffPercentage());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // if top activity isn't MarketSense main activity, then it will create new one when user clicks back
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);  //Create new activity to reuse old activity (onNewIntent())
        return PendingIntent.getActivities(context, getId(), new Intent[] {mainIntent, intent}, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
