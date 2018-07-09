package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.CommentFragment;

/**
 * Created by daniel.hsieh on 2018/7/5.
 */

public class DiscussionScreenSlidePagerAdapter extends BaseScreenSlidePagerAdapter {

    public DiscussionScreenSlidePagerAdapter(Context context, FragmentManager fm) {
        super(context, fm, new String[]{
                context.getResources().getString(R.string.title_discussion)
        });
    }

    @Override
    public Fragment getItem(int position) {
        return new CommentFragment();
    }
}
