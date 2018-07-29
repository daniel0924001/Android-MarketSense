package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.StockActivity;
import com.idroi.marketsense.data.Stock;

/**
 * Created by daniel.hsieh on 2018/4/19.
 */

public class NewsViewHolder{

    View mainView;
    TextView titleView;
    TextView dateView;
    TextView fireTextView;
    ImageView fireImageView;

    RecyclerView relatedRecyclerView;
    RelatedStockNameAdapter relatedStockNameAdapter;

    static final NewsViewHolder EMPTY_VIEW_HOLDER = new NewsViewHolder();

    private NewsViewHolder() {}

    static NewsViewHolder convertToViewHolder(final View view) {
        final NewsViewHolder newsViewHolder = new NewsViewHolder();
        newsViewHolder.mainView = view;
        try {
            newsViewHolder.titleView = view.findViewById(R.id.marketsense_news_title_tv);
            newsViewHolder.dateView = view.findViewById(R.id.marketsense_news_date_tv);
            newsViewHolder.fireTextView = view.findViewById(R.id.marketsense_news_fire_tv);
            newsViewHolder.fireImageView = view.findViewById(R.id.marketsense_news_fire_iv);
            newsViewHolder.relatedRecyclerView = view.findViewById(R.id.related_stock_name);
            newsViewHolder.relatedRecyclerView.setNestedScrollingEnabled(false);
            newsViewHolder.relatedRecyclerView.setLayoutManager(
                    new LinearLayoutManager(
                            view.getContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false));

            newsViewHolder.relatedStockNameAdapter = new RelatedStockNameAdapter(view.getContext());
            newsViewHolder.relatedRecyclerView.setAdapter(newsViewHolder.relatedStockNameAdapter);
            newsViewHolder.relatedStockNameAdapter.setOnItemClickListener(new RelatedStockNameAdapter.OnItemClickListener() {
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
            return newsViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
