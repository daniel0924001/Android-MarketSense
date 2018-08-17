package com.idroi.marketsense.viewholders;

import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/8/15.
 */

public class ChartTaTopItemsViewHolder {

    public View mainView;
    public TextView tradeDateTextView;
    public TextView closePriceTextView;
    public TextView diffTextView;
    public TextView tradeVolumeTextView;

    static final ChartTaTopItemsViewHolder EMPTY_VIEW_HOLDER = new ChartTaTopItemsViewHolder();

    private ChartTaTopItemsViewHolder() {}

    public static ChartTaTopItemsViewHolder convertToViewHolder(final View view) {
        final ChartTaTopItemsViewHolder viewHolder = new ChartTaTopItemsViewHolder();
        try {
            viewHolder.mainView = view;
            viewHolder.tradeDateTextView = view.findViewById(R.id.chart_ta_trade_date);
            viewHolder.closePriceTextView = view.findViewById(R.id.stock_trade_close_price);
            viewHolder.diffTextView = view.findViewById(R.id.stock_trade_diff);
            viewHolder.tradeVolumeTextView = view.findViewById(R.id.stock_trade_volume);

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
