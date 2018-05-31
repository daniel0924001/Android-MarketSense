package com.idroi.marketsense.adapter;

import android.view.View;
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

    TextView priceView;
    TextView diffView;

    TextView predictNewsText;
    TextView predictPeopleText;

    static final StockViewHolder EMPTY_VIEW_HOLDER = new StockViewHolder();

    private StockViewHolder() {}

    static StockViewHolder convertToViewHolder(final View view) {
        final StockViewHolder stockViewHolder = new StockViewHolder();
        stockViewHolder.mainView = view;
        try {
            stockViewHolder.nameView = view.findViewById(R.id.marketsense_stock_name_tv);
            stockViewHolder.codeView = view.findViewById(R.id.marketsense_stock_code_tv);
            stockViewHolder.priceView = view.findViewById(R.id.marketsense_stock_price_tv);
            stockViewHolder.diffView = view.findViewById(R.id.marketsense_stock_diff_tv);
            stockViewHolder.predictNewsText = view.findViewById(R.id.predict_news_description);
            stockViewHolder.predictPeopleText = view.findViewById(R.id.predict_people_description);
            return stockViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
