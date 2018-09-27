package com.idroi.marketsense.viewholders;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.StockActivity;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.adapter.RelatedStockNameAdapter;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.Stock;

/**
 * Created by daniel.hsieh on 2018/9/25.
 */

public class NewsReferencedByCommentViewHolder {

    public View mainView;

    public TextView titleTextView;
    public TextView predictionTextView;

    public RecyclerView relatedStockRecyclerView;
    public RelatedStockNameAdapter relatedStockNameAdapter;

    static final NewsReferencedByCommentViewHolder EMPTY_VIEW_HOLDER = new NewsReferencedByCommentViewHolder();

    private NewsReferencedByCommentViewHolder() {

    }

    public static NewsReferencedByCommentViewHolder convertToViewHolder(final View view) {
        final NewsReferencedByCommentViewHolder viewHolder = new NewsReferencedByCommentViewHolder();
        try {
            viewHolder.mainView = view;
            viewHolder.titleTextView = view.findViewById(R.id.news_title);
            viewHolder.predictionTextView = view.findViewById(R.id.news_prediction);

            viewHolder.relatedStockRecyclerView = view.findViewById(R.id.related_stock_name);
            viewHolder.relatedStockRecyclerView.setNestedScrollingEnabled(false);
            viewHolder.relatedStockRecyclerView.setLayoutManager(
                    new LinearLayoutManager(
                            view.getContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false));

            viewHolder.relatedStockNameAdapter = new RelatedStockNameAdapter(view.getContext());
            viewHolder.relatedStockRecyclerView.setAdapter(viewHolder.relatedStockNameAdapter);
            viewHolder.relatedStockNameAdapter.setOnItemClickListener(new RelatedStockNameAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Stock stock) {
                    try {
                        Activity activity = (Activity) view.getContext();
                        activity.startActivity(StockActivity.generateStockActivityIntent(
                                activity, stock.getName(), stock.getCode(), stock.getRaiseNum(), stock.getFallNum(),
                                stock.getPrice(), stock.getDiffNumber(), stock.getDiffPercentage()));
                        activity.overridePendingTransition(R.anim.enter, R.anim.stop);
                    } catch (Exception e) {
                        MSLog.e("related stock name click exception: " + e.toString());
                    }
                }
            });

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void update(Context context, final News news, final CommentsRecyclerViewAdapter.OnNewsItemClickListener listener) {

        mainView.setVisibility(View.VISIBLE);
        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onNewsItemClick(news);
            }
        });
        MarketSenseRendererHelper.addTextView(titleTextView, news.getTitle());

        update(context, news.getTitle(), news.getStockKeywords(), news.getLevel());
    }

    public void update(Context context, String title, String[] relatedStockNames, int level) {
        mainView.setVisibility(View.VISIBLE);
        MarketSenseRendererHelper.addTextView(titleTextView, title);

        relatedStockNameAdapter.setRelatedStockNames(relatedStockNames);
        if(relatedStockNameAdapter.hasRelatedStock()) {
            relatedStockRecyclerView.setVisibility(View.VISIBLE);
        } else {
            relatedStockRecyclerView.setVisibility(View.GONE);
        }

        if(level > 0) {
            predictionTextView.setVisibility(View.VISIBLE);
            predictionTextView.setTextColor(context.getResources().getColor(R.color.trend_red));
            relatedStockNameAdapter.setMaxItemCount(2);
        } else if(level < 0) {
            predictionTextView.setVisibility(View.VISIBLE);
            predictionTextView.setTextColor(context.getResources().getColor(R.color.trend_green));
            relatedStockNameAdapter.setMaxItemCount(2);
        } else {
            predictionTextView.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) relatedStockRecyclerView.getLayoutParams();
            params.goneStartMargin = 0;
            relatedStockRecyclerView.setLayoutParams(params);
            relatedStockNameAdapter.setMaxItemCount(3);
        }

        switch (level) {
            case 3:
                predictionTextView.setText(R.string.title_news_good3);
                break;
            case 2:
                predictionTextView.setText(R.string.title_news_good2);
                break;
            case 1:
                predictionTextView.setText(R.string.title_news_good1);
                break;
            case -1:
                predictionTextView.setText(R.string.title_news_bad1);
                break;
            case -2:
                predictionTextView.setText(R.string.title_news_bad2);
                break;
            case -3:
                predictionTextView.setText(R.string.title_news_bad3);
                break;
        }
    }
}
