package com.idroi.marketsense.adapter;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockViewHolder {

    View mainView;

    TextView nameView;
    TextView codeView;

    ImageView priceImageView;
    TextView priceView;
    TextView diffView;

    ConstraintLayout techBlockView;
    TextView techTitleTextView;
    TextView techUnavailableTextView;
    ImageView[] techImageViews;

    ConstraintLayout newsBlockView;
    TextView newsTitleTextView;
    TextView newsUnavailableTextView;
    ImageView[] newsImageViews;

    ConstraintLayout todayBlock;
    TextView todayTitleTextView;
    TextView todayStatusTextView;

    ConstraintLayout tomorrowBlock;
    TextView tomorrowTitleTextView;
    TextView tomorrowStatusTextView;

    static final StockViewHolder EMPTY_VIEW_HOLDER = new StockViewHolder();

    private StockViewHolder() {}

    static StockViewHolder convertToViewHolder(final View view) {
        final StockViewHolder stockViewHolder = new StockViewHolder();
        stockViewHolder.mainView = view;
        try {

            // upper block
            stockViewHolder.nameView = view.findViewById(R.id.marketsense_stock_name_tv);
            stockViewHolder.codeView = view.findViewById(R.id.marketsense_stock_code_tv);
            stockViewHolder.priceImageView = view.findViewById(R.id.marketsense_stock_price_iv);
            stockViewHolder.priceView = view.findViewById(R.id.marketsense_stock_price_tv);
            stockViewHolder.diffView = view.findViewById(R.id.marketsense_stock_diff_tv);

            // two bottom right blocks
            stockViewHolder.techBlockView = view.findViewById(R.id.tech_block);
            stockViewHolder.techTitleTextView = view.findViewById(R.id.tech_block_title);
            stockViewHolder.techUnavailableTextView = view.findViewById(R.id.tech_block_unavailable);
            stockViewHolder.techImageViews = new ImageView[]{
                    view.findViewById(R.id.tech_block_icon1),
                    view.findViewById(R.id.tech_block_icon2),
                    view.findViewById(R.id.tech_block_icon3)};

            stockViewHolder.newsBlockView = view.findViewById(R.id.news_block);
            stockViewHolder.newsTitleTextView = view.findViewById(R.id.news_block_title);
            stockViewHolder.newsUnavailableTextView = view.findViewById(R.id.news_block_unavailable);
            stockViewHolder.newsImageViews = new ImageView[]{
                    view.findViewById(R.id.news_block_icon1),
                    view.findViewById(R.id.news_block_icon2),
                    view.findViewById(R.id.news_block_icon3)};

            // today part
            stockViewHolder.todayBlock = view.findViewById(R.id.today_block);
            stockViewHolder.todayTitleTextView = view.findViewById(R.id.today_block_predict_title);
            stockViewHolder.todayStatusTextView = view.findViewById(R.id.today_block_predict_status);

            // tomorrow part
            stockViewHolder.tomorrowBlock = view.findViewById(R.id.tomorrow_block);
            stockViewHolder.tomorrowTitleTextView = view.findViewById(R.id.tomorrow_block_predict_title);
            stockViewHolder.tomorrowStatusTextView = view.findViewById(R.id.tomorrow_block_predict_status);

            return stockViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
