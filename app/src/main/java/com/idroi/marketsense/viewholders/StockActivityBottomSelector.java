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

    public View newsUnderline;
    public View commentUnderline;

    private TextView[] selectors;
    private View[] underlines;

    static final StockActivityBottomSelector EMPTY_VIEW_HOLDER = new StockActivityBottomSelector();

    private StockActivityBottomSelector() {}

    public static StockActivityBottomSelector convertToViewHolder(final View view) {
        final StockActivityBottomSelector viewHolder = new StockActivityBottomSelector();
        try {
            viewHolder.mainView = view;
            viewHolder.newsSelector = view.findViewById(R.id.selector_news_block);
            viewHolder.commentSelector = view.findViewById(R.id.selector_comment_block);
            viewHolder.newsUnderline = view.findViewById(R.id.news_block_underline);
            viewHolder.commentUnderline = view.findViewById(R.id.comment_block_underline);

            viewHolder.selectors = new TextView[] {
                    viewHolder.newsSelector,
                    viewHolder.commentSelector
            };
            viewHolder.underlines = new View[] {
                    viewHolder.newsUnderline,
                    viewHolder.commentUnderline
            };

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    void setSelected(Context context, TextView selected, View underline) {
        for(TextView other : selectors) {
            if(other != selected) {
                other.setTextColor(context.getResources().getColor(R.color.text_third));
            } else {
                other.setTextColor(context.getResources().getColor(R.color.text_first));
            }
        }

        for(View other : underlines) {
            if(other != underline) {
                other.setVisibility(View.GONE);
            } else {
                other.setVisibility(View.VISIBLE);
            }
        }
    }

    void setVisibility(int visibility) {
        mainView.setVisibility(visibility);
    }
}
