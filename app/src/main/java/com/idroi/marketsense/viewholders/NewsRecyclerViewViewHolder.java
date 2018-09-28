package com.idroi.marketsense.viewholders;

import android.app.Activity;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.NewsWebViewActivity;
import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.request.NewsRequest;

import java.util.ArrayList;

import static com.idroi.marketsense.adapter.NewsRecyclerAdapter.NEWS_SINGLE_LAYOUT;
import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_NAME;
import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_TASK_ID;

/**
 * Created by daniel.hsieh on 2018/9/28.
 */

public class NewsRecyclerViewViewHolder {

    public RecyclerView newsRecyclerView;
    public NewsRecyclerAdapter newsRecyclerAdapter;
    public ProgressBar progressBar;
    public Group newsGroup;

    static final NewsRecyclerViewViewHolder EMPTY_VIEW_HOLDER = new NewsRecyclerViewViewHolder();

    private NewsRecyclerViewViewHolder() {}

    public static NewsRecyclerViewViewHolder convertToViewHolder(final View view) {
        final NewsRecyclerViewViewHolder viewHolder = new NewsRecyclerViewViewHolder();
        try {
            viewHolder.progressBar = view.findViewById(R.id.news_list_progress_bar);
            viewHolder.newsRecyclerView = view.findViewById(R.id.news_recycler_view);
            viewHolder.newsGroup = view.findViewById(R.id.news_group);

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void update(final Activity activity, NestedScrollView nestedScrollView, String keywords) {
        Bundle bundle = new Bundle();
        bundle.putString(KEYWORD_NAME, keywords);

        newsRecyclerAdapter = new NewsRecyclerAdapter(activity, KEYWORD_TASK_ID, bundle);
        newsRecyclerAdapter.setNewsLayoutType(NEWS_SINGLE_LAYOUT);
        newsRecyclerView.setNestedScrollingEnabled(false);
        newsRecyclerView.setAdapter(newsRecyclerAdapter);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        newsRecyclerAdapter.setNewsAvailableListener(new NewsRecyclerAdapter.NewsAvailableListener() {
            @Override
            public void onNewsAvailable() {
                if(progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                newsGroup.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNewsEmpty() {
                if(progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                newsGroup.setVisibility(View.GONE);
            }
        });

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(v.getChildAt(v.getChildCount() - 1) != null) {
                    if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                            scrollY > oldScrollY) {
                        newsRecyclerAdapter.expand(7);
                    }
                }
            }
        });

        newsRecyclerAdapter.setOnItemClickListener(new NewsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(News news) {
                newsRecyclerAdapter.notifyNewsIsClicked(news);
                activity.startActivity(NewsWebViewActivity.generateNewsWebViewActivityIntent(
                        activity, news.getId(), news.getTitle(),
                        news.getUrlImage(), news.getDate(),
                        news.getPageLink(), news.getOriginLink(),
                        news.getVoteRaiseNum(), news.getVoteFallNum(),
                        news.getStockKeywords(), news.getExplicitKeywords(), news.getLevel()));
                activity.overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });

        ArrayList<String> networkUrls = new ArrayList<>();
        ArrayList<String> cacheUrls = new ArrayList<>();
        networkUrls.add(NewsRequest.queryKeywordNewsUrl(activity, keywords, true));
        cacheUrls.add(NewsRequest.queryKeywordNewsUrl(activity, keywords, false));

        newsRecyclerAdapter.loadNews(networkUrls, cacheUrls);
    }

    public void destroy() {
        if(newsRecyclerAdapter != null) {
            newsRecyclerAdapter.destroy();
        }
    }
}
