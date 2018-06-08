package com.idroi.marketsense.notification;

import android.content.Context;

import com.idroi.marketsense.common.FrescoHelper;

import java.util.Map;

/**
 * Created by daniel.hsieh on 2018/5/25.
 */

public class NotificationHelper {

    public static final String NEWS_GENERAL_ALL = "news-general-all";
    public static final String VOTING_GENERAL_ALL = "voting-general-all";
    public static final String STOCK_PRICE_SELECTED_SUFFIX = "-price-selected";

    public static void sendNotification(Context context, String from, Map<String, String> data) {

        BaseNotificationHandler baseNotificationHandler = null;

        if(from.contains(NEWS_GENERAL_ALL)) {
            baseNotificationHandler = new NewsNotification(data);
        } else if(from.contains(STOCK_PRICE_SELECTED_SUFFIX)) {
            baseNotificationHandler = new StockNotification(data);
        } else if(from.contains(VOTING_GENERAL_ALL)) {
            baseNotificationHandler = new VotingGeneralNotification(data);
        }

        if(baseNotificationHandler != null) {
            FrescoHelper.initialize(context);
            baseNotificationHandler.sendNotification(context);
        }

    }

    public static String getTopicForStockCode(String code) {
        return code + STOCK_PRICE_SELECTED_SUFFIX;
    }

}
