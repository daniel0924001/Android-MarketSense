package com.idroi.marketsense.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.idroi.marketsense.R;
import com.idroi.marketsense.StockActivity;
import com.idroi.marketsense.adapter.StockListRecyclerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.StockRequest;

import org.w3c.dom.Text;

import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;

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

    public enum TASK {
        PREDICT_WIN(PREDICT_WIN_ID),
        PREDICT_LOSE(PREDICT_LOSE_ID),
        ACTUAL_WIN(ACTUAL_WIN_ID),
        ACTUAL_LOSE(ACTUAL_LOSE_ID),
        SELF_CHOICES(SELF_CHOICES_ID);

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
    private int mTaskId;

    private UserProfile.UserProfileChangeListener mUserProfileChangeListener;

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
        mRecyclerView = view.findViewById(R.id.stock_recycler_view);
        mNoDataImageView = view.findViewById(R.id.no_stock_iv);
        if(mTaskId == SELF_CHOICES_ID) {
            mNoDataImageView.setImageResource(R.drawable.baseline_playlist_add_24px);
        }
        mNoDataTextView = view.findViewById(R.id.no_stock_tv);

        mStockListRecyclerAdapter = new StockListRecyclerAdapter(getActivity(), mTaskId);
        mRecyclerView.setAdapter(mStockListRecyclerAdapter);

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
                setVisibilityForEmptyData(false);
            }

            @Override
            public void onStockListEmpty() {
                if(mSkeletonScreen != null) {
                    mSkeletonScreen.hide();
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
                        stock.getRaiseNum(), stock.getFallNum()));
                if(getActivity() != null) {
                    getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
                }
            }
        });
    }

    public String generateNetworkURL() {
        return StockRequest.queryStockList(getContext(), true);
    }

    public String generateCacheUrl() {
        return StockRequest.queryStockList(getContext(), false);
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
        return mTaskId == SELF_CHOICES_ID && ClientData.getInstance().getUserProfile().isEmptyFavoriteStock();
    }
}
