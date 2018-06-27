package com.idroi.marketsense.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.idroi.marketsense.R;
import com.idroi.marketsense.StockActivity;
import com.idroi.marketsense.adapter.StockListRecyclerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.StockRequest;

import java.util.HashMap;

import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.datasource.StockListPlacer.SORT_BY_DIFF;
import static com.idroi.marketsense.datasource.StockListPlacer.SORT_BY_NAME;
import static com.idroi.marketsense.datasource.StockListPlacer.SORT_BY_NEWS;
import static com.idroi.marketsense.datasource.StockListPlacer.SORT_BY_PEOPLE;
import static com.idroi.marketsense.datasource.StockListPlacer.SORT_BY_PRICE;
import static com.idroi.marketsense.datasource.StockListPlacer.SORT_DOWNWARD;
import static com.idroi.marketsense.datasource.StockListPlacer.SORT_UPWARD;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListFragment extends Fragment {

    public final static String TASK_NAME = "TASK_NAME";
    public final static int PREDICT_WIN_ID = 1;
    public final static int PREDICT_LOSE_ID = 2;
    public final static int ACTUAL_WIN_ID = 3;
    public final static int ACTUAL_LOSE_ID = 4;
    public final static int SELF_CHOICES_ID = 5;
    public final static int WPCT_ID = 6;

    public enum TASK {
        PREDICT_WIN(PREDICT_WIN_ID),
        PREDICT_LOSE(PREDICT_LOSE_ID),
        ACTUAL_WIN(ACTUAL_WIN_ID),
        ACTUAL_LOSE(ACTUAL_LOSE_ID),
        SELF_CHOICES(SELF_CHOICES_ID),
        WPCT(WPCT_ID);

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
    private StockListRecyclerAdapter mStockListRecyclerAdapter;
    private RecyclerViewSkeletonScreen mSkeletonScreen;
    private ProgressBar mLoadingProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mTaskId;

    private TextView mSortedByName, mSortedByPrice, mSortedByDiff, mSortedByPeople, mSortedByNews;
    private TextView[] mSortedViews;
    private HashMap<View, String> mSortedTexts;
    private View mLastSortedView;
    private int mSortedDirection;

    private UserProfile.UserProfileChangeListener mUserProfileChangeListener;

    private ConstraintLayout mNoDataRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if(getArguments() != null) {
            mTaskId = getArguments().getInt(TASK_NAME);
        } else {
            mTaskId = PREDICT_WIN_ID; // default
        }

        final View view = inflater.inflate(R.layout.stock_list_fragment, container, false);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_to_refresh);
        mRecyclerView = view.findViewById(R.id.stock_recycler_view);
        mNoDataRefreshLayout = view.findViewById(R.id.no_data_block);
        mNoDataImageView = view.findViewById(R.id.no_stock_iv);
        if(mTaskId == SELF_CHOICES_ID) {
            mNoDataImageView.setImageResource(R.drawable.baseline_playlist_add_24px);
        }
        mNoDataTextView = view.findViewById(R.id.no_stock_tv);

        mStockListRecyclerAdapter = new StockListRecyclerAdapter(getActivity(), mTaskId, SORT_BY_NEWS, SORT_DOWNWARD);
        mRecyclerView.setAdapter(mStockListRecyclerAdapter);

        mLoadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        mSkeletonScreen = Skeleton.bind(mRecyclerView)
                .adapter(mStockListRecyclerAdapter)
                .load(R.layout.layout_default_item_skeleton)
                .shimmer(false)
                .show();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if(mTaskId == SELF_CHOICES_ID) {
            mUserProfileChangeListener = new UserProfile.UserProfileChangeListener() {
                @Override
                public void onUserProfileChange(int notifyId) {
                    if(notifyId == NOTIFY_ID_FAVORITE_LIST) {
                        MSLog.d("onUserProfileChange in StockListFragment: " + generateNetworkURL());
                        mStockListRecyclerAdapter.loadStockList(generateNetworkURL(), generateCacheUrl());
                    }
                }
            };
            ClientData.getInstance().getUserProfile()
                    .addUserProfileChangeListener(mUserProfileChangeListener);
        }

        view.setBackgroundColor(getResources().getColor(R.color.bottom_navigation_item_checked_false_bg));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStockListRecyclerAdapter.setStockListAvailableListener(new StockListRecyclerAdapter.StockListAvailableListener() {
            @Override
            public void onStockListAvailable() {
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
            public void onStockListEmpty() {
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
        mStockListRecyclerAdapter.loadStockList(generateNetworkURL(), generateCacheUrl());

        mStockListRecyclerAdapter.setOnItemClickListener(new StockListRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Stock stock) {
                startActivity(StockActivity.generateStockActivityIntent(
                        getContext(), stock.getName(), stock.getCode(),
                        stock.getRaiseNum(), stock.getFallNum(),
                        stock.getPrice(), stock.getDiffNumber(), stock.getDiffPercentage()));
                if(getActivity() != null) {
                    getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mStockListRecyclerAdapter.loadStockList(generateNetworkURL(), generateCacheUrl());
            }
        });

        mNoDataRefreshLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mSkeletonScreen != null) {
                    mSkeletonScreen.show();
                }
                setVisibilityForEmptyData(false);
                mStockListRecyclerAdapter.loadStockList(generateNetworkURL(), generateCacheUrl());
            }
        });

        setSortBlock(view);
    }

    private void setSortBlock(View view) {
        mSortedByName = view.findViewById(R.id.sorted_by_name);
        mSortedByPrice = view.findViewById(R.id.sorted_by_price);
        mSortedByDiff = view.findViewById(R.id.sorted_by_diff);
        mSortedByPeople = view.findViewById(R.id.sorted_by_people);
        mSortedByNews = view.findViewById(R.id.sorted_by_news);

        mSortedViews = new TextView[] {
                mSortedByName, mSortedByPrice, mSortedByDiff, mSortedByPeople, mSortedByNews};
        mSortedTexts = new HashMap<>();
        mSortedTexts.put(mSortedByName, getString(R.string.title_company_predict_name));
        mSortedTexts.put(mSortedByPrice, getString(R.string.title_company_predict_price));
        mSortedTexts.put(mSortedByDiff, getString(R.string.title_company_predict_fluctuation));
        mSortedTexts.put(mSortedByPeople, getString(R.string.title_company_predict_people_title));
        mSortedTexts.put(mSortedByNews, getString(R.string.title_company_predict_news_title));

        mLastSortedView = null;
        mSortedDirection = SORT_UPWARD;

        mSortedByName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSortedBlockLayout(SORT_BY_NAME, view);
            }
        });

        mSortedByPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSortedBlockLayout(SORT_BY_PRICE, view);
            }
        });

        mSortedByDiff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSortedBlockLayout(SORT_BY_DIFF, view);
            }
        });

        mSortedByPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSortedBlockLayout(SORT_BY_PEOPLE, view);
            }
        });

        mSortedByNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSortedBlockLayout(SORT_BY_NEWS, view);
            }
        });
        changeSortedBlockLayout(SORT_BY_NEWS, mSortedByNews);
    }

    private void changeSortedBlockLayout(int field, View view) {
        if(view != mLastSortedView) {
            mLastSortedView = view;
            if(field != SORT_BY_NAME) {
                mSortedDirection = SORT_DOWNWARD;
            } else {
                mSortedDirection = SORT_UPWARD;
            }
        } else {
            mSortedDirection = (mSortedDirection == SORT_UPWARD ? SORT_DOWNWARD : SORT_UPWARD);
        }
        mStockListRecyclerAdapter.sortByTask(field, mSortedDirection);

        for (TextView textView : mSortedViews) {
            if (textView != view) {
                // others
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_sorting_off, 0);
            } else {
                // sorted one
                if(mSortedDirection == SORT_UPWARD) {
                    textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_sorting_on_up, 0);
                } else {
                    textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_sorting_on_down, 0);
                }
            }
        }
    }

    public String generateNetworkURL() {
        if(mTaskId == WPCT_ID) {
            return StockRequest.queryStockListWithMode(getContext(), true, StockRequest.MODE_WPCT);
        } else {
            return StockRequest.queryStockList(getContext(), true);
        }
    }

    public String generateCacheUrl() {
        if(mTaskId == WPCT_ID) {
            return StockRequest.queryStockListWithMode(getContext(), false, StockRequest.MODE_WPCT);
        } else {
            return StockRequest.queryStockList(getContext(), false);
        }
    }

    @Override
    public void onDestroyView() {
        mStockListRecyclerAdapter.destroy();
        if(mTaskId == SELF_CHOICES_ID) {
            ClientData.getInstance().getUserProfile()
                    .deleteUserProfileChangeListener(mUserProfileChangeListener);
        }
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
        return mTaskId == SELF_CHOICES_ID && ClientData.getInstance().getUserProfile().isEmptyFavoriteStock();
    }
}
