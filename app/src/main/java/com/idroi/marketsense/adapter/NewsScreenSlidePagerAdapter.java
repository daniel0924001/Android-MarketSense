package com.idroi.marketsense.adapter;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.Fragment;

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

public class NewsScreenSlidePagerAdapter extends BaseScreenSlidePagerAdapter {

    public NewsScreenSlidePagerAdapter(Context context, FragmentManager fm) {
        super(context, fm, new String[]{
                context.getResources().getString(R.string.title_news_best_optimistic),
                context.getResources().getString(R.string.title_news_best_pessimistic),
                context.getResources().getString(R.string.title_news_optimistic),
                context.getResources().getString(R.string.title_news_pessimistic)
        });
    }

    @Override
    public Fragment getItem(int position) {
        NewsFragment newsFragment = new NewsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TASK_NAME, NewsFragment.TASK.GENERAL.getTaskId());
        switch (position) {
            case 0:
                bundle.putString(PARAM_STATUS, PARAM_STATUS_RISING);
                bundle.putInt(PARAM_LEVEL, 3);
                break;
            case 1:
                bundle.putString(PARAM_STATUS, PARAM_STATUS_FALLING);
                bundle.putInt(PARAM_LEVEL, -3);
                break;
            case 2:
                bundle.putString(PARAM_STATUS, PARAM_STATUS_RISING);
                bundle.putInt(PARAM_LEVEL, 1);
                break;
            case 3:
                bundle.putString(PARAM_STATUS, PARAM_STATUS_FALLING);
                bundle.putInt(PARAM_LEVEL, -1);
                break;
        }
        newsFragment.setArguments(bundle);
        return newsFragment;
    }
}
