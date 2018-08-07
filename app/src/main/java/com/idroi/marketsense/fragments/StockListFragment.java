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

import com.idroi.marketsense.R;
import com.idroi.marketsense.StockActivity;
import com.idroi.marketsense.adapter.StockListRecyclerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.StockRequest;

import static com.idroi.marketsense.adapter.StockListRecyclerAdapter.ADAPTER_UPDATE_PRICE_ONLY;
import static com.idroi.marketsense.adapter.StockListRecyclerAdapter.ADAPTER_UPDATE_RIGHT_BLOCK_ONLY;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_PRICE_CHANGED;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_RIGHT_PART_CHANGE;
import static com.idroi.marketsense.datasource.StockListPlacer.SORT_BY_PREDICTION;
import static com.idroi.marketsense.datasource.StockListPlacer.SORT_DOWNWARD;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListFragment extends Fragment {

    public final static String TASK_NAME = "TASK_NAME";
    public final static int MAIN_ID = 1;
    public final static int NORMAL_ID = 2;
    public final static int SELF_CHOICES_ID = 5;
    public final static int WPCT_ID = 6;

    public enum TASK {
        MAIN(MAIN_ID),
        NORMAL(NORMAL_ID),
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
    private ProgressBar mLoadingProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mTaskId;

    private UserProfile.GlobalBroadcastListener mGlobalBroadcastListener;

    private ConstraintLayout mNoDataRefreshLayout;

    private boolean mIsRecyclerViewIdle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if(getArguments() != null) {
            mTaskId = getArguments().getInt(TASK_NAME);
        } else {
            mTaskId = MAIN_ID; // default
        }

        ClientData clientData = ClientData.getInstance();
        if(clientData != null) {
            clientData.updateClockInformation();
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

        mStockListRecyclerAdapter = new StockListRecyclerAdapter(getActivity(), mTaskId, SORT_BY_PREDICTION, SORT_DOWNWARD);
        mRecyclerView.setAdapter(mStockListRecyclerAdapter);

        mLoadingProgressBar = view.findViewById(R.id.loading_progress_bar);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mIsRecyclerViewIdle = true;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mIsRecyclerViewIdle = (newState == RecyclerView.SCROLL_STATE_IDLE);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mGlobalBroadcastListener = new UserProfile.GlobalBroadcastListener() {
            @Override
            public void onGlobalBroadcast(int notifyId, Object payload) {
                if(notifyId == NOTIFY_ID_FAVORITE_LIST && mTaskId == SELF_CHOICES_ID) {
                    loadStockList();
                } else if(notifyId == NOTIFY_ID_PRICE_CHANGED && mIsRecyclerViewIdle) {
                    mStockListRecyclerAdapter.updatePriceInVisibleItems(ADAPTER_UPDATE_PRICE_ONLY);
                } else if(notifyId == NOTIFY_ID_RIGHT_PART_CHANGE) {
                    mStockListRecyclerAdapter.updatePriceInVisibleItems(ADAPTER_UPDATE_RIGHT_BLOCK_ONLY);
                }
            }
        };
        ClientData.getInstance().getUserProfile().addGlobalBroadcastListener(mGlobalBroadcastListener);

        view.setBackgroundColor(getResources().getColor(R.color.bottom_navigation_item_checked_false_bg));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStockListRecyclerAdapter.setStockListAvailableListener(new StockListRecyclerAdapter.StockListAvailableListener() {
            @Override
            public void onStockListAvailable() {
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
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                }
                if(mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                setVisibilityForEmptyData(true);
            }
        });
        loadStockList();

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
                loadStockList();
            }
        });

        mNoDataRefreshLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibilityForEmptyData(false);
                loadStockList();
            }
        });
    }

    private void loadStockList() {
        if(mStockListRecyclerAdapter != null) {
            if(mTaskId == WPCT_ID) {
                mStockListRecyclerAdapter.loadStockList(
                        StockRequest.queryStockListWithMode(getContext(), true, StockRequest.MODE_WPCT),
                        StockRequest.queryStockListWithMode(getContext(), false, StockRequest.MODE_WPCT),
                        StockRequest.MODE_WPCT
                );
            } else {
                mStockListRecyclerAdapter.loadStockList(
                        StockRequest.queryStockList(getContext(), true),
                        StockRequest.queryStockList(getContext(), false));
            }
        }
    }

    @Override
    public void onDestroyView() {
        mStockListRecyclerAdapter.destroy();
        mRecyclerView.clearOnScrollListeners();
        ClientData.getInstance().getUserProfile()
                .deleteGlobalBroadcastListener(mGlobalBroadcastListener);
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
