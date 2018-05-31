package com.idroi.marketsense;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.idroi.marketsense.Logging.MSLog;

/**
 * Created by daniel.hsieh on 2018/5/30.
 */

public class SwipeableViewPager extends ViewPager {

    private boolean mCanSwipeable;

    public SwipeableViewPager(@NonNull Context context) {
        super(context);
        mCanSwipeable = false;
    }

    public SwipeableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mCanSwipeable = false;
    }

    public void setSwipeable(boolean canSwipe) {
        mCanSwipeable = canSwipe;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mCanSwipeable && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mCanSwipeable && super.onTouchEvent(event);
    }
}
