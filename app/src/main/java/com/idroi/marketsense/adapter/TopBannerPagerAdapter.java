package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.idroi.marketsense.Logging.MSLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel.hsieh on 2018/10/1.
 */

public class TopBannerPagerAdapter extends PagerAdapter {

    public interface TopBannerClickListener {
        void onItemClick(int position);
    }

    private List<View> mTopBannerImageView;
    private TopBannerClickListener mTopBannerClickListener;

    public TopBannerPagerAdapter(List<View> topBannerImageView,
                                 TopBannerClickListener topBannerClickListener) {
        mTopBannerImageView = topBannerImageView;
        mTopBannerClickListener = topBannerClickListener;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
        View view = (View) object;
        view.setOnClickListener(null);
        container.removeView(view);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View view = mTopBannerImageView.get(position);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTopBannerClickListener != null) {
                    mTopBannerClickListener.onItemClick(position);
                }
            }
        });
        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return mTopBannerImageView.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
