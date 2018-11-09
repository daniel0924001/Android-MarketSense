package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.os.Handler;
import android.support.constraint.Group;
import android.view.View;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/10/4.
 */

public class LoadingDotsPageViewHolder {

    public View mainView;
    public View firstDot;
    public View secondDot;
    public View thirdDot;
    public View[] dotsView;

    private int startIndex = 1;
    private Handler mHandler;
    private Runnable mRefreshRunnable;

    static final LoadingDotsPageViewHolder EMPTY_VIEW_HOLDER = new LoadingDotsPageViewHolder();

    private LoadingDotsPageViewHolder() {}

    public static LoadingDotsPageViewHolder convertToViewHolder(final View view) {
        final LoadingDotsPageViewHolder viewHolder = new LoadingDotsPageViewHolder();
        try {
            viewHolder.mainView = view;
            viewHolder.firstDot = view.findViewById(R.id.first_dot);
            viewHolder.secondDot = view.findViewById(R.id.second_dot);
            viewHolder.thirdDot = view.findViewById(R.id.third_dot);
            viewHolder.dotsView = new View[] {
                    viewHolder.firstDot, viewHolder.secondDot, viewHolder.thirdDot};

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void start(final Context context) {
        mHandler = new Handler();
        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                dotsView[next(0)].setBackground(
                        context.getDrawable(R.drawable.top_banner_selector_red_first));
                dotsView[next(1)].setBackground(
                        context.getDrawable(R.drawable.top_banner_selector_red_third));
                dotsView[next(2)].setBackground(
                        context.getDrawable(R.drawable.top_banner_selector_red_second));
                startIndex = (startIndex + 1) % dotsView.length;
                mHandler.postDelayed(mRefreshRunnable, 250);
            }
        };
        mainView.setVisibility(View.VISIBLE);
        mHandler.post(mRefreshRunnable);
    }

    public void stopAndGone() {
        if(mRefreshRunnable != null) {
            mHandler.removeCallbacks(mRefreshRunnable);
        }
        mainView.setVisibility(View.GONE);
    }

    private int next(int step) {
        return (startIndex + step) % dotsView.length;
    }

}
