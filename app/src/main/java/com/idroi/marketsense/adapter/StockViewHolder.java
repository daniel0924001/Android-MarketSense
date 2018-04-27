package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    ImageView trendImageView;
    TextView trendTextView;
    TextView confidenceTextView;
    ProgressBar progressBar;

    static final StockViewHolder EMPTY_VIEW_HOLDER = new StockViewHolder();

    private StockViewHolder() {}

    static StockViewHolder convertToViewHolder(final View view) {
        final StockViewHolder stockViewHolder = new StockViewHolder();
        stockViewHolder.mainView = view;
        try {
            stockViewHolder.nameView = view.findViewById(R.id.marketsense_stock_name_tv);
            stockViewHolder.codeView = view.findViewById(R.id.marketsense_stock_code_tv);
            stockViewHolder.trendImageView = view.findViewById(R.id.marketsense_stock_trend_iv);
            stockViewHolder.trendTextView = view.findViewById(R.id.marketsense_stock_trend_tv);
            stockViewHolder.confidenceTextView = view.findViewById(R.id.marketsense_stock_confidence_tv);
            stockViewHolder.progressBar = view.findViewById(R.id.marketsense_stock_confidence_progressbar);
            return stockViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
