package com.idroi.marketsense.viewholders;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.StockRankingRecyclerAdapter;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.datasource.StockListPlacer;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/8/2.
 */

public class RankingListViewHolder {

    public View mainView;
    public ProgressBar progressBar;

    public RecyclerView recyclerView;
    public StockRankingRecyclerAdapter stockRankingRecyclerAdapter;

    static final RankingListViewHolder EMPTY_VIEW_HOLDER = new RankingListViewHolder();

    private RankingListViewHolder() {

    }

    public static RankingListViewHolder convertToViewHolder(final View view, int titleStringId) {
        final RankingListViewHolder rankingListViewHolder = new RankingListViewHolder();
        try {
            rankingListViewHolder.mainView = view;
            rankingListViewHolder.progressBar = view.findViewById(R.id.loading_progress_bar);
            rankingListViewHolder.recyclerView = view.findViewById(R.id.stock_list);

            TextView titleTextView = view.findViewById(R.id.ranking_title);
            titleTextView.setText(titleStringId);

            return rankingListViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void update(Activity activity,
                       ArrayList<Stock> stockArrayList,
                       int rankingType,
                       StockRankingRecyclerAdapter.OnItemClickListener listener) {
        stockRankingRecyclerAdapter = new StockRankingRecyclerAdapter(activity, stockArrayList, rankingType);
        recyclerView.setAdapter(stockRankingRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setNestedScrollingEnabled(false);
        stockRankingRecyclerAdapter.setOnItemClickListener(listener);
    }

    public void hide() {
        recyclerView.setVisibility(View.GONE);
        mainView.setVisibility(View.GONE);
    }

    public void destroy() {
        if(stockRankingRecyclerAdapter != null) {
            stockRankingRecyclerAdapter.destroy();
        }
    }
}
