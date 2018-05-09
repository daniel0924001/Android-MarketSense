package com.idroi.marketsense.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class DateConverter {

    public static String convertToDate(int sourceDate) {
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
