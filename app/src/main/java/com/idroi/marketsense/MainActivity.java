package com.idroi.marketsense;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.BaseScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.ChoiceScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.PredictScreenSlidePagerAdapter;
import com.idroi.marketsense.adapter.NewsScreenSlidePagerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.MarketSenseCommonNavigator;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;

import static com.idroi.marketsense.SearchAndResponseActivity.EXTRA_SELECTED_COMPANY_CODE_KEY;
import static com.idroi.marketsense.SearchAndResponseActivity.EXTRA_SELECTED_COMPANY_NAME_KEY;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;

public class MainActivity extends AppCompatActivity {

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

    private ViewPager mViewPager;
    private MagicIndicator mMagicIndicator;
    private int mLastSelectedItemId = -1;

    public final static int sSearchRequestCode = 1;
    public final static int sSettingRequestCode = 2;
    private SimpleDraweeView mAvatarImageView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if(mLastSelectedItemId != item.getItemId()) {
                mLastSelectedItemId = item.getItemId();
                switch (item.getItemId()) {
                    case R.id.navigation_predict:
                        setViewPager(R.id.navigation_predict);
                        return true;
                    case R.id.navigation_news:
                        setViewPager(R.id.navigation_news);
                        return true;
                    case R.id.navigation_choices:
                        setViewPager(R.id.navigation_choices);
                        return true;
                }
            }
            return false;
        }
    };

    private ViewPager.OnPageChangeListener mOnPageChangeListener
            = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if(mMagicIndicator != null) {
                mMagicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if(mMagicIndicator != null) {
                mMagicIndicator.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if(mMagicIndicator != null) {
                mMagicIndicator.onPageScrollStateChanged(state);
            }
        }
    };

    private FloatingActionButton.OnClickListener mOnFabClickListener
            = new FloatingActionButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, SearchAndResponseActivity.class);
            startActivityForResult(intent, sSearchRequestCode);
            overridePendingTransition(R.anim.enter, R.anim.stop);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MSLog.i("Enter MainActivity");
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

        setActionBar();
        setViewPager();
    }

    @Override
    protected void onDestroy() {
        MSLog.i("Exit MainActivity");
        super.onDestroy();
    }

    private void setAvatarImage() {
        if(mAvatarImageView != null) {
            if (FBHelper.checkFBLogin()) {
                mAvatarImageView.setImageURI(
                        ClientData.getInstance(this).getUserProfile().getUserAvatarLink());
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
                roundingParams.setRoundAsCircle(true);
                mAvatarImageView.getHierarchy().setRoundingParams(roundingParams);
            } else {
                mAvatarImageView.setImageResource(R.drawable.ic_account_circle_white_24px);
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(0);
                roundingParams.setRoundAsCircle(false);
                mAvatarImageView.getHierarchy().setRoundingParams(roundingParams);
            }
        }
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.main_action_bar, null);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);

            mAvatarImageView = view.findViewById(R.id.action_bar_avatar);
            if(mAvatarImageView != null) {
                mAvatarImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivityForResult(intent, sSettingRequestCode);
                        overridePendingTransition(R.anim.enter, R.anim.stop);
                    }
                });
                setAvatarImage();
            }

            ImageView notificationView = view.findViewById(R.id.action_bar_notification);
            if(notificationView != null) {
                notificationView.setVisibility(View.VISIBLE);
                notificationView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.stop);
                    }
                });
            }
        }
    }

    private void setFab(boolean show) {
        final FloatingActionButton fab = findViewById(R.id.fab_add);
        if(fab != null) {
            if (show) {
                fab.setVisibility(View.VISIBLE);
                fab.setOnClickListener(mOnFabClickListener);
            } else {
                fab.setVisibility(View.GONE);
                fab.setOnClickListener(null);
            }
        }
    }

    private void setViewPager() {
        setViewPager(R.id.navigation_predict);
    }

    private void setViewPager(int itemId) {

        clearViewPager();

        // Initialize the ViewPager and set an adapter
        mViewPager = findViewById(R.id.pager);
        BaseScreenSlidePagerAdapter baseScreenSlidePagerAdapter = null;
        switch (itemId) {
            case R.id.navigation_predict:
                baseScreenSlidePagerAdapter =
                        new PredictScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(false);
                break;
            case R.id.navigation_news:
                baseScreenSlidePagerAdapter =
                        new NewsScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(false);
                break;
            case R.id.navigation_choices:
                baseScreenSlidePagerAdapter =
                        new ChoiceScreenSlidePagerAdapter(this, getSupportFragmentManager());
                setFab(true);
                break;
            default:
                // invalid category
                return;
        }
        mViewPager.setAdapter(baseScreenSlidePagerAdapter);

        mMagicIndicator = (MagicIndicator) findViewById(R.id.tabs);
        MarketSenseCommonNavigator commonNavigator =
                new MarketSenseCommonNavigator(this, mViewPager,
                        baseScreenSlidePagerAdapter.getTitles());

        mViewPager.addOnPageChangeListener(mOnPageChangeListener);

        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mViewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == sSearchRequestCode) {
            if(resultCode == RESULT_OK) {
                String name = data.getStringExtra(EXTRA_SELECTED_COMPANY_NAME_KEY);
                String code = data.getStringExtra(EXTRA_SELECTED_COMPANY_CODE_KEY);
                MSLog.d("select favorite stock name: " + name + ", code: " + code);
                addFavoriteStock(code);
            }
        } else if(requestCode == sSettingRequestCode) {
            setAvatarImage();
        }
    }

    private void clearViewPager() {
        if(mViewPager != null) {
            mViewPager.clearOnPageChangeListeners();
            mViewPager.setAdapter(null);
            mViewPager = null;
        }
        if(mMagicIndicator != null) {
            mMagicIndicator.setNavigator(null);
            mMagicIndicator = null;
        }
    }

    private void addFavoriteStock(String code) {
        PostEvent.sendFavoriteStocksAdd(this, code);
        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        userProfile.addFavoriteStock(code);
        userProfile.notifyUserProfile(NOTIFY_ID_FAVORITE_LIST);
    }
}
