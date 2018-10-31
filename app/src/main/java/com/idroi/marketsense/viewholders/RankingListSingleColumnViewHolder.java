package com.idroi.marketsense.viewholders;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.StockRankingRecyclerAdapter;
import com.idroi.marketsense.adapter.StockRankingRendererSingleColumn;
import com.idroi.marketsense.data.Stock;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/8/2.
 */

public class RankingListSingleColumnViewHolder {

    public View mainView;

    public TextView valueTextView;
    public ImageView foldImageView;
    public RecyclerView recyclerView;
    public StockRankingRecyclerAdapter stockRankingRecyclerAdapter;
    public View bottomDivisor;

    public ConstraintLayout foldGroup;
    public int shrinkHeight;

    static final RankingListSingleColumnViewHolder EMPTY_VIEW_HOLDER = new RankingListSingleColumnViewHolder();

    private RankingListSingleColumnViewHolder() {

    }

    public static RankingListSingleColumnViewHolder convertToViewHolder(final View view,
                                                                        int titleStringId) {
        final RankingListSingleColumnViewHolder rankingListViewHolder = new RankingListSingleColumnViewHolder();
        try {
            rankingListViewHolder.mainView = view;
            rankingListViewHolder.recyclerView = view.findViewById(R.id.stock_list);

            rankingListViewHolder.foldGroup = view.findViewById(R.id.fold_group);
            rankingListViewHolder.foldGroup.setZ(-100);

            rankingListViewHolder.bottomDivisor = view.findViewById(R.id.bottom_divider);
            rankingListViewHolder.foldImageView = view.findViewById(R.id.iv_fold);

            rankingListViewHolder.valueTextView = view.findViewById(R.id.company_value);
            TextView titleTextView = view.findViewById(R.id.ranking_title);
            titleTextView.setText(titleStringId);
            titleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(rankingListViewHolder.foldGroup.getVisibility() == View.VISIBLE) {
                        rankingListViewHolder.close();
                    } else {
                        rankingListViewHolder.open();
                    }
                }
            });

            return rankingListViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void update(Activity activity,
                       ArrayList<Stock> stockArrayList,
                       int rankingType,
                       StockRankingRecyclerAdapter.OnItemClickListener listener,
                       boolean needToSort) {
        stockRankingRecyclerAdapter = new StockRankingRecyclerAdapter(activity, new StockRankingRendererSingleColumn(), rankingType);
        stockRankingRecyclerAdapter.setStockList(stockArrayList, needToSort);
        stockRankingRecyclerAdapter.setOnItemClickListener(listener);

        recyclerView.setAdapter(stockRankingRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setNestedScrollingEnabled(false);
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

    public void close() {
        foldImageView.setImageResource(R.mipmap.ic_fold);
        shrinkHeight = recyclerView.getHeight() + valueTextView.getHeight() + bottomDivisor.getHeight();
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                foldGroup.setAlpha(1 - interpolatedTime);
                if(interpolatedTime == 1){
                    foldGroup.getLayoutParams().height = 0;
                    foldGroup.setVisibility(View.GONE);
                    foldGroup.clearAnimation();
                }else{
                    foldGroup.getLayoutParams().height = shrinkHeight - (int)(shrinkHeight * interpolatedTime);
                    foldGroup.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(300);
        a.setFillAfter(true);
        foldGroup.startAnimation(a);
    }

    public void open() {
        foldImageView.setImageResource(R.mipmap.ic_unfold);
        final Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                foldGroup.setAlpha(interpolatedTime);
                foldGroup.getLayoutParams().height = (int)(shrinkHeight * interpolatedTime);
                foldGroup.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(300);
        a.setFillAfter(true);
        foldGroup.startAnimation(a);
        foldGroup.setVisibility(View.VISIBLE);
    }
}
