package com.idroi.marketsense.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.UserProfile;

import java.util.Calendar;

/**
 * Created by daniel.hsieh on 2018/11/12.
 */

public class TimeTaskReceiver extends BroadcastReceiver {

    public final static String PREDICTION_DISCLOSURE = "com.idroi.marketsense.common.action.PREDICTION_DISCLOSURE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action != null && action.equals(PREDICTION_DISCLOSURE)) {

            ClientData clientData = ClientData.getInstance(context);
            clientData.updateClockInformation();
            clientData.getUserProfile().globalBroadcast(UserProfile.NOTIFY_ID_RIGHT_PART_CHANGE);

            registerNextPredictionDisclosureBroadcast(context);
        }
    }

    public static void registerNextPredictionDisclosureBroadcast(Context context) {
        // update clock information
        ClientData clientData = ClientData.getInstance(context);
        clientData.updateClockInformation();

        // register the next day broadcast event
        Calendar nextTaskTime = null;
        if(clientData.isWorkDayAfterAnswerDisclosure()) {
            nextTaskTime = clientData.getWorkDayPlusOne();
        } else {
            nextTaskTime = clientData.getWorkDay();
        }
        nextTaskTime.set(Calendar.HOUR_OF_DAY, 15);
        nextTaskTime.set(Calendar.MINUTE, 0);
        nextTaskTime.set(Calendar.SECOND, 5);
        nextTaskTime.set(Calendar.MILLISECOND, 0);
        Intent intentDisclosure = new Intent(PREDICTION_DISCLOSURE);

        PendingIntent pi = PendingIntent.getBroadcast(context, 1, intentDisclosure, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(am != null) {
            am.set(AlarmManager.RTC_WAKEUP, nextTaskTime.getTimeInMillis(), pi);
        }
    }
}
