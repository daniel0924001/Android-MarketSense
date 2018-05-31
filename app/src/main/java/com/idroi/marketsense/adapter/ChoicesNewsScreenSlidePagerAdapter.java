package com.idroi.marketsense.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.NewsFragment;

import static com.idroi.marketsense.fragments.NewsFragment.TASK_NAME;
import static com.idroi.marketsense.request.NewsRequest.PARAM_LEVEL;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS_FALLING;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS_RISING;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class ChoicesNewsScreenSlidePagerAdapter extends BaseScreenSlidePagerAdapter {

    public ChoicesNewsScreenSlidePagerAdapter(Context context, FragmentManager fm) {
        super(context, fm, new String[]{
                context.getResources().getString(R.string.title_choices_news)
        });
    }

    @Override
    public Fragment getItem(int position) {
        NewsFragment newsFragment = new NewsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(NewsFragment.TASK_NAME, NewsFragment.TASK.KEYWORD_ARRAY.getTaskId());
        newsFragment.setArguments(bundle);
        return newsFragment;
    }
}
