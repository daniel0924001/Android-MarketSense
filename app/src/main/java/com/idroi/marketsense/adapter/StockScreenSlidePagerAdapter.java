package com.idroi.marketsense.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.NewsFragment;

import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_NAME;
import static com.idroi.marketsense.fragments.NewsFragment.TASK_NAME;

/**
 * Created by daniel.hsieh on 2018/4/24.
 */

public class StockScreenSlidePagerAdapter extends BaseScreenSlidePagerAdapter {

    private String mStockName;

    public StockScreenSlidePagerAdapter(Context context, FragmentManager fm, String stockName) {
        super(context, fm, new String[] {
                context.getResources().getString(R.string.title_stock_overview),
                context.getResources().getString(R.string.title_stock_related_news)
        });
        mStockName = stockName;
    }

    @Override
    public Fragment getItem(int position) {
        NewsFragment newsFragment = new NewsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TASK_NAME, NewsFragment.TASK.KEYWORD.getTaskId());
        bundle.putString(KEYWORD_NAME, mStockName);
        newsFragment.setArguments(bundle);
        return newsFragment;
    }
}
