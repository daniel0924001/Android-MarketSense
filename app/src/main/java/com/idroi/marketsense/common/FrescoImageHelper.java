package com.idroi.marketsense.common;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by daniel.hsieh on 2017/5/8.
 */

public class FrescoImageHelper {

    public interface ImageListener {
        void onImagesCached();

        void onImagesFailedToCache(MarketSenseError errorCode);
    }

    public final static float MAIN_IMAGE_RATIO = 16.0f/9;
    public final static float ICON_IMAGE_RATIO = 1.0f;

    static void preCacheImages(@NonNull final Context context,
                               @NonNull final List<String> imageUrls,
                               @NonNull final FrescoImageHelper.ImageListener imageListener){

        if(imageUrls.size() == 0){
            imageListener.onImagesCached();
            return;
        }

        // These Atomics are only accessed on the main thread.
        // We use Atomics here so we can change their values while keeping a reference for the inner class.
        final AtomicInteger imageCounter = new AtomicInteger(imageUrls.size());
        final AtomicBoolean anyFailures = new AtomicBoolean(false);

        FrescoImageListener volleyImageListener = new FrescoImageListener() {
            @Override
            public void onResponse(String imageUrl, DataSource<Void> dataSource, boolean isImmediate) {
                if(dataSource == null){
                    MSLog.e("Fresco preCacheImages failed: dataSource is null");
                    onError(imageCounter, anyFailures, imageListener);
                    return;
                }
                if(dataSource.hasFailed()){
                    MSLog.e("Fresco preCacheImages failed: ", dataSource.getFailureCause());
                    onError(imageCounter, anyFailures, imageListener);
                } else {
                    final int count = imageCounter.decrementAndGet();
                    if(count == 0 && !anyFailures.get()){
                        imageListener.onImagesCached();
                    }
                }
            }
        };

        for (String url : imageUrls) {
            if (TextUtils.isEmpty(url)) {
                anyFailures.set(true);
                imageListener.onImagesFailedToCache(MarketSenseError.IMAGE_DOWNLOAD_FAILURE);
                return;
            }
            get(url, volleyImageListener);
        }
    }

    private static void get(final String url, final FrescoImageListener listener){

        throwIfNotOnMainThread();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
//        Uri uri = Uri.parse(url);
//        final boolean inMemoryCache = imagePipeline.isInDiskCacheSync(uri);

        com.facebook.imagepipeline.request.ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                .build();

        DataSource<Void> dataSource =
                imagePipeline.prefetchToDiskCache(imageRequest, UiThreadImmediateExecutorService.getInstance());

        DataSubscriber<Void> dataSubscriber =
                new BaseDataSubscriber<Void>() {
                    @Override
                    public void onNewResultImpl(DataSource<Void> dataSource) {
                        if(!dataSource.isFinished()){
                            MSLog.w("fresco onNewResultImpl is not finished");
                            return;
                        }
                        // second parameter is for test (open above imagePipeline.isInDiskCacheSync(uri))
                        listener.onResponse(url, dataSource, true);

                    }

                    @Override
                    public void onFailureImpl(DataSource<Void> dataSource) {
                        MSLog.e("fresco onFailureImpl is not finished");
                        listener.onResponse(url, dataSource, true);
                    }
                };
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());

    }

    private static void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
        }
    }


    public static void loadImageView(@Nullable final String url, @Nullable final SimpleDraweeView imageView, final float ratio) {
        if(imageView == null){
            return;
        }

        if(url == null){
            MSLog.w("Cannot load image with null url");
            imageView.setImageURI((String) null);
            return;
        }

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(imageView.getController())
                .setAutoPlayAnimations(true)
                .setControllerListener(new ControllerListener<ImageInfo>() {
                    @Override
                    public void onSubmit(String id, Object callerContext) {

                    }

                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        changeImageViewLayout(imageView, imageInfo, ratio);                    }

                    @Override
                    public void onIntermediateImageSet(String id, ImageInfo imageInfo) {

                    }

                    @Override
                    public void onIntermediateImageFailed(String id, Throwable throwable) {
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                    }

                    @Override
                    public void onRelease(String id) {

                    }
                })
                .setImageRequest(request)
                .build();
        imageView.setController(controller);
    }

    private static void changeImageViewLayout(final SimpleDraweeView imageView, final ImageInfo imageInfo, final float defaultRatio){

        int imageHeight = 0;
        int imageWidth = 0;

        if(imageInfo != null){
            imageHeight = imageInfo.getHeight();
            imageWidth = imageInfo.getWidth();
        }

        if(imageHeight == 0 && imageWidth == 0){
            imageView.setAspectRatio(defaultRatio);
            MSLog.w("set ratio of SimpleDraweeView to default value");
        } else {
            imageView.setAspectRatio((float) imageWidth / imageHeight);
        }
    }

    private static void onError(final AtomicInteger imageCounter,
                                final AtomicBoolean anyFailures,
                                final FrescoImageHelper.ImageListener imageListener){

        boolean anyPreviousErrors = anyFailures.getAndSet(true);
        imageCounter.decrementAndGet();
        if(!anyPreviousErrors){
            imageListener.onImagesFailedToCache(MarketSenseError.IMAGE_DOWNLOAD_FAILURE);
        }
    }
}
