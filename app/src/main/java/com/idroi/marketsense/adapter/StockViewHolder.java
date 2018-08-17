package com.idroi.marketsense.adapter;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.viewholders.StockPredictionBlockViewHolder;

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

    TextView hitView;

    StockPredictionBlockViewHolder stockPredictionBlockViewHolder;

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
            stockViewHolder.hitView = view.findViewById(R.id.is_hit_tv);

            stockViewHolder.stockPredictionBlockViewHolder =
                    StockPredictionBlockViewHolder.convertToViewHolder(view);

            return stockViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
