package com.idroi.marketsense.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.analytics.CampaignTrackingReceiver;

/**
 * Created by daniel.hsieh on 2018/7/27.
 */

public class CustomReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new CampaignTrackingReceiver().onReceive(context, intent);
    }
}
