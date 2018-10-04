package com.idroi.marketsense.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.idroi.marketsense.NewsWebViewActivity;
import com.idroi.marketsense.R;
import com.idroi.marketsense.StockActivity;
import com.idroi.marketsense.StockKnowledgeListActivity;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.adapter.StockRankingRecyclerAdapter;
import com.idroi.marketsense.adapter.StockRankingRenderer;
import com.idroi.marketsense.adapter.TopBannerPagerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.datasource.StockListPlacer;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.request.StockRequest;
import com.idroi.marketsense.viewholders.LoadingDotsPageViewHolder;
import com.idroi.marketsense.viewholders.RankingListViewHolder;

import java.util.ArrayList;

import static com.idroi.marketsense.adapter.NewsRecyclerAdapter.NEWS_SINGLE_LAYOUT;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_NEWS_READ_RECORD_LIST;
import static com.idroi.marketsense.fragments.NewsFragment.GENERAL_TASK_ID;
import static com.idroi.marketsense.fragments.StockListFragment.NORMAL_ID;
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
    private ProgressBar mLoadingProgressBar;

    private StockListPlacer mStockListPlacer;

    private NestedScrollView mNestedScrollView;
    private Fragment mStockListFragment;
    private OnActionBarChangeListener mOnActionBarChangeListener;

    private RankingListViewHolder mTechBlockViewHolder, mNewsBlockViewHolder;
    private LoadingDotsPageViewHolder mLoadingDotsPageViewHolder;

    private UserProfile.GlobalBroadcastListener mGlobalBroadcastListener;

    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private ViewPager mViewPager;
    private Runnable mSwipeRunnable;
    private Handler mSwipeHandler;
    private final int REFRESH_TIME = 6000;

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

        mLoadingProgressBar = view.findViewById(R.id.news_list_progress_bar);

        mNestedScrollView = view.findViewById(R.id.body_scroll_view);

        mLoadingDotsPageViewHolder = LoadingDotsPageViewHolder
                .convertToViewHolder(view.findViewById(R.id.loading_dots_page));
        mTechBlockViewHolder =
                RankingListViewHolder.convertToViewHolder(
                        view.findViewById(R.id.tech_block),
                        R.string.main_page_tech_ranking,
                        R.string.main_page_tech_trend);
        mNewsBlockViewHolder =
                RankingListViewHolder.convertToViewHolder(
                        view.findViewById(R.id.news_block),
                        R.string.main_page_news_ranking,
                        R.string.main_page_news_trend);

        initTopBanner(view.findViewById(R.id.top_banner_block));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGlobalBroadcastListener = new UserProfile.GlobalBroadcastListener() {
            @Override
            public void onGlobalBroadcast(int notifyId, Object payload) {
                if(notifyId == NOTIFY_ID_NEWS_READ_RECORD_LIST) {
                    MSLog.d("update user's read news records");
                    mNewsRecyclerAdapter.notifyItemRangeChanged(0, mNewsRecyclerAdapter.getItemCount());
                }
            }
        };

        ClientData clientData = ClientData.getInstance(getContext());
        UserProfile userProfile = clientData.getUserProfile();
        if(userProfile != null) {
            userProfile.addGlobalBroadcastListener(mGlobalBroadcastListener);
        }

        ArrayList<Stock> techSortedStocks = clientData.getSortedRealTimePrices(StockRankingRenderer.RANKING_BY_TECH);
        ArrayList<Stock> newsSortedStocks = clientData.getSortedRealTimePrices(StockRankingRenderer.RANKING_BY_NEWS);

        if(techSortedStocks != null && techSortedStocks.size() > 0 && newsSortedStocks != null && newsSortedStocks.size() > 0) {
            MSLog.d("sorted stocks are in memory");
            mTechBlockViewHolder.update(getActivity(), techSortedStocks, StockRankingRenderer.RANKING_BY_TECH, new StockRankingRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Stock stock) {
                    openStockActivity(stock);
                }
            }, false);
            mNewsBlockViewHolder.update(getActivity(), newsSortedStocks, StockRankingRenderer.RANKING_BY_NEWS, new StockRankingRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Stock stock) {
                    openStockActivity(stock);
                }
            }, false);
        } else {
            MSLog.d("sorted stocks are not in memory");
            mStockListPlacer = new StockListPlacer(getActivity(), NORMAL_ID);
            mStockListPlacer.setStockListListener(new StockListPlacer.StockListListener() {
                @Override
                public void onStockListLoaded() {
                    mLoadingDotsPageViewHolder.stopAndGone();

                    if(mStockListPlacer.getStocks() != null) {
                        mTechBlockViewHolder.update(getActivity(), mStockListPlacer.getStocks(), StockRankingRenderer.RANKING_BY_TECH, new StockRankingRecyclerAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(Stock stock) {
                                openStockActivity(stock);
                            }
                        }, true);
                        mNewsBlockViewHolder.update(getActivity(), mStockListPlacer.getStocks(), StockRankingRenderer.RANKING_BY_NEWS, new StockRankingRecyclerAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(Stock stock) {
                                openStockActivity(stock);
                            }
                        }, true);
                    } else {
                        mTechBlockViewHolder.hide();
                        mNewsBlockViewHolder.hide();
                    }
                }
            });

            mStockListPlacer.setLoadingPageListener(new StockListPlacer.LoadingPageListener() {
                @Override
                public void onLoadingPageVisible() {
                    mLoadingDotsPageViewHolder.start(getActivity());
                }
            });
            mStockListPlacer.loadStockList(
                    StockRequest.queryStockList(getActivity(), true),
                    StockRequest.queryStockList(getActivity(), false));
        }
        clientData.prefetchData();

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

        final RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        radioGroup.check(R.id.radio_button_1);
                        break;
                    case 1:
                        radioGroup.check(R.id.radio_button_2);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };

        mViewPager = view.findViewById(R.id.top_banner);
        final LayoutInflater mInflater = LayoutInflater.from(getContext());

        View view1 = mInflater.inflate(R.layout.layout_top_banner_image_view_top_10, null);
        View view2 = mInflater.inflate(R.layout.layout_top_banner_image_view_knowledge, null);

        ArrayList<View> viewArrayList = new ArrayList<>();
        viewArrayList.add(view1);
        viewArrayList.add(view2);

        mViewPager.setAdapter(new TopBannerPagerAdapter(viewArrayList, new TopBannerPagerAdapter.TopBannerClickListener() {
            @Override
            public void onItemClick(int position) {
                Activity activity = getActivity();
                if(activity == null) {
                    return;
                }

                switch (position) {
                    case 0:
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
                        break;
                    case 1:
                        Intent intent = new Intent(activity, StockKnowledgeListActivity.class);
                        startActivity(intent);
                        activity.overridePendingTransition(R.anim.enter, R.anim.stop);
                        break;
                }
            }
        }));
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        mViewPager.setCurrentItem(0);

        mSwipeHandler = new Handler();
        mSwipeRunnable = new Runnable() {
            @Override
            public void run() {
                PagerAdapter pagerAdapter = mViewPager.getAdapter();
                if(pagerAdapter != null) {
                    int position = (mViewPager.getCurrentItem() + 1) % pagerAdapter.getCount();
                    mViewPager.setCurrentItem(position);
                }
                mSwipeHandler.postDelayed(mSwipeRunnable, REFRESH_TIME);
            }
        };
        mSwipeHandler.postDelayed(mSwipeRunnable, REFRESH_TIME);
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
        if(mViewPager != null) {
            mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
        }

        if(mNewsRecyclerAdapter != null) {
            mNewsRecyclerAdapter.destroy();
        }
        if(mStockListPlacer != null) {
            mStockListPlacer.clear();
        }
        if(mTechBlockViewHolder != null) {
            mTechBlockViewHolder.destroy();
        }
        if(mNewsBlockViewHolder != null) {
            mNewsBlockViewHolder.destroy();
        }
        if(mLoadingDotsPageViewHolder != null) {
            mLoadingDotsPageViewHolder.stopAndGone();
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

        UserProfile userProfile = ClientData.getInstance(getContext()).getUserProfile();
        if(userProfile != null) {
            userProfile.deleteGlobalBroadcastListener(mGlobalBroadcastListener);
        }

        super.onDestroyView();
    }
}
