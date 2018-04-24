package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.NewsFragment;

import java.lang.ref.WeakReference;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public abstract class BaseScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    private final int NUM_PAGES;
    private String[] mTitles;

    public BaseScreenSlidePagerAdapter(Context context, FragmentManager fm, String[] titles) {
        super(fm);
        mTitles = titles;
        NUM_PAGES = mTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    public String[] getTitles() {
        return mTitles;
    }
}
