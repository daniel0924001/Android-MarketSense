package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/4/19.
 */

public class NewsViewHolder {

    View mainView;
    TextView titleView;
    TextView dateView;
    TextView fireTextView;
    ImageView fireImageView;

    static final NewsViewHolder EMPTY_VIEW_HOLDER = new NewsViewHolder();

    private NewsViewHolder() {}

    static NewsViewHolder convertToViewHolder(final View view) {
        final NewsViewHolder newsViewHolder = new NewsViewHolder();
        newsViewHolder.mainView = view;
        try {
            newsViewHolder.titleView = view.findViewById(R.id.marketsense_news_title_tv);
            newsViewHolder.dateView = view.findViewById(R.id.marketsense_news_date_tv);
            newsViewHolder.fireTextView = view.findViewById(R.id.marketsense_news_fire_tv);
            newsViewHolder.fireImageView = view.findViewById(R.id.marketsense_news_fire_iv);
            return newsViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

}
