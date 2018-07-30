package com.idroi.marketsense.notification;

import android.content.Context;

import com.idroi.marketsense.common.FrescoHelper;

import java.util.Map;

/**
 * Created by daniel.hsieh on 2018/5/25.
 */

public class NotificationHelper {

    public static final String USER_REGISTRATION_TOKEN_PREFIX = "user-registration-token-";
    public static final String NEWS_GENERAL_ALL = "news-general-all";
    public static final String VOTING_GENERAL_ALL = "voting-general-all";
    public static final String STOCK_PRICE_SELECTED_SUFFIX = "-price-selected";

    public static void sendNotification(Context context, Map<String, String> data) {

        BaseNotificationHandler baseNotificationHandler = null;

        String topics = data.get(BaseNotificationHandler.TOPICS) != null
                ? data.get(BaseNotificationHandler.TOPICS) : "";
        if(topics.contains(NEWS_GENERAL_ALL)) {
            baseNotificationHandler = new NewsNotification(data);
        } else if(topics.contains(STOCK_PRICE_SELECTED_SUFFIX)) {
            baseNotificationHandler = new StockNotification(data);
        } else if(topics.contains(VOTING_GENERAL_ALL)) {
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
