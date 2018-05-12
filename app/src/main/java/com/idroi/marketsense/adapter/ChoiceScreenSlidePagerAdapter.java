package com.idroi.marketsense.adapter;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.NewsFragment;
import com.idroi.marketsense.fragments.StockListFragment;

import static com.idroi.marketsense.fragments.StockListFragment.TASK_NAME;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class ChoiceScreenSlidePagerAdapter extends BaseScreenSlidePagerAdapter {

    public ChoiceScreenSlidePagerAdapter(Context context, FragmentManager fm) {
        super(context, fm, new String[] {
                context.getResources().getString(R.string.title_choices_list),
//                context.getResources().getString(R.string.title_choices_news)
        });
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            StockListFragment stockListFragment = new StockListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(TASK_NAME, StockListFragment.TASK.SELF_CHOICES.getTaskId());
            stockListFragment.setArguments(bundle);
            return stockListFragment;
        } else if(position == 1) {
            return new NewsFragment();
        }
        return null;
    }
}
