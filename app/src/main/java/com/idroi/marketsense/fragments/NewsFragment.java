package com.idroi.marketsense.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ethanhua.skeleton.RecyclerViewSkeletonScreen;
import com.ethanhua.skeleton.Skeleton;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.NewsWebViewActivity;
import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.request.NewsRequest;

import static com.idroi.marketsense.request.NewsRequest.PARAM_LEVEL;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public class NewsFragment extends Fragment {

    public final static String TASK_NAME = "TASK_NAME";
    public final static String KEYWORD_NAME = "KEYWORD_NAME";
    public final static int GENERAL_TASK_ID = 1;
    public final static int KEYWORD_TASK_ID = 2;

    public enum TASK {
        GENERAL(GENERAL_TASK_ID), KEYWORD(KEYWORD_TASK_ID);
        int taskId;
        TASK(int id) {
            taskId = id;
        }

        public int getTaskId() {
            return taskId;
        }
    }

    private RecyclerView mRecyclerView;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;
    private RecyclerViewSkeletonScreen mSkeletonScreen;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.news_fragment, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.news_recycler_view);

        mNewsRecyclerAdapter = new NewsRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mNewsRecyclerAdapter);

        mSkeletonScreen = Skeleton.bind(mRecyclerView)
                .adapter(mNewsRecyclerAdapter)
                .load(R.layout.layout_default_item_skeleton)
                .shimmer(false).show();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MSLog.i("Enter NewsFragment");

        mNewsRecyclerAdapter.setNewsAvailableListener(new NewsRecyclerAdapter.NewsAvailableListener() {
            @Override
            public void onNewsAvailable() {
                if(mSkeletonScreen != null) {
                    mSkeletonScreen.hide();
                }
            }
        });
        mNewsRecyclerAdapter.loadNews(generateURL());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    mNewsRecyclerAdapter.expand(7);
                }
            }
        });

        mNewsRecyclerAdapter.setOnItemClickListener(new NewsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(News news) {
                startActivity(NewsWebViewActivity.generateNewsWebViewActivityIntent(
                        getContext(), news.getTitle(), news.getUrlImage(), news.getDate(),
                        news.getPageLink(), news.getOriginLink()));
                getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
    }

    public String generateURL() {
        if(getArguments() == null) {
            return null;
        }

        int taskId = getArguments().getInt(TASK_NAME);
        switch (taskId) {
            case GENERAL_TASK_ID:
                return NewsRequest.queryNewsURL(
                        getArguments().getString(PARAM_STATUS),
                        getArguments().getInt(PARAM_LEVEL));
            case KEYWORD_TASK_ID:
                return NewsRequest.queryKeywordNewsURL(getArguments().getString(KEYWORD_NAME));
            default:
                return null;
        }
    }

    @Override
    public void onDestroyView() {
        MSLog.i("Exit NewsFragment");
        mNewsRecyclerAdapter.destroy();
        super.onDestroyView();
    }
}
