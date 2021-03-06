package com.idroi.marketsense.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

import java.util.Map;

/**
 * Created by daniel.hsieh on 2018/5/28.
 */

public abstract class BaseNotificationHandler {

    // http://10.128.80.50:8991/news
    // http://10.128.80.50:8991/2330

    private Map<String, String> mData;

    public static final String TITLE_KEY = "title";
    public static final String DESCRIPTION_KEY = "description";
    public static final String TOPICS = "topics";
    public static final String IMPORTANCE = "importance";

    private int mImportance;

    BaseNotificationHandler(Map<String, String> data) {
        mData = data;
        mImportance = Integer.valueOf(data.get(IMPORTANCE) != null ? data.get(IMPORTANCE) : "3");
    }

    public void sendNotification(Context context) {
        PendingIntent pendingIntent = generatePendingIntent(context, mData);
        sendNotification(context, pendingIntent, getTitle(), getText());
    }

    // Remember you can only update channel's id and name, nothing else.
    // The importance will be ignored, because the user
    // might have already changed the importance of the channel manually.
    protected int getImportanceScore() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Math.min(mImportance, NotificationManager.IMPORTANCE_DEFAULT);
        } else {
            return Math.min(mImportance, 3);
        }
    }

    protected int getPriority() {
        int priority = mImportance - 3;
        return Math.min(priority, NotificationCompat.PRIORITY_DEFAULT);
    }

    abstract protected String getTitle();

    abstract protected String getText();

    abstract protected int getId();

    abstract protected String getImageUrl();

    abstract protected PendingIntent generatePendingIntent(Context context, Map<String, String> data);

    abstract protected String getChannelId(Context context);

    abstract protected String getChannelTitle(Context context);

    private void sendNotification(Context context, PendingIntent pendingIntent, String title, String text) {
        String channelId = getChannelId(context);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_notification_logo)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setColor(context.getResources().getColor(R.color.trend_red))
                        .setContentIntent(pendingIntent)
                        .setVibrate(new long[] {0})
                        .setPriority(getPriority())
                        .setOnlyAlertOnce(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager == null) {
            MSLog.e("NotificationManager is null.");
            return;
        }

        // Since android Oreo notification channel is needed.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    getChannelTitle(context),
                    getImportanceScore());
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(channelId);
        }

        notifyWithIconSetting(notificationManager, notificationBuilder);
    }

    private void notifyWithIconSetting(final NotificationManager notificationManager, final NotificationCompat.Builder notificationBuilder) {
        String imageUrl = getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            ImageRequest imageRequest = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(imageUrl))
                    .build();
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            final DataSource<CloseableReference<CloseableImage>>
                    dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);
            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                public void onNewResultImpl(@Nullable Bitmap bitmap) {
                    if (dataSource.isFinished() && bitmap != null) {
                        //https://stackoverflow.com/questions/45668079/notificationchannel-issue-in-android-o
                        Bitmap img = Bitmap.createBitmap(bitmap);
                        notificationBuilder.setLargeIcon(img);
                        notificationManager.notify(getId(), notificationBuilder.build());
                        dataSource.close();
                    }
                }

                @Override
                public void onFailureImpl(DataSource dataSource) {
                    if (dataSource != null) {
                        notificationManager.notify(getId(), notificationBuilder.build());
                        dataSource.close();
                    }
                }
            }, CallerThreadExecutor.getInstance());
        } else {
            // no image url
            notificationManager.notify(getId(), notificationBuilder.build());
        }
    }

    static int djb2(String id) {
        Long hash = 5381L;
        int len = id.length();
        for (int index = 0; index < len; index++) {
            hash = ((hash << 5) + hash) + Character.getNumericValue(id.charAt(index));
        }
        return hash.intValue();
    }
}
