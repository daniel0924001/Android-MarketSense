package com.idroi.marketsense.adapter;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.NewsFragment;

import java.util.ArrayList;

import static com.idroi.marketsense.fragments.NewsFragment.TASK_NAME;
import static com.idroi.marketsense.request.NewsRequest.PARAM_GTS;
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
                context.getResources().getString(R.string.title_focus_news),
                context.getResources().getString(R.string.title_recent_news),
                context.getResources().getString(R.string.title_good_news),
                context.getResources().getString(R.string.title_bad_news)
        });
    }

    @Override
    public Fragment getItem(int position) {
        NewsFragment newsFragment = new NewsFragment();
        Bundle bundle = new Bundle();

        ArrayList<String> statusArrayList = new ArrayList<>();
        ArrayList<Integer> levelArrayList = new ArrayList<>();

        bundle.putInt(TASK_NAME, NewsFragment.TASK.GENERAL.getTaskId());
        long now = System.currentTimeMillis() / 1000;
        switch (position) {
            case 0:
                statusArrayList.add(PARAM_STATUS_RISING);
                statusArrayList.add(PARAM_STATUS_FALLING);
                levelArrayList.add(3);
                levelArrayList.add(-3);
                bundle.putString(PARAM_GTS, String.valueOf(now - 86400));
                break;
            case 1:
                statusArrayList.add(PARAM_STATUS_RISING);
                statusArrayList.add(PARAM_STATUS_FALLING);
                levelArrayList.add(0);
                levelArrayList.add(0);
                bundle.putString(PARAM_GTS, String.valueOf(now - 86400));
                break;
            case 2:
                statusArrayList.add(PARAM_STATUS_RISING);
                levelArrayList.add(1);
                bundle.putString(PARAM_GTS, String.valueOf(now - 3 * 86400));
                break;
            case 3:
                statusArrayList.add(PARAM_STATUS_FALLING);
                levelArrayList.add(-1);
                bundle.putString(PARAM_GTS, String.valueOf(now - 3 * 86400));
                break;
        }
        bundle.putStringArrayList(PARAM_STATUS, statusArrayList);
        bundle.putIntegerArrayList(PARAM_LEVEL,levelArrayList);
        newsFragment.setArguments(bundle);
        return newsFragment;
    }
}
