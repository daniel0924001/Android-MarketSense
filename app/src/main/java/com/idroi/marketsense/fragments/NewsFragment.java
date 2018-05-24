package com.idroi.marketsense.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ethanhua.skeleton.RecyclerViewSkeletonScreen;
import com.ethanhua.skeleton.Skeleton;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.NewsWebViewActivity;
import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.SharedPreferencesCompat;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.NewsRequest;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_REQUEST_NAME;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
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

    private String mNetworkUrl;
    private int mTaskId;
    private UserProfile.UserProfileChangeListener mUserProfileChangeListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.news_fragment, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.news_recycler_view);
        mNoDataImageView = view.findViewById(R.id.no_news_iv);
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

        if(mTaskId == KEYWORD_ARRAY_TASK_ID) {
            mUserProfileChangeListener = new UserProfile.UserProfileChangeListener() {
                @Override
                public void onUserProfileChange(int notifyId) {
                    if(notifyId == NOTIFY_ID_FAVORITE_LIST) {
                        String url = generateURL();
                        MSLog.d("onUserProfileChange in NewsFragment: " + url);
                        if(url == null) {
                            mNewsRecyclerAdapter.clearNews();
                        } else {
                            mNetworkUrl = generateURL(true);
                            mNewsRecyclerAdapter.loadNews(mNetworkUrl, generateURL(false));
                        }
                    }
                }
            };
            ClientData.getInstance().getUserProfile()
                    .addUserProfileChangeListener(mUserProfileChangeListener);
        }

        mNewsRecyclerAdapter.setNewsAvailableListener(new NewsRecyclerAdapter.NewsAvailableListener() {
            @Override
            public void onNewsAvailable() {
                if(mSkeletonScreen != null) {
                    mSkeletonScreen.hide();
                }
                setVisibilityForEmptyData(false);
            }

            @Override
            public void onNewsEmpty() {
                if(mSkeletonScreen != null) {
                    mSkeletonScreen.hide();
                }
                setVisibilityForEmptyData(true);
            }
        });

        String url = generateURL();
        if(url != null) {
            mNetworkUrl = generateURL(true);
            mNewsRecyclerAdapter.loadNews(mNetworkUrl, generateURL(false));
        } else {
            setVisibilityForEmptyData(true);
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
                startActivity(NewsWebViewActivity.generateNewsWebViewActivityIntent(
                        getContext(), news.getId(), news.getTitle(),
                        news.getUrlImage(), news.getDate(),
                        news.getPageLink(), news.getOriginLink(),
                        news.getVoteRaiseNum(), news.getVoteFallNum()));
                getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
    }

    public String generateURL() {
        return generateURL(true);
    }

    public String generateURL(boolean isNetworkUrl) {
        if(getArguments() == null) {
            return null;
        }

        switch (mTaskId) {
            case GENERAL_TASK_ID:
                return NewsRequest.queryNewsUrl(
                        getContext(),
                        getArguments().getString(PARAM_STATUS),
                        getArguments().getInt(PARAM_LEVEL),
                        isNetworkUrl);
            case KEYWORD_TASK_ID:
                return NewsRequest.queryKeywordNewsUrl(
                        getContext(),
                        getArguments().getString(KEYWORD_NAME),
                        isNetworkUrl);
            case KEYWORD_ARRAY_TASK_ID:
                return NewsRequest.queryKeywordArrayNewsUrl(getContext(), isNetworkUrl);
            default:
                return null;
        }
    }

    @Override
    public void onDestroyView() {
        MSLog.i("Exit NewsFragment");
        mNewsRecyclerAdapter.destroy();
        if(mTaskId == KEYWORD_ARRAY_TASK_ID) {
            ClientData.getInstance().getUserProfile()
                    .deleteUserProfileChangeListener(mUserProfileChangeListener);
        }
        super.onDestroyView();
    }

    private void setVisibilityForEmptyData(boolean isEmpty) {
        if(isEmpty) {
            mRecyclerView.setVisibility(View.GONE);
            mNoDataTextView.setVisibility(View.VISIBLE);
            mNoDataImageView.setVisibility(View.VISIBLE);
            if(isSelfNoneChoices()) {
                mNoDataTextView.setText(R.string.add_first_stock);
                mNoDataImageView.setImageResource(R.drawable.baseline_playlist_add_24px);
            } else {
                mNoDataTextView.setText(R.string.ops_something_wrong);
                mNoDataImageView.setImageResource(R.drawable.baseline_sentiment_dissatisfied_24px);
            }
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mNoDataTextView.setVisibility(View.GONE);
            mNoDataImageView.setVisibility(View.GONE);
        }
    }

    private boolean isSelfNoneChoices() {
        return mTaskId == KEYWORD_ARRAY_TASK_ID && ClientData.getInstance().getUserProfile().isEmptyFavoriteStock();
    }
}
