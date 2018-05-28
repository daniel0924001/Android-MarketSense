package com.idroi.marketsense.datasource;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.notification.NotificationHelper;

import java.util.Set;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_USER_SETTING;
import static com.idroi.marketsense.common.Constants.USER_SETTING_NOTIFICATION_KEY;

/**
 * Created by daniel.hsieh on 2018/5/25.
 */

public class MarketSenseFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        SharedPreferences sharedPreferences =
                this.getSharedPreferences(SHARED_PREFERENCE_USER_SETTING, Context.MODE_PRIVATE);
        boolean isNotificationEnable = sharedPreferences.getBoolean(USER_SETTING_NOTIFICATION_KEY, false);
        MSLog.d("receive notification and the user's notification setting is: " + isNotificationEnable);
        if(!isNotificationEnable) {
            return;
        }

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        MSLog.i("onMessageReceived From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            MSLog.i("onMessageReceived Message data payload: " + remoteMessage.getData());

            Set<String> keys = remoteMessage.getData().keySet();
            for(String s : keys) {
                MSLog.i("onMessageReceived " + s + ": " + remoteMessage.getData().get(s));
            }

            NotificationHelper.sendNotification(this, remoteMessage.getFrom(), remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            MSLog.i("onMessageReceived Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }
}
