package com.idroi.marketsense.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.idroi.marketsense.NewsWebViewActivity;
import com.idroi.marketsense.R;
import com.idroi.marketsense.StockActivity;
import com.idroi.marketsense.WrapGridView;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.adapter.RankingGridViewAdapter;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.datasource.StockListPlacer;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.request.StockRequest;

import java.util.ArrayList;

import static com.idroi.marketsense.adapter.NewsRecyclerAdapter.NEWS_SINGLE_LAYOUT;
import static com.idroi.marketsense.fragments.NewsFragment.GENERAL_TASK_ID;
import static com.idroi.marketsense.request.NewsRequest.PARAM_GTS;
import static com.idroi.marketsense.request.NewsRequest.PARAM_LEVEL;
import static com.idroi.marketsense.request.NewsRequest.PARAM_STATUS;

/**
 * Created by daniel.hsieh on 2018/6/25.
 */

public class MainFragment extends Fragment {

    public interface OnActionBarChangeListener {
        void onActionBarChange(String title, boolean canReturn);
    }

    private RecyclerView mNewsRecyclerView;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;

    private ConstraintLayout mNoDataRefreshLayout;
    private ImageView mNoDataImageView;
    private TextView mNoDataTextView;
    private ProgressBar mLoadingProgressBar, mLoadingProgressBarRankingPeople, mLoadingProgressBarRankingNews;

    private WrapGridView mPeopleGridView, mNewsGridView;
    private RankingGridViewAdapter mPeopleGridViewAdapter, mNewsGridViewAdapter;

    private StockListPlacer mStockListPlacer;

    private NestedScrollView mNestedScrollView;
    private Fragment mStockListFragment;
    private OnActionBarChangeListener mOnActionBarChangeListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.main_fragment, container, false);

        mNoDataImageView = view.findViewById(R.id.no_news_iv);
        mNoDataTextView = view.findViewById(R.id.no_news_tv);
        mNoDataRefreshLayout = view.findViewById(R.id.no_data_block);

        mNewsRecyclerView = view.findViewById(R.id.news_recycler_view);
        mNewsRecyclerAdapter = new NewsRecyclerAdapter(getActivity(), GENERAL_TASK_ID, getArguments());
        mNewsRecyclerAdapter.setNewsLayoutType(NEWS_SINGLE_LAYOUT);
        mNewsRecyclerView.setNestedScrollingEnabled(false);
        mNewsRecyclerView.setAdapter(mNewsRecyclerAdapter);
        mNewsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mLoadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        mLoadingProgressBarRankingNews = view.findViewById(R.id.loading_progress_bar_rank_news);
        mLoadingProgressBarRankingPeople = view.findViewById(R.id.loading_progress_bar_rank_people);

        mPeopleGridView = view.findViewById(R.id.grid_ranking_people);
        mNewsGridView = view.findViewById(R.id.grid_ranking_news);

        mNestedScrollView = view.findViewById(R.id.body_scroll_view);

        initTopBanner(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStockListPlacer = new StockListPlacer(getActivity());
        mStockListPlacer.setStockListListener(new StockListPlacer.StockListListener() {
            @Override
            public void onStockListLoaded() {
                if(mLoadingProgressBarRankingNews != null) {
                    mLoadingProgressBarRankingNews.setVisibility(View.GONE);
                }
                if(mLoadingProgressBarRankingPeople != null) {
                    mLoadingProgressBarRankingPeople.setVisibility(View.GONE);
                }
                TextView secondTitle = view.findViewById(R.id.tv_news_ranking);
                ((ConstraintLayout.LayoutParams) secondTitle.getLayoutParams()).topToBottom = R.id.grid_ranking_people;
                TextView thirdTitle = view.findViewById(R.id.tv_news);
                ((ConstraintLayout.LayoutParams) thirdTitle.getLayoutParams()).topToBottom = R.id.grid_ranking_news;

                mPeopleGridViewAdapter = new RankingGridViewAdapter(getActivity(), mStockListPlacer.getStocks(), RankingGridViewAdapter.RANKING_BY_PEOPLE);
                mNewsGridViewAdapter = new RankingGridViewAdapter(getActivity(), mStockListPlacer.getStocks(), RankingGridViewAdapter.RANKING_BY_NEWS);

                mPeopleGridView.setAdapter(mPeopleGridViewAdapter);
                mNewsGridView.setAdapter(mNewsGridViewAdapter);

                mPeopleGridViewAdapter.setOnItemClickListener(new RankingGridViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Stock stock) {
                        openStockActivity(stock);
                    }
                });

                mNewsGridViewAdapter.setOnItemClickListener(new RankingGridViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Stock stock) {
                        openStockActivity(stock);
                    }
                });
            }
        });
        mStockListPlacer.loadStockList(
                StockRequest.queryStockList(getActivity(), true),
                StockRequest.queryStockList(getActivity(), false));

        mNewsRecyclerAdapter.setNewsAvailableListener(new NewsRecyclerAdapter.NewsAvailableListener() {
            @Override
            public void onNewsAvailable() {
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                }
                setVisibilityForEmptyData(false);
            }

            @Override
            public void onNewsEmpty() {
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                }
                setVisibilityForEmptyData(true);
            }
        });

        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(v.getChildAt(v.getChildCount() - 1) != null) {
                    if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                            scrollY > oldScrollY) {
                        mNewsRecyclerAdapter.expand(7);
                    }
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

        mNoDataRefreshLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                }
                setVisibilityForEmptyData(false);
                mNewsRecyclerAdapter.loadNews(generateURL(true), generateURL(false));
            }
        });

        mNewsRecyclerAdapter.loadNews(generateURL(true), generateURL(false));
    }

    public void setOnActionBarChangeListener(OnActionBarChangeListener listener) {
        mOnActionBarChangeListener = listener;
    }

    private void initTopBanner(final View view) {

        mStockListFragment = new StockListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(StockListFragment.TASK_NAME, StockListFragment.TASK.WPCT.getTaskId());
        mStockListFragment.setArguments(bundle);

        ConstraintLayout card1 = view.findViewById(R.id.top_banner_1);
        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fm = getFragmentManager();
                if(fm != null) {
                    FragmentTransaction transaction = fm.beginTransaction();

                    transaction.replace(R.id.fragment_container, mStockListFragment);
                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();
                }
                if(mOnActionBarChangeListener != null) {
                    mOnActionBarChangeListener.onActionBarChange(getResources().getString(R.string.main_page_card_sub_title), true);
                }
            }
        });
    }

    private void openStockActivity(Stock stock) {
        startActivity(StockActivity.generateStockActivityIntent(
                getContext(), stock.getName(), stock.getCode(),
                stock.getRaiseNum(), stock.getFallNum(),
                stock.getPrice(), stock.getDiffNumber(), stock.getDiffPercentage()));
        if(getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
        }
    }

    private void setVisibilityForEmptyData(boolean isEmpty) {
        if(isEmpty) {
            mNoDataRefreshLayout.setVisibility(View.VISIBLE);
            mNewsRecyclerView.setVisibility(View.GONE);
            mNoDataTextView.setText(R.string.ops_something_wrong);
            mNoDataImageView.setImageResource(R.drawable.baseline_sentiment_dissatisfied_24px);
        } else {
            mNoDataRefreshLayout.setVisibility(View.GONE);
        }
    }

    public ArrayList<String> generateURL(boolean isNetworkUrl) {
        if(getArguments() == null) {
            return null;
        }

        ArrayList<String> results = new ArrayList<>();
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
        return results;
    }



    @Override
    public void onDestroyView() {
        if(mNewsRecyclerAdapter != null) {
            mNewsRecyclerAdapter.destroy();
        }
        if(mStockListPlacer != null) {
            mStockListPlacer.clear();
        }

        FragmentManager fm = getFragmentManager();
        if(fm != null) {
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.remove(mStockListFragment);
            // Commit the transaction
            transaction.commitAllowingStateLoss();
        }

        if(mOnActionBarChangeListener != null) {
            mOnActionBarChangeListener.onActionBarChange(getResources().getString(R.string.app_name), false);
        }

        super.onDestroyView();
    }
}
