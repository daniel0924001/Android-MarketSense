package com.idroi.marketsense;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.fragments.NewsFragment;

public class MainActivity extends AppCompatActivity {

    public final static String TAG = "MainActivity";

    private FragmentManager mFragmentMgr;
    private Fragment mNewsFragment;

    public static final int MAX_IMAGE_SIZE = ByteConstants.MB;

    final MemoryCacheParams mBitmapCacheParams = new MemoryCacheParams(
            10 * ByteConstants.MB,
            Integer.MAX_VALUE,
            3 * ByteConstants.MB,
            Integer.MAX_VALUE,
            MAX_IMAGE_SIZE);

    public static final int MAX_DISK_CACHE_VERY_LOW_SIZE = 10 * ByteConstants.MB;
    public static final int MAX_DISK_CACHE_LOW_SIZE = 30 * ByteConstants.MB;
    public static final int MAX_DISK_CACHE_SIZE = 100 * ByteConstants.MB;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    MSLog.d("select navigation_home");
                    return true;
                case R.id.navigation_dashboard:
                    MSLog.d("select navigation_dashboard");
                    return true;
                case R.id.navigation_notifications:
                    MSLog.d("select navigation_notifications");
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null) {
            return;
        }

        Supplier<MemoryCacheParams> mSupplierMemoryCacheParams = new Supplier<MemoryCacheParams>() {
            @Override
            public MemoryCacheParams get() {
                return mBitmapCacheParams;
            }
        };

        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(this)
                .setMaxCacheSize(MAX_DISK_CACHE_SIZE)
                .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)
                .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERY_LOW_SIZE)
                .build();
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams)
                .setMainDiskCacheConfig(diskCacheConfig)
                .setDownsampleEnabled(true)
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();

        // Fresco
        Fresco.initialize(getApplicationContext(), config);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mNewsFragment = new NewsFragment();

        mFragmentMgr = getFragmentManager();
        mFragmentMgr.beginTransaction()
                .replace(R.id.frameLay, mNewsFragment, "TAG-NewsFragment")
                .addToBackStack(null)
                .commit();
    }

}
