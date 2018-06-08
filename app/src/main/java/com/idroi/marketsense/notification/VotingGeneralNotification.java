package com.idroi.marketsense.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.idroi.marketsense.MainActivity;
import com.idroi.marketsense.R;

import java.util.Map;

/**
 * Created by daniel.hsieh on 2018/6/8.
 */

public class VotingGeneralNotification extends BaseNotificationHandler {

    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_CORRECT_COUNT = "correct_count";
    public static final String KEY_CORRECT_RATE = "correct_rate";

    private String mTitle;
    private String mText;

    VotingGeneralNotification(Map<String, String> data) {
        super(data);
        mTitle = data.get(KEY_TITLE);
        mText = data.get(KEY_DESCRIPTION);
    }

    @Override
    protected String getTitle() {
        return mTitle;
    }

    @Override
    protected String getText() {
        return mText;
    }

    @Override
    protected int getId() {
        return R.string.notification_channel_voting_result_id;
    }

    @Override
    protected String getImageUrl() {
        return null;
    }

    @Override
    protected PendingIntent generatePendingIntent(Context context, Map<String, String> data) {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);  //Create new activity to reuse old activity (onNewIntent())
        return PendingIntent.getActivity(context, getId(), mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected String getChannelId(Context context) {
        return context.getString(R.string.notification_channel_voting_result_id);
    }

    @Override
    protected String getChannelTitle(Context context) {
        return context.getString(R.string.notification_channel_voting_result_title);
    }
}
