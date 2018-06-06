package com.idroi.marketsense.common;

import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.idroi.marketsense.Logging.MSLog;

/**
 * Created by daniel.hsieh on 2018/5/28.
 */

public class FrescoHelper {

    private static final int MAX_IMAGE_SIZE = ByteConstants.MB;

    private static final MemoryCacheParams mBitmapCacheParams = new MemoryCacheParams(
            10 * ByteConstants.MB,
            Integer.MAX_VALUE,
            3 * ByteConstants.MB,
            Integer.MAX_VALUE,
            MAX_IMAGE_SIZE);

    private static final int MAX_DISK_CACHE_VERY_LOW_SIZE = 10 * ByteConstants.MB;
    private static final int MAX_DISK_CACHE_LOW_SIZE = 30 * ByteConstants.MB;
    private static final int MAX_DISK_CACHE_SIZE = 100 * ByteConstants.MB;

    public static void initialize(Context context) {
        // Fresco
        if(!Fresco.hasBeenInitialized()) {
            Supplier<MemoryCacheParams> mSupplierMemoryCacheParams = new Supplier<MemoryCacheParams>() {
                @Override
                public MemoryCacheParams get() {
                    return mBitmapCacheParams;
                }
            };

            DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context.getApplicationContext())
                    .setMaxCacheSize(MAX_DISK_CACHE_SIZE)
                    .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)
                    .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERY_LOW_SIZE)
                    .build();
            ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context.getApplicationContext())
                    .setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams)
                    .setMainDiskCacheConfig(diskCacheConfig)
                    .setDownsampleEnabled(true)
                    .setBitmapsConfig(Bitmap.Config.RGB_565)
                    .build();
            MSLog.i("Initialize Fresco");
            Fresco.initialize(context.getApplicationContext(), config);
        }
    }
}
