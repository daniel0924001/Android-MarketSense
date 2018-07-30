package com.idroi.marketsense.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.StockListFragment;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class PredictScreenSlidePagerAdapter extends BaseScreenSlidePagerAdapter {

    public PredictScreenSlidePagerAdapter(Context context, FragmentManager fm) {
        super(context, fm, new String[]{
                context.getResources().getString(R.string.title_news_win_predict),
                context.getResources().getString(R.string.title_choices_list),
        });
    }

    @Override
    public Fragment getItem(int position) {

        StockListFragment stockListFragment = new StockListFragment();
        Bundle bundle = new Bundle();
        switch (position) {
            case 0:
                bundle.putInt(StockListFragment.TASK_NAME, StockListFragment.TASK.MAIN.getTaskId());
                break;
            case 1:
                bundle.putInt(StockListFragment.TASK_NAME, StockListFragment.TASK.SELF_CHOICES.getTaskId());
                break;
        }
        stockListFragment.setArguments(bundle);
        return stockListFragment;
    }
}
