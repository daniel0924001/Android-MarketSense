package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/7/29.
 */

public class RelatedStockNameViewHolder {

    TextView titleView;
    TextView diffView;

    static final RelatedStockNameViewHolder EMPTY_VIEW_HOLDER = new RelatedStockNameViewHolder();

    private RelatedStockNameViewHolder() {}

    static RelatedStockNameViewHolder convertToViewHolder(final View view) {
        final RelatedStockNameViewHolder relatedStockNameViewHolder = new RelatedStockNameViewHolder();
        try {
            relatedStockNameViewHolder.titleView = view.findViewById(R.id.related_stock_name);
            relatedStockNameViewHolder.diffView = view.findViewById(R.id.related_stock_diff);
            return relatedStockNameViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
