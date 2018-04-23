package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.NewsFragment;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class NewsScreenSlidePagerAdapter extends BaseScreenSlidePagerAdapter {

    public NewsScreenSlidePagerAdapter(Context context, FragmentManager fm) {
        super(context, fm, new String[]{
                context.getResources().getString(R.string.title_news_win),
                context.getResources().getString(R.string.title_news_lose),
                context.getResources().getString(R.string.title_news_optimistic),
                context.getResources().getString(R.string.title_news_pessimistic)
        });
    }

    @Override
    public Fragment getItem(int position) {
        return new NewsFragment();
    }
}
