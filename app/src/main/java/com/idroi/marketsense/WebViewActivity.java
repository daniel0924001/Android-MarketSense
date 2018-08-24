package com.idroi.marketsense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.ViewSkeletonScreen;
import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.util.ActionBarHelper;

/**
 * Created by daniel.hsieh on 2018/5/28.
 */

public class WebViewActivity extends AppCompatActivity {

    private static final String EXTRA_ACTION_BAR_TITLE = "EXTRA_ACTION_BAR_TITLE";
    private static final String EXTRA_URL = "EXTRA_URL";
    private static final String EXTRA_ID = "EXTRA_ID";

    private String mActionBarTitle;
    private String mUrl;
    private int mId;
    private NewsWebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrescoHelper.initialize(getApplicationContext());
        setContentView(R.layout.activity_webview);

        setInformation();
        setActionBar();
        initWebView();
    }

    private void initWebView() {
        mWebView = findViewById(R.id.marketsense_webview);
        mWebView.setVerticalScrollBarEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);

        final ViewSkeletonScreen skeletonScreen =
                Skeleton.bind(mWebView).shimmer(false)
                        .load(R.layout.skeleton_webview).show();

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                skeletonScreen.hide();
                super.onPageFinished(view, url);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                MSLog.e(consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        mWebView.loadUrl(mUrl);
    }

    private void setInformation() {
        mActionBarTitle = getIntent().getStringExtra(EXTRA_ACTION_BAR_TITLE);
        mUrl = getIntent().getStringExtra(EXTRA_URL);
        mId = getIntent().getIntExtra(EXTRA_ID, -1);
    }

    private void setActionBar() {
        ActionBarHelper.setActionBarForSimpleTitleAndBack(this, mActionBarTitle);
    }

    @Override
    protected void onDestroy() {
        if(mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }

    public static Intent generateWebViewActivityIntent(Context context, int id, String actionBarTitle, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_ACTION_BAR_TITLE, actionBarTitle);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }
}
