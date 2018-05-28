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

    BaseNotificationHandler(Map<String, String> data) {
        mData = data;
    }

    public void sendNotification(Context context) {
        PendingIntent pendingIntent = generatePendingIntent(context, mData);
        sendNotification(context, pendingIntent, getTitle(), getText());
    }

    abstract protected String getTitle();

    abstract protected String getText();

    abstract protected int getId();

    abstract protected String getImageUrl();

    abstract protected PendingIntent generatePendingIntent(Context context, Map<String, String> data);

    private void sendNotification(Context context, PendingIntent pendingIntent, String title, String text) {
        String channelId = context.getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_trending_up_red_24px)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setColor(context.getResources().getColor(R.color.colorPrimary))
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager == null) {
            MSLog.e("NotificationManager is null.");
            return;
        }

        // Since android Oreo notification channel is needed.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    context.getString(R.string.default_notification_channel_title),
                    NotificationManager.IMPORTANCE_DEFAULT);
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
