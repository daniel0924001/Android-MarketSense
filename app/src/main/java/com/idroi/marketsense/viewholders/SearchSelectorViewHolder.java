package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/11/6.
 */

public class SearchSelectorViewHolder {

    public View mainView;

    public TextView stockSelector;
    public TextView newsSelector;
    public TextView commentSelector;

    public View stockUnderline;
    public View newsUnderline;
    public View commentUnderline;

    private TextView[] selectors;
    private View[] underlines;
    private RecyclerView[] recyclerViews;

    static final SearchSelectorViewHolder EMPTY_VIEW_HOLDER = new SearchSelectorViewHolder();

    private SearchSelectorViewHolder() {}

    public static SearchSelectorViewHolder convertToViewHolder(final View view,
                                                               final RecyclerView stockRecyclerView,
                                                               final RecyclerView newsRecyclerView,
                                                               final RecyclerView commentRecyclerView) {
        final SearchSelectorViewHolder viewHolder = new SearchSelectorViewHolder();
        try {
            viewHolder.mainView = view;
            viewHolder.stockSelector = view.findViewById(R.id.selector_stock_block);
            viewHolder.newsSelector = view.findViewById(R.id.selector_news_block);
            viewHolder.commentSelector = view.findViewById(R.id.selector_comment_block);

            viewHolder.stockSelector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MSLog.d("stock click");
                    viewHolder.setSelected(view.getContext(),
                            viewHolder.stockSelector,
                            viewHolder.stockUnderline,
                            stockRecyclerView);
                }
            });

            viewHolder.newsSelector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.setSelected(view.getContext(),
                            viewHolder.newsSelector,
                            viewHolder.newsUnderline,
                            newsRecyclerView);
                }
            });

            viewHolder.commentSelector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.setSelected(view.getContext(),
                            viewHolder.commentSelector,
                            viewHolder.commentUnderline,
                            commentRecyclerView);
                }
            });

            viewHolder.stockUnderline = view.findViewById(R.id.stock_block_underline);
            viewHolder.newsUnderline = view.findViewById(R.id.news_block_underline);
            viewHolder.commentUnderline = view.findViewById(R.id.comment_block_underline);

            viewHolder.selectors = new TextView[] {
                    viewHolder.stockSelector,
                    viewHolder.newsSelector,
                    viewHolder.commentSelector
            };
            viewHolder.underlines = new View[] {
                    viewHolder.stockUnderline,
                    viewHolder.newsUnderline,
                    viewHolder.commentUnderline
            };
            viewHolder.recyclerViews = new RecyclerView[] {
                    stockRecyclerView,
                    newsRecyclerView,
                    commentRecyclerView
            };

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    void setSelected(Context context, TextView selected, View underline, RecyclerView contentRecyclerView) {
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

        for(RecyclerView other : recyclerViews) {
            if(other != contentRecyclerView) {
                other.setVisibility(View.GONE);
            } else {
                other.setVisibility(View.VISIBLE);
            }
        }
    }

    public void show() {
        mainView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mainView.setVisibility(View.GONE);
    }
}
