package com.idroi.marketsense.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ethanhua.skeleton.RecyclerViewSkeletonScreen;
import com.ethanhua.skeleton.Skeleton;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.MainActivity;
import com.idroi.marketsense.R;
import com.idroi.marketsense.StockActivity;
import com.idroi.marketsense.adapter.StockListRecyclerAdapter;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.request.StockRequest;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListFragment extends Fragment {

    public final static String TASK_NAME = "TASK_NAME";
    public final static String CONCEPT_NAME = "CONCEPT";
    public final static int PREDICT_WIN_ID = 1;
    public final static int PREDICT_LOSE_ID = 2;
    public final static int ACTUAL_WIN_ID = 3;
    public final static int ACTUAL_LOSE_ID = 4;
    public final static int SELF_CHOICES_ID = 5;

    public enum TASK {
        PREDICT_WIN(PREDICT_WIN_ID),
        PREDICT_LOSE(PREDICT_LOSE_ID),
        ACTUAL_WIN(PREDICT_WIN_ID),
        ACTUAL_LOSE(PREDICT_LOSE_ID),
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
    private StockListRecyclerAdapter mStockListRecyclerAdapter;
    private RecyclerViewSkeletonScreen mSkeletonScreen;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.stock_fragment, container, false);
        mRecyclerView = view.findViewById(R.id.stock_recycler_view);

        mStockListRecyclerAdapter = new StockListRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mStockListRecyclerAdapter);

        mSkeletonScreen = Skeleton.bind(mRecyclerView)
                .adapter(mStockListRecyclerAdapter)
                .load(R.layout.layout_default_item_skeleton).show();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStockListRecyclerAdapter.setStockListAvailableListener(new StockListRecyclerAdapter.StockListAvailableListener() {
            @Override
            public void onStockListAvailable() {
                if(mSkeletonScreen != null) {
                    mSkeletonScreen.hide();
                }
            }
        });
        mStockListRecyclerAdapter.loadStockList(generateURL());

        mStockListRecyclerAdapter.setOnItemClickListener(new StockListRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Stock stock) {
                startActivity(StockActivity.generateStockActivityIntent(
                        getContext(), stock.getName()));
                getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
    }

    public String generateURL() {
        if(getArguments() == null) {
            return StockRequest.queryStockList();
        }

        int taskId = getArguments().getInt(TASK_NAME);
        switch (taskId) {
            case PREDICT_WIN_ID:
            case PREDICT_LOSE_ID:
            case ACTUAL_WIN_ID:
            case ACTUAL_LOSE_ID:
                return StockRequest.queryStockList();
            case SELF_CHOICES_ID:
                return "marketsense://stock_favorite";
            default:
                return StockRequest.queryStockList();
        }
    }

    @Override
    public void onDestroyView() {
        mStockListRecyclerAdapter.destroy();
        super.onDestroyView();
    }
}
