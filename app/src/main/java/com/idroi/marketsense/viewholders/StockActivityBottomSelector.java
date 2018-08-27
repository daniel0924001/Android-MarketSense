package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/8/27.
 */

public class StockActivityBottomSelector {

    public View mainView;
    public TextView newsSelector;
    public TextView commentSelector;

    private TextView[] selectors;

    static final StockActivityBottomSelector EMPTY_VIEW_HOLDER = new StockActivityBottomSelector();

    private StockActivityBottomSelector() {}

    public static StockActivityBottomSelector convertToViewHolder(final View view) {
        final StockActivityBottomSelector stockActivityBottomSelector = new StockActivityBottomSelector();
        try {
            stockActivityBottomSelector.mainView = view;
            stockActivityBottomSelector.newsSelector = view.findViewById(R.id.selector_news_block);
            stockActivityBottomSelector.commentSelector = view.findViewById(R.id.selector_comment_block);

            stockActivityBottomSelector.selectors = new TextView[] {
                    stockActivityBottomSelector.newsSelector,
                    stockActivityBottomSelector.commentSelector
            };

            return stockActivityBottomSelector;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    void setSelected(Context context, TextView selected) {
        for(TextView other : selectors) {
            if(other != selected) {
                other.setBackground(context.getDrawable(R.drawable.border_selector));
                other.setTextColor(context.getResources().getColor(R.color.text_black));
            } else {
                other.setBackground(context.getDrawable(R.drawable.border_selector_selected));
                other.setTextColor(context.getResources().getColor(R.color.trend_red));
            }
        }
    }

    void setVisibility(int visibility) {
        mainView.setVisibility(visibility);
    }
}
