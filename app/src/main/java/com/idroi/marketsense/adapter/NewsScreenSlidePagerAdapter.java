package com.idroi.marketsense.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.NewsFragment;
import com.idroi.marketsense.fragments.StockListFragment;

import java.util.ArrayList;

import static com.idroi.marketsense.fragments.NewsFragment.TASK_NAME;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class NewsScreenSlidePagerAdapter extends BaseScreenSlidePagerAdapter {

    public NewsScreenSlidePagerAdapter(Context context, FragmentManager fm) {
        super(context, fm, new String[]{
                context.getResources().getString(R.string.title_news_win_predict),
                context.getResources().getString(R.string.title_news_lose_predict),
                context.getResources().getString(R.string.title_news_win_today),
                context.getResources().getString(R.string.title_news_lose_today),
                context.getResources().getString(R.string.title_news_optimistic),
                context.getResources().getString(R.string.title_news_pessimistic)
        });
    }

    @Override
    public Fragment getItem(int position) {
        // TODO: maybe there is a better way

        if(position >= 0 && position <=3) {
            StockListFragment stockListFragment = new StockListFragment();
            Bundle bundle = new Bundle();
            switch (position) {
                case 0:
                    bundle.putInt(TASK_NAME, StockListFragment.TASK.PREDICT_WIN.getTaskId());
                    break;
                case 1:
                    bundle.putInt(TASK_NAME, StockListFragment.TASK.PREDICT_LOSE.getTaskId());
                    break;
                case 2:
                    bundle.putInt(TASK_NAME, StockListFragment.TASK.ACTUAL_WIN.getTaskId());
                    break;
                case 3:
                    bundle.putInt(TASK_NAME, StockListFragment.TASK.ACTUAL_LOSE.getTaskId());
                    break;
            }
            stockListFragment.setArguments(bundle);
            return new StockListFragment();
        } else {
            return new NewsFragment();
        }
    }
}
