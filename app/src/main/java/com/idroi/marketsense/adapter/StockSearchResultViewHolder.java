package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/5/31.
 */

public class StockSearchResultViewHolder {

    View mainView;
    TextView nameView;
    TextView codeView;

    static final StockSearchResultViewHolder EMPTY_VIEW_HOLDER = new StockSearchResultViewHolder();

    private StockSearchResultViewHolder() {

    }

    static StockSearchResultViewHolder convertToViewHolder(final View view) {
        final StockSearchResultViewHolder stockSearchResultViewHolder = new StockSearchResultViewHolder();
        stockSearchResultViewHolder.mainView = view;
        try {
            stockSearchResultViewHolder.nameView = view.findViewById(R.id.marketsense_stock_simple_name_tv);
            stockSearchResultViewHolder.codeView = view.findViewById(R.id.marketsense_stock_simple_code_tv);
            return stockSearchResultViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
