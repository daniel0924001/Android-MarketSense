package com.idroi.marketsense.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ethanhua.skeleton.RecyclerViewSkeletonScreen;
import com.ethanhua.skeleton.Skeleton;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.NewsWebViewActivity;
import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.NewsRequest;

import java.util.ArrayList;

import static com.idroi.marketsense.adapter.NewsRecyclerAdapter.NEWS_SINGLE_LAYOUT;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_NEWS_READ_RECORD_LIST;
import static com.idroi.marketsense.request.NewsRequest.PARAM_GTS;
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
    public final static int KEYWORD_ARRAY_TASK_ID = 3;

    public enum TASK {
        GENERAL(GENERAL_TASK_ID),
        KEYWORD(KEYWORD_TASK_ID),
        KEYWORD_ARRAY(KEYWORD_ARRAY_TASK_ID);

        int taskId;
        TASK(int id) {
            taskId = id;
        }

        public int getTaskId() {
            return taskId;
        }
    }

    private RecyclerView mRecyclerView;
    private ImageView mNoDataImageView;
    private TextView mNoDataTextView;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;
    private RecyclerViewSkeletonScreen mSkeletonScreen;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mLoadingProgressBar;

    private int mTaskId;
    private UserProfile.GlobalBroadcastListener mGlobalBroadcastListener;
    private UserProfile.GlobalBroadcastListener mGlobalBroadcastListener2;

    private ConstraintLayout mNoDataRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.news_fragment, container, false);
        mRecyclerView = view.findViewById(R.id.news_recycler_view);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_to_refresh);
        mNoDataImageView = view.findViewById(R.id.no_news_iv);
        mNoDataRefreshLayout = view.findViewById(R.id.no_data_block);
        if(mTaskId == KEYWORD_ARRAY_TASK_ID) {
            mNoDataImageView.setImageResource(R.drawable.baseline_playlist_add_24px);
        }
        mNoDataTextView = view.findViewById(R.id.no_news_tv);

        if(getArguments() != null) {
            mTaskId = getArguments().getInt(TASK_NAME);
        } else {
            mTaskId = GENERAL_TASK_ID; // default
        }

        mNewsRecyclerAdapter = new NewsRecyclerAdapter(getActivity(), mTaskId, getArguments());
        mNewsRecyclerAdapter.setNewsLayoutType(NEWS_SINGLE_LAYOUT);
        mRecyclerView.setAdapter(mNewsRecyclerAdapter);

        mLoadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        mSkeletonScreen = Skeleton.bind(mRecyclerView)
                .adapter(mNewsRecyclerAdapter)
                .load(R.layout.skeleton_news_list)
                .shimmer(false).show();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MSLog.i("Enter NewsFragment");

        if(mTaskId == KEYWORD_ARRAY_TASK_ID) {
            mGlobalBroadcastListener = new UserProfile.GlobalBroadcastListener() {
                @Override
                public void onGlobalBroadcast(int notifyId, Object payload) {
                    if(notifyId == NOTIFY_ID_FAVORITE_LIST) {
                        ArrayList<String> urls = generateURL(true);
                        MSLog.d("onUserProfileChange in NewsFragment");
                        if(urls == null) {
                            mNewsRecyclerAdapter.clearNews();
                        } else {
                            mNewsRecyclerAdapter.loadNews(urls, generateURL(false));
                        }
                    }
                }
            };
            ClientData.getInstance().getUserProfile()
                    .addGlobalBroadcastListener(mGlobalBroadcastListener);
        }

        mNewsRecyclerAdapter.setNewsAvailableListener(new NewsRecyclerAdapter.NewsAvailableListener() {
            @Override
            public void onNewsAvailable() {
                if(mSkeletonScreen != null) {
                    mSkeletonScreen.hide();
                }
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                }
                if(mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                setVisibilityForEmptyData(false);
            }

            @Override
            public void onNewsEmpty() {
                if(mSkeletonScreen != null) {
                    mSkeletonScreen.hide();
                }
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                }
                if(mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                setVisibilityForEmptyData(true);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mNewsRecyclerAdapter.loadNews(generateURL(true), generateURL(false));
            }
        });

        ArrayList<String> urls = generateURL(true);
        if(urls != null) {
            mNewsRecyclerAdapter.loadNews(urls, generateURL(false));
        } else {
            setVisibilityForEmptyData(true);
            if(mLoadingProgressBar != null) {
                mLoadingProgressBar.setVisibility(View.GONE);
            }
        }

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
                mNewsRecyclerAdapter.notifyNewsIsClicked(news);
                startActivity(NewsWebViewActivity.generateNewsWebViewActivityIntent(
                        getContext(), news.getId(), news.getTitle(),
                        news.getUrlImage(), news.getDate(),
                        news.getPageLink(), news.getOriginLink(),
                        news.getVoteRaiseNum(), news.getVoteFallNum(),
                        news.getStockKeywords(), news.getExplicitKeywords(), news.getLevel()));
                getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });

        mNoDataRefreshLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> urls = generateURL(true);
                if(urls != null) {
                    mNewsRecyclerAdapter.loadNews(urls, generateURL(false));
                } else {
                    return;
                }

                if(mSkeletonScreen != null) {
                    mSkeletonScreen.show();
                }
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                }
                setVisibilityForEmptyData(false);
            }
        });

        mGlobalBroadcastListener2 = new UserProfile.GlobalBroadcastListener() {
            @Override
            public void onGlobalBroadcast(int notifyId, Object payload) {
                if(notifyId == NOTIFY_ID_NEWS_READ_RECORD_LIST) {
                    MSLog.d("update user's read news records");
                    mNewsRecyclerAdapter.notifyItemRangeChanged(0, mNewsRecyclerAdapter.getItemCount());
                }
            }
        };

        UserProfile userProfile = ClientData.getInstance(getContext()).getUserProfile();
        if(userProfile != null) {
            userProfile.addGlobalBroadcastListener(mGlobalBroadcastListener2);
        }
    }

    public ArrayList<String> generateURL(boolean isNetworkUrl) {
        if(getArguments() == null) {
            return null;
        }

        ArrayList<String> results = new ArrayList<>();
        switch (mTaskId) {
            case GENERAL_TASK_ID:
                ArrayList<String> statusArrayList = getArguments().getStringArrayList(PARAM_STATUS);
                ArrayList<Integer> levelArrayList = getArguments().getIntegerArrayList(PARAM_LEVEL);
                String gts = getArguments().getString(PARAM_GTS);
                if(statusArrayList == null || levelArrayList == null || statusArrayList.size() != levelArrayList.size()) {
                    MSLog.e("size of statusArrayList and levelArrayList is not equal.");
                    return null;
                }

                for(int i = 0; i < statusArrayList.size(); i++) {
                    String temp = NewsRequest.queryNewsUrl(
                            getContext(),
                            statusArrayList.get(i),
                            levelArrayList.get(i),
                            isNetworkUrl,
                            gts);
                    results.add(temp);
                }
                break;
            case KEYWORD_TASK_ID:
                results.add(NewsRequest.queryKeywordNewsUrl(
                        getContext(),
                        getArguments().getString(KEYWORD_NAME),
                        isNetworkUrl));
                break;
            case KEYWORD_ARRAY_TASK_ID:
                String keywordsUrl = NewsRequest.queryKeywordArrayNewsUrl(getContext(), isNetworkUrl);
                if(keywordsUrl == null) {
                    return null;
                }
                results.add(keywordsUrl);
                break;
            default:
                return null;
        }
        return results;
    }

    @Override
    public void onDestroyView() {
        MSLog.i("Exit NewsFragment");
        mNewsRecyclerAdapter.destroy();
        UserProfile userProfile = ClientData.getInstance(getContext()).getUserProfile();
        if(mTaskId == KEYWORD_ARRAY_TASK_ID) {
            userProfile.deleteGlobalBroadcastListener(mGlobalBroadcastListener);
        }
        userProfile.deleteGlobalBroadcastListener(mGlobalBroadcastListener2);
        super.onDestroyView();
    }

    private void setVisibilityForEmptyData(boolean isEmpty) {
        if(isEmpty) {
            mSwipeRefreshLayout.setVisibility(View.GONE);
            mNoDataRefreshLayout.setVisibility(View.VISIBLE);
            if(isSelfNoneChoices()) {
                mNoDataTextView.setText(R.string.add_first_stock);
                mNoDataImageView.setImageResource(R.drawable.baseline_playlist_add_24px);
            } else {
                mNoDataTextView.setText(R.string.ops_something_wrong);
                mNoDataImageView.setImageResource(R.drawable.baseline_sentiment_dissatisfied_24px);
            }
        } else {
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            mNoDataRefreshLayout.setVisibility(View.GONE);
        }
    }

    private boolean isSelfNoneChoices() {
        return mTaskId == KEYWORD_ARRAY_TASK_ID && ClientData.getInstance().getUserProfile().isEmptyFavoriteStock();
    }
}
