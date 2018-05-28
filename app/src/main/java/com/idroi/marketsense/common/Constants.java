package com.idroi.marketsense.common;

import com.idroi.marketsense.data.Stock;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class Constants {

    public static final String SHARED_PREFERENCE_REQUEST_NAME = "com.idroi.marketsense.request";
    public static final String SHARED_PREFERENCE_USER_SETTING = "com.idroi.marketsense.user_setting";

    public static final String USER_SETTING_NOTIFICATION_KEY = "com.idroi.marketsense.user_setting.notification";

    public static final String HTTP = "http://";
    public static final String FACEBOOK_CONSTANTS = "Facebook";
    public static final String GOOGLE_PLUS_CONSTANTS = "GooglePlus";

    public static final Stock[] HOT_STOCKS_KEYWORDS = new Stock[] {
            new Stock("2454", "聯發科"),
            new Stock("2330", "台積電"),
            new Stock("2317", "鴻海")
    };
}
