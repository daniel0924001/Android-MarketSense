package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/7/28.
 */

public class StockRankingViewHolder {

    TextView nameView;
    TextView priceView;

    ImageView predictionImageView;

    static final StockRankingViewHolder EMPTY_VIEW_HOLDER = new StockRankingViewHolder();

    private StockRankingViewHolder() {}

    static StockRankingViewHolder convertToViewHolder(final View view) {
        final StockRankingViewHolder stockRankingViewHolder = new StockRankingViewHolder();
        try {
            stockRankingViewHolder.nameView = view.findViewById(R.id.ranking_stock_title);
            stockRankingViewHolder.priceView = view.findViewById(R.id.ranking_stock_price);
            stockRankingViewHolder.predictionImageView = view.findViewById(R.id.ranking_stock_iv);
            return stockRankingViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
