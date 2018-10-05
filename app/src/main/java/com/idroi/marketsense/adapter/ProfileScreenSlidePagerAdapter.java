package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.idroi.marketsense.R;
import com.idroi.marketsense.fragments.ProfileFragment;

/**
 * Created by daniel.hsieh on 2018/8/23.
 */

public class ProfileScreenSlidePagerAdapter extends BaseScreenSlidePagerAdapter {

    public ProfileScreenSlidePagerAdapter(Context context, FragmentManager fm) {
        super(context, fm, new String[]{
                context.getResources().getString(R.string.title_profile)
        });
    }

    @Override
    public Fragment getItem(int position) {
        return new ProfileFragment();
    }
}
