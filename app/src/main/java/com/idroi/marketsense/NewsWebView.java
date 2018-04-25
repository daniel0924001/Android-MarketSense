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

    public NewsWebView(Context context) {
        super(context);
        init();
    }


    public NewsWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init();
    }


    public NewsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init();
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
}
