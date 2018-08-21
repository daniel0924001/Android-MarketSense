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

public class StockActivityBottomSelector {

    public TextView newsSelector;
    public TextView commentSelector;

    private TextView[] selectors;

    public ConstraintLayout newsBlock, commentBlock;

    private ConstraintLayout[] blocks;

    static final StockActivityBottomSelector EMPTY_VIEW_HOLDER = new StockActivityBottomSelector();

    private StockActivityBottomSelector() {}

    public static StockActivityBottomSelector convertToViewHolder(final View view) {
        final StockActivityBottomSelector stockActivityBottomSelector = new StockActivityBottomSelector();
        try {
            stockActivityBottomSelector.newsSelector = view.findViewById(R.id.selector_news_block);
            stockActivityBottomSelector.commentSelector = view.findViewById(R.id.selector_comment_block);

            stockActivityBottomSelector.selectors = new TextView[] {
                    stockActivityBottomSelector.newsSelector,
                    stockActivityBottomSelector.commentSelector
            };

            stockActivityBottomSelector.newsBlock = view.findViewById(R.id.marketsense_stock_news);
            stockActivityBottomSelector.commentBlock = view.findViewById(R.id.marketsense_stock_comment);

            stockActivityBottomSelector.blocks = new ConstraintLayout[] {
                    stockActivityBottomSelector.newsBlock,
                    stockActivityBottomSelector.commentBlock
            };

            return stockActivityBottomSelector;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void setSelected(Context context, TextView selected, ConstraintLayout selectedBlock) {
        for(TextView other : selectors) {
            if(other != selected) {
                other.setBackground(context.getDrawable(R.drawable.border_selector));
                other.setTextColor(context.getResources().getColor(R.color.text_black));
            } else {
                other.setBackground(context.getDrawable(R.drawable.border_selector_selected));
                other.setTextColor(context.getResources().getColor(R.color.grapefruit_four));
            }
        }

        for(ConstraintLayout other : blocks) {
            if(other != selectedBlock) {
                other.setVisibility(View.GONE);
            } else {
                other.setVisibility(View.VISIBLE);
            }
        }
    }
}
