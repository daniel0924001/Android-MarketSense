package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/8/21.
 */

public class StockActivityBottomContent {

    public StockActivityBottomSelector stockActivityBottomSelector;

    public ConstraintLayout newsBlock, commentBlock;

    private ConstraintLayout[] blocks;

    static final StockActivityBottomContent EMPTY_VIEW_HOLDER = new StockActivityBottomContent();

    private StockActivityBottomContent() {}

    public static StockActivityBottomContent convertToViewHolder(final View view) {
        final StockActivityBottomContent stockActivityBottomContent = new StockActivityBottomContent();
        try {

            stockActivityBottomContent.stockActivityBottomSelector = StockActivityBottomSelector
                            .convertToViewHolder(view.findViewById(R.id.bottom_content_selector));

            stockActivityBottomContent.newsBlock = view.findViewById(R.id.marketsense_stock_news);
            stockActivityBottomContent.commentBlock = view.findViewById(R.id.marketsense_stock_comment);

            stockActivityBottomContent.blocks = new ConstraintLayout[] {
                    stockActivityBottomContent.newsBlock,
                    stockActivityBottomContent.commentBlock
            };

            return stockActivityBottomContent;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void setSelected(Context context, TextView selected, View underline, ConstraintLayout selectedBlock) {

        stockActivityBottomSelector.setSelected(context, selected, underline);

        for(ConstraintLayout other : blocks) {
            if(other != selectedBlock) {
                other.setVisibility(View.GONE);
            } else {
                other.setVisibility(View.VISIBLE);
            }
        }
    }
}
