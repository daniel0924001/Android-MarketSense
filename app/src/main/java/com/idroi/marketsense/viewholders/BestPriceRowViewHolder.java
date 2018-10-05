package com.idroi.marketsense.viewholders;

import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/8/20.
 */

public class BestPriceRowViewHolder {
    TextView buyPrice;
    TextView buyVolume;
    TextView sellPrice;
    TextView sellVolume;

    View burBar;

    View buyColorBar;
    View sellColorBar;

    static final BestPriceRowViewHolder EMPTY_VIEW_HOLDER = new BestPriceRowViewHolder();

    private BestPriceRowViewHolder() {}

    public static BestPriceRowViewHolder convertToViewHolder(final View view) {
        final BestPriceRowViewHolder viewHolder = new BestPriceRowViewHolder();
        try {
            viewHolder.buyPrice = view.findViewById(R.id.buy_price);
            viewHolder.buyVolume = view.findViewById(R.id.buy_volume);
            viewHolder.sellPrice = view.findViewById(R.id.sell_price);
            viewHolder.sellVolume = view.findViewById(R.id.sell_volume);
            viewHolder.burBar = view.findViewById(R.id.buy_bar);
            viewHolder.buyColorBar = view.findViewById(R.id.buy_bar_color);
            viewHolder.sellColorBar = view.findViewById(R.id.sell_bar_color);

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
