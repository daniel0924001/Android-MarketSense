package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/5/16.
 */

public class NewsFirstRowViewHolder {

    View mainView;
    TextView titleView;
    SimpleDraweeView mainImageView;

    static final NewsFirstRowViewHolder EMPTY_VIEW_HOLDER = new NewsFirstRowViewHolder();

    private NewsFirstRowViewHolder() {};

    static NewsFirstRowViewHolder convertToViewHolder(final View view) {
        final NewsFirstRowViewHolder newsFirstRowViewHolder = new NewsFirstRowViewHolder();
        newsFirstRowViewHolder.mainView = view;
        try {
            newsFirstRowViewHolder.titleView = view.findViewById(R.id.marketsense_news_title_tv);
            newsFirstRowViewHolder.mainImageView = view.findViewById(R.id.marketsense_news_image_iv);
            return newsFirstRowViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
