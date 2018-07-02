package com.idroi.marketsense;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Created by daniel.hsieh on 2018/4/25.
 */

public class NewsWebView extends WebView {

    public interface OnReachMaxHeightListener {
        void onReachMaxHeight();
    }

    private int mMaxHeight = -1;
    private OnReachMaxHeightListener mOnReachMaxHeightListener;
    private boolean mIsReachMaxHeight = false;

    public NewsWebView(Context context) {
        super(context);
        init();
    }


    public NewsWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    public NewsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setOnReachMaxHeightListener(OnReachMaxHeightListener listener) {
        mOnReachMaxHeightListener = listener;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {

        getSettings().setJavaScriptEnabled(true);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setUseWideViewPort(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setDisplayZoomControls(false);

    }

    @Override
    public void destroy() {
        stopLoading();
        getSettings().setJavaScriptEnabled(false);
        clearCache(false);
        clearFormData();
        clearMatches();
        clearSslPreferences();
        clearDisappearingChildren();
        clearHistory();
        clearAnimation();
        loadUrl("about:blank");
        setTag(null);
        if(getParent() != null) {
            ((ViewGroup) getParent()).removeAllViews();
        }
        removeAllViews();
        super.destroy();
    }

    public void setMaxHeight(int height) {
        mMaxHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(mMaxHeight > -1 && getMeasuredHeight() > mMaxHeight) {
            setMeasuredDimension(getMeasuredWidth(), mMaxHeight);
            if(mOnReachMaxHeightListener != null && !mIsReachMaxHeight) {
                mIsReachMaxHeight = true;
                mOnReachMaxHeightListener.onReachMaxHeight();
            }
        }
    }
}
