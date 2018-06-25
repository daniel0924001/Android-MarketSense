package com.idroi.marketsense.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.MainFragment;
import com.idroi.marketsense.fragments.NewsFragment;

import java.util.ArrayList;

import static com.idroi.marketsense.fragments.NewsFragment.TASK_NAME;
import static com.idroi.marketsense.request.NewsRequest.PARAM_GTS;
import static com.idroi.marketsense.request.NewsRequest.PARAM_LEVEL;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS_FALLING;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS_RISING;

/**
 * Created by daniel.hsieh on 2018/6/25.
 */

public class MainPageScreenSlidePagerAdapter extends BaseScreenSlidePagerAdapter {

    public MainPageScreenSlidePagerAdapter(Context context, FragmentManager fm) {
        super(context, fm, new String[]{
                context.getResources().getString(R.string.title_main_page)
        });
    }

    @Override
    public Fragment getItem(int position) {

        MainFragment mainFragment = new MainFragment();
        Bundle bundle = new Bundle();

        ArrayList<String> statusArrayList = new ArrayList<>();
        ArrayList<Integer> levelArrayList = new ArrayList<>();
        long now = System.currentTimeMillis() / 1000;
        statusArrayList.add(PARAM_STATUS_RISING);
        statusArrayList.add(PARAM_STATUS_FALLING);
        levelArrayList.add(0);
        levelArrayList.add(0);

//        bundle.putInt(TASK_NAME, NewsFragment.TASK.GENERAL.getTaskId());
        bundle.putString(PARAM_GTS, String.valueOf(now - 86400));
        bundle.putStringArrayList(PARAM_STATUS, statusArrayList);
        bundle.putIntegerArrayList(PARAM_LEVEL,levelArrayList);
        mainFragment.setArguments(bundle);

        return mainFragment;
    }
}
