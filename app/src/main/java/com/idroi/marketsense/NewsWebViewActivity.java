package com.idroi.marketsense;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.ViewSkeletonScreen;
import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.FrescoImageHelper;

import java.io.ByteArrayInputStream;

/**
 * Created by daniel.hsieh on 2018/4/25.
 */

public class NewsWebViewActivity extends AppCompatActivity {

    private static final String UTM_PARAM_STRING = "utm_source=infohub&utm_medium=android_app&utm_campaign=news_click";
    private static final String PAGELINK_PARM_STRING = "user_name=infohub|android_app|news_click";
    private static final String PAGELINK_PARM_KEY = "config=";

    public static final String EXTRA_MIDDLE_TITLE = "EXTRA_MIDDLE_TITLE";
    public static final String EXTRA_MIDDLE_DATE = "EXTRA_MIDDLE_SOURCE_DATE";
    public static final String EXTRA_MIDDLE_IMAGE_URL = "EXTRA_MIDDLE_IMAGE_URL";
    public static final String EXTRA_MIDDLE_PAGE_URL = "EXTRA_MIDDLE_PAGE_URL";
    public static final String EXTRA_ORIGINAL_PAGE_URL = "EXTRA_ORIGINAL_PAGE_URL";

    private String mTitle, mImageUrl, mSourceDate;
    private String mMiddlePageUrl;
    private String mOriginalPageUrl;
    private String mPageLink;

    private View mImageMask;
    private ConstraintLayout mUpperBlock;
    private NewsWebView mNewsWebViewOriginal;
    private NewsWebView mNewsWebViewMiddle;
    private TextView mNewsWebViewMiddleTitleTextView;
    private TextView mNewsWebViewMiddleDateTextView;
    private SimpleDraweeView mNewsWebViewMiddleImageView;

    private Button mReadButton;

    public static final int sPostDelayMilliSeconds = 1200;
    private boolean mIsOriginalVisible = false;
    private boolean mTryToLoadOtherWebViewFlag = false;
    private Handler mHandler = new Handler();
    private Runnable mLoadOriginalWebViewRunnable = new Runnable() {
        @Override
        public void run() {
            if(!mTryToLoadOtherWebViewFlag) {
                mTryToLoadOtherWebViewFlag = true;
                if (mNewsWebViewOriginal != null) {
                    MSLog.i("Loading web page (original): " + mOriginalPageUrl);
                    mNewsWebViewOriginal.loadUrl(mOriginalPageUrl);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_webview);

        if(savedInstanceState != null) {
            return;
        }

        mReadButton = (Button) findViewById(R.id.btn_convert_webview);
        mReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeVisibility();
            }
        });

        setInformation();
        setActionBar();
        initUpperBlock();
        initWebView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }

    private void setInformation() {
        mMiddlePageUrl = getIntent().getStringExtra(EXTRA_MIDDLE_PAGE_URL);
        mOriginalPageUrl = getIntent().getStringExtra(EXTRA_ORIGINAL_PAGE_URL);
        mTitle = getIntent().getStringExtra(EXTRA_MIDDLE_TITLE);
        mImageUrl = getIntent().getStringExtra(EXTRA_MIDDLE_IMAGE_URL);
        mSourceDate = getIntent().getStringExtra(EXTRA_MIDDLE_DATE);
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

    private void initUpperBlock() {

        MSLog.i("title: " + mTitle + ", source: " + mSourceDate + ", image: " + mImageUrl);
        mUpperBlock = findViewById(R.id.marketsense_webview_upper_block);
        mImageMask = findViewById(R.id.marketsense_webview_activity_image_mask);
        mNewsWebViewMiddleTitleTextView = findViewById(R.id.marketsense_webview_activity_title);
        mNewsWebViewMiddleDateTextView = findViewById(R.id.marketsense_webview_activity_source_date);
        if(mNewsWebViewMiddleTitleTextView != null) {
            mNewsWebViewMiddleTitleTextView.setText(mTitle);
        }
        if(mNewsWebViewMiddleDateTextView != null) {
            mNewsWebViewMiddleDateTextView.setText(mSourceDate);
        }
        mNewsWebViewMiddleImageView = findViewById(R.id.marketsense_webview_activity_image);
        if(mNewsWebViewMiddleImageView != null && mImageUrl != null) {
            FrescoImageHelper.loadImageView(mImageUrl,
                    mNewsWebViewMiddleImageView, FrescoImageHelper.MAIN_IMAGE_RATIO);
            mImageMask.setVisibility(View.VISIBLE);
        }

        if(mImageUrl == null) {
            mNewsWebViewMiddleDateTextView.setTextColor(getResources().getColor(R.color.marketsense_text_black));
            mNewsWebViewMiddleTitleTextView.setTextColor(getResources().getColor(R.color.marketsense_text_black));
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {

        mNewsWebViewOriginal = (NewsWebView) findViewById(R.id.news_webview_original);
        mNewsWebViewOriginal.setVerticalScrollBarEnabled(true);
        mNewsWebViewOriginal.setHorizontalFadingEdgeEnabled(false);
        mNewsWebViewOriginal.getSettings().setBlockNetworkImage(true);

        mNewsWebViewMiddle = (NewsWebView) findViewById(R.id.news_webview_middle);
        mNewsWebViewMiddle.setVerticalScrollBarEnabled(true);
        mNewsWebViewMiddle.setHorizontalFadingEdgeEnabled(false);
        mNewsWebViewMiddle.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        mNewsWebViewMiddle.getSettings().setAllowFileAccess(true);
        mNewsWebViewMiddle.getSettings().setAppCacheEnabled(true);
        mNewsWebViewMiddle.getSettings().setBlockNetworkImage(true);

        final ViewSkeletonScreen skeletonScreen =
                Skeleton.bind(mNewsWebViewMiddle).shimmer(false)
                        .load(R.layout.skeleton_webview).show();

        mNewsWebViewMiddle.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        mNewsWebViewMiddle.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                skeletonScreen.hide();
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if(url.contains("adsbygoogle.js") || url.contains("ebay") || url.contains("amazon")) {
                    return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
                }
                return super.shouldInterceptRequest(view, url);
            }
        });

        mNewsWebViewMiddle.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress >= 80) {
                    mNewsWebViewMiddle.getSettings().setBlockNetworkImage(false);
                }

                if(newProgress >= 80 && !mTryToLoadOtherWebViewFlag) {
                    MSLog.w("GOOD!! start to load original web view");
                    mHandler.post(mLoadOriginalWebViewRunnable);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        mNewsWebViewOriginal.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        mNewsWebViewOriginal.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress >= 80) {
                    mNewsWebViewOriginal.getSettings().setBlockNetworkImage(false);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        String text = Base64.encodeToString(PAGELINK_PARM_STRING.getBytes(), Base64.DEFAULT);
        mPageLink = mMiddlePageUrl + '?' + UTM_PARAM_STRING + '&' + PAGELINK_PARM_KEY + text;

        MSLog.i("Loading web page (middle): " + mPageLink);
        mNewsWebViewMiddle.loadUrl(mPageLink);
        mHandler.postDelayed(mLoadOriginalWebViewRunnable, sPostDelayMilliSeconds);
    }

    public static Intent generateNewsWebViewActivityIntent(
            Context context, String title, String imageUrl, String sourceDate, String middleUrl, String originalUrl) {
        Intent intent = new Intent(context, NewsWebViewActivity.class);
        intent.putExtra(EXTRA_MIDDLE_TITLE, title);
        intent.putExtra(EXTRA_MIDDLE_DATE, sourceDate);
        intent.putExtra(EXTRA_MIDDLE_IMAGE_URL, imageUrl);
        intent.putExtra(EXTRA_MIDDLE_PAGE_URL, middleUrl);
        intent.putExtra(EXTRA_ORIGINAL_PAGE_URL, originalUrl);
        return intent;
    }

    private void changeVisibility() {
        if(mIsOriginalVisible) {
            // show our webview
            mIsOriginalVisible = false;
            mNewsWebViewMiddle.setVisibility(View.VISIBLE);
            mNewsWebViewOriginal.setVisibility(View.GONE);
            mUpperBlock.setVisibility(View.VISIBLE);
            mReadButton.setText(R.string.title_news_read_original);
        } else {
            // show original webview
            mIsOriginalVisible = true;
            mNewsWebViewMiddle.setVisibility(View.GONE);
            mNewsWebViewOriginal.setVisibility(View.VISIBLE);
            mUpperBlock.setVisibility(View.GONE);
            mReadButton.setText(R.string.title_news_read_middle);
        }
    }

    @Override
    protected void onDestroy() {
        if(mHandler != null) {
            mHandler.removeCallbacks(mLoadOriginalWebViewRunnable);
            mHandler = null;
        }
        if(mNewsWebViewMiddle != null) {
            mNewsWebViewMiddle.destroy();
            mNewsWebViewMiddle = null;
        }
        if(mNewsWebViewOriginal != null) {
            mNewsWebViewOriginal.destroy();
            mNewsWebViewOriginal = null;
        }
        super.onDestroy();
    }
}
