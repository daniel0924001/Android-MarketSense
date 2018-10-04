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
import com.idroi.marketsense.data.Stock;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/8/2.
 */

public class RankingListViewHolder {

    public View mainView;

    public ImageView foldImageView;
    public TextView trendTextView;
    public RecyclerView recyclerView;
    public StockRankingRecyclerAdapter stockRankingRecyclerAdapter;
    public View bottomDividor;

    public ConstraintLayout foldGroup;
    public int shrinkHeight;

    static final RankingListViewHolder EMPTY_VIEW_HOLDER = new RankingListViewHolder();

    private RankingListViewHolder() {

    }

    public static RankingListViewHolder convertToViewHolder(final View view,
                                                            int titleStringId,
                                                            int trendStringId) {
        final RankingListViewHolder rankingListViewHolder = new RankingListViewHolder();
        try {
            rankingListViewHolder.mainView = view;
            rankingListViewHolder.recyclerView = view.findViewById(R.id.stock_list);

            rankingListViewHolder.foldGroup = view.findViewById(R.id.fold_group);
            rankingListViewHolder.foldGroup.setZ(-100);

            rankingListViewHolder.bottomDividor = view.findViewById(R.id.bottom_divider);
            rankingListViewHolder.foldImageView = view.findViewById(R.id.iv_fold);

            TextView titleTextView = view.findViewById(R.id.ranking_title);
            titleTextView.setText(titleStringId);
            titleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(rankingListViewHolder.foldGroup.getVisibility() == View.VISIBLE) {
                        rankingListViewHolder.foldImageView.setImageResource(R.mipmap.ic_fold);
                        rankingListViewHolder.shrinkHeight = rankingListViewHolder.recyclerView.getHeight() + rankingListViewHolder.trendTextView.getHeight() + rankingListViewHolder.bottomDividor.getHeight();
                        Animation a = new Animation()
                        {
                            @Override
                            protected void applyTransformation(float interpolatedTime, Transformation t) {
                                rankingListViewHolder.foldGroup.setAlpha(1 - interpolatedTime);
                                if(interpolatedTime == 1){
                                    rankingListViewHolder.foldGroup.setVisibility(View.GONE);
                                    rankingListViewHolder.foldGroup.clearAnimation();
                                }else{
                                    rankingListViewHolder.foldGroup.getLayoutParams().height = rankingListViewHolder.shrinkHeight - (int)(rankingListViewHolder.shrinkHeight * interpolatedTime);
                                    rankingListViewHolder.foldGroup.requestLayout();
                                }
                            }

                            @Override
                            public boolean willChangeBounds() {
                                return true;
                            }
                        };

                        a.setDuration(300);
                        a.setFillAfter(true);
                        rankingListViewHolder.foldGroup.startAnimation(a);
                    } else {
                        rankingListViewHolder.foldImageView.setImageResource(R.mipmap.ic_unfold);
                        final Animation a = new Animation()
                        {
                            @Override
                            protected void applyTransformation(float interpolatedTime, Transformation t) {
                                rankingListViewHolder.foldGroup.setAlpha(interpolatedTime);
                                rankingListViewHolder.foldGroup.getLayoutParams().height = (int)(rankingListViewHolder.shrinkHeight * interpolatedTime);
                                rankingListViewHolder.foldGroup.requestLayout();
                            }

                            @Override
                            public boolean willChangeBounds() {
                                return true;
                            }
                        };

                        a.setDuration(300);
                        a.setFillAfter(true);
                        a.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                rankingListViewHolder.foldGroup.setVisibility(View.VISIBLE);
                                rankingListViewHolder.foldGroup.getLayoutParams().height = 1;
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        rankingListViewHolder.foldGroup.startAnimation(a);
                    }
                }
            });

            rankingListViewHolder.trendTextView = view.findViewById(R.id.company_trend);
            rankingListViewHolder.trendTextView.setText(trendStringId);

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
        stockRankingRecyclerAdapter = new StockRankingRecyclerAdapter(activity);
        stockRankingRecyclerAdapter.setStockList(stockArrayList, rankingType, needToSort);
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
}
