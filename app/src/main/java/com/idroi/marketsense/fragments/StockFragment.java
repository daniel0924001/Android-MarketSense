package com.idroi.marketsense.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.ViewSkeletonScreen;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.NewsWebView;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.PostEvent;

import java.util.Locale;

/**
 * Created by daniel.hsieh on 2018/5/6.
 */

public class StockFragment extends Fragment {

    public final static String STOCK_CODE = "STOCK_CODE";
    private final static String STOCK_REAL_TIME_URL_PREFIX = "https://so.cnyes.com/JavascriptGraphic/chartstudy.aspx?country=tw&market=twreal&divwidth=%d&divheight=%d&code=%s";

    private NewsWebView mStockPriceRealTimeWebView;
    private ViewSkeletonScreen mSkeletonScreen;
    private String mStockId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.stock_fragment, container, false);

        initRealTimeWebView(view);
        initVotingBtn(view);

        mSkeletonScreen = Skeleton.bind(mStockPriceRealTimeWebView)
                .shimmer(false)
                .load(R.layout.skeleton_webview)
                .show();

        return view;
    }

    private void initRealTimeWebView(View view) {

        mStockId = getArguments().getString(STOCK_CODE);

        mStockPriceRealTimeWebView = view.findViewById(R.id.stock_real_time_webview);
        mStockPriceRealTimeWebView.setVerticalScrollBarEnabled(true);
        mStockPriceRealTimeWebView.setHorizontalFadingEdgeEnabled(true);

        mStockPriceRealTimeWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mSkeletonScreen.hide();
                super.onPageFinished(view, url);
            }
        });

        String url =
                getStockPriceURL(mStockId);
        MSLog.i("Load the real time price of " + mStockId + ": " + url);
        mStockPriceRealTimeWebView.getSettings().setLoadWithOverviewMode(false);
        mStockPriceRealTimeWebView.getSettings().setUseWideViewPort(false);

        mStockPriceRealTimeWebView.loadUrl(url);
    }

    private String getStockPriceURL(String code) {
        int width = ClientData.getInstance().getScreenWidth();
        int height = (int)((float)(width * 2)/3);
        return String.format(Locale.US, STOCK_REAL_TIME_URL_PREFIX, width, height, code);
    }

    private void initVotingBtn(View view) {
        final Button buttonRaise = view.findViewById(R.id.btn_say_good);
        final Button buttonFall = view.findViewById(R.id.btn_say_bad);

        buttonRaise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MSLog.e("click good in company: " + mStockId);
                PostEvent.sendStockVote(getContext(), mStockId, PostEvent.Event.VOTE_RAISE.getEventName(), 1);
                buttonRaise.setEnabled(false);
                buttonRaise.setAlpha(0.5f);
                buttonFall.setEnabled(true);
                buttonFall.setAlpha(1);
            }
        });

        buttonFall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MSLog.e("click bad in company: " + mStockId);
                PostEvent.sendStockVote(getContext(), mStockId, PostEvent.Event.VOTE_FALL.getEventName(), 1);
                buttonRaise.setEnabled(true);
                buttonRaise.setAlpha(1);
                buttonFall.setEnabled(false);
                buttonFall.setAlpha(0.5f);
            }
        });
    }


    @Override
    public void onDestroy() {
        if(mStockPriceRealTimeWebView != null) {
            mStockPriceRealTimeWebView.destroy();
            mStockPriceRealTimeWebView = null;
        }
        super.onDestroy();
    }
}
