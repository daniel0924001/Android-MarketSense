package com.idroi.marketsense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.adapter.StockScreenSlidePagerAdapter;
import com.idroi.marketsense.common.MarketSenseCommonNavigator;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;

/**
 * Created by daniel.hsieh on 2018/4/24.
 */

public class StockActivity extends AppCompatActivity {

    public final static String EXTRA_CODE = "com.idroi.marketsense.StockActivity.extra_code";

    private ViewPager mViewPager;
    private MagicIndicator mMagicIndicator;

    private String mStockName;
    private String mCode;

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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        if(savedInstanceState != null) {
            return;
        }

        setInformation();
        setActionBar();
        setViewPager();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }

    @Override
    protected void onDestroy() {
        clearViewPager();
        super.onDestroy();
    }

    private void setInformation() {
        mStockName = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        mCode = getIntent().getStringExtra(EXTRA_CODE);
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.main_action_bar, null);

            SimpleDraweeView imageView = view.findViewById(R.id.action_bar_avatar);
            if(imageView != null) {
                imageView.setImageResource(R.drawable.ic_keyboard_backspace_white_24px);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
            }

            TextView textView = view.findViewById(R.id.action_bar_name);
            if(textView != null) {
                String title = mStockName + " " + mCode;
                textView.setText(title);
            }

            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    private void setViewPager() {
        mViewPager = findViewById(R.id.pager);
        StockScreenSlidePagerAdapter stockScreenSlidePagerAdapter =
                new StockScreenSlidePagerAdapter(
                        this, getSupportFragmentManager(), mStockName, mCode);
        mViewPager.setAdapter(stockScreenSlidePagerAdapter);

        mMagicIndicator = (MagicIndicator) findViewById(R.id.tabs);
        MarketSenseCommonNavigator commonNavigator =
                new MarketSenseCommonNavigator(this, mViewPager,
                        stockScreenSlidePagerAdapter.getTitles());

        mViewPager.addOnPageChangeListener(mOnPageChangeListener);

        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mViewPager);
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

    public static Intent generateStockActivityIntent(Context context, String title, String code) {
        Intent intent = new Intent(context, StockActivity.class);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(EXTRA_CODE, code);
        return intent;
    }
}
