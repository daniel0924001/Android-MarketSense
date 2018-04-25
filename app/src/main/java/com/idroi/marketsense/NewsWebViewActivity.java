package com.idroi.marketsense;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;

/**
 * Created by daniel.hsieh on 2018/4/25.
 */

public class NewsWebViewActivity extends AppCompatActivity {

    public static final String EXTRA_MIDDLE_PAGE_URL = "EXTRA_MIDDLE_PAGE_URL";
    public static final String EXTRA_ORIGINAL_PAGE_URL = "EXTRA_ORIGINAL_PAGE_URL";

    private String mMiddlePageUrl;
    private String mOriginalPageUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_webview);

        if(savedInstanceState != null) {
            return;
        }

        setInformation();
        setActionBar();
        setWebView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }

    private void setInformation() {
        mMiddlePageUrl = getIntent().getStringExtra(EXTRA_MIDDLE_PAGE_URL);
        mOriginalPageUrl = getIntent().getStringExtra(EXTRA_ORIGINAL_PAGE_URL);
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.main_action_bar, null);

            ImageView imageView = view.findViewById(R.id.action_bar_avatar);
            if(imageView != null) {
                imageView.setImageResource(R.drawable.ic_keyboard_backspace_white_24px);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
            }

            TextView textView = view.findViewById(R.id.action_bar_name);
            if(textView != null) {
                textView.setText(getResources().getText(R.string.activity_news_web_name));
            }

            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void setWebView() {
        WebView webView = (WebView) findViewById(R.id.news_webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        MSLog.i("Loading web page: " + mOriginalPageUrl);
        webView.loadUrl(mOriginalPageUrl);
    }

    public static Intent generateNewsWebViewActivityIntent(
            Context context, String middleUrl, String originalUrl) {
        Intent intent = new Intent(context, NewsWebViewActivity.class);
        intent.putExtra(EXTRA_MIDDLE_PAGE_URL, middleUrl);
        intent.putExtra(EXTRA_ORIGINAL_PAGE_URL, originalUrl);
        return intent;
    }
}
