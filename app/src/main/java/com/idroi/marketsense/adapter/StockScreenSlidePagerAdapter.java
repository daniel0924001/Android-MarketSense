package com.idroi.marketsense.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.NewsFragment;
import com.idroi.marketsense.fragments.StockFragment;

import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_NAME;
import static com.idroi.marketsense.fragments.NewsFragment.TASK_NAME;
import static com.idroi.marketsense.fragments.StockFragment.FALL_BUNDLE;
import static com.idroi.marketsense.fragments.StockFragment.RAISE_BUNDLE;
import static com.idroi.marketsense.fragments.StockFragment.STOCK_CODE;
import static com.idroi.marketsense.fragments.StockFragment.STOCK_NAME;

/**
 * Created by daniel.hsieh on 2018/4/24.
 */

public class StockScreenSlidePagerAdapter extends BaseScreenSlidePagerAdapter {

    private String mStockName;
    private String mCode;
    private int mRaiseNum, mFallNum;

    public StockScreenSlidePagerAdapter(Context context, FragmentManager fm,
                                        String stockName, String code, int raiseNum, int fallNum) {
        super(context, fm, new String[] {
                context.getResources().getString(R.string.title_stock_overview),
                context.getResources().getString(R.string.title_stock_related_news)
        });
        mStockName = stockName;
        mCode = code;
        mRaiseNum = raiseNum;
        mFallNum = fallNum;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            StockFragment stockFragment = new StockFragment();
            Bundle bundle = new Bundle();
            bundle.putString(STOCK_CODE, mCode);
            bundle.putString(STOCK_NAME, mStockName);
            bundle.putInt(RAISE_BUNDLE, mRaiseNum);
            bundle.putInt(FALL_BUNDLE, mFallNum);
            stockFragment.setArguments(bundle);
            return stockFragment;
        } else {
            NewsFragment newsFragment = new NewsFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(TASK_NAME, NewsFragment.TASK.KEYWORD.getTaskId());
            bundle.putString(KEYWORD_NAME, mStockName);
            newsFragment.setArguments(bundle);
            return newsFragment;
        }
    }
}
