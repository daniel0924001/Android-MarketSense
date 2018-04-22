package com.idroi.marketsense.adapter;

import android.util.Log;
import android.view.View;
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
    SimpleDraweeView mainImageView;

    static final NewsViewHolder EMPTY_VIEW_HOLDER = new NewsViewHolder();

    private NewsViewHolder() {}

    static NewsViewHolder convertToViewHolder(final View view) {
        final NewsViewHolder newsViewHolder = new NewsViewHolder();
        newsViewHolder.mainView = view;
        try {
            newsViewHolder.titleView = view.findViewById(R.id.infohub_news_title_tv);
            newsViewHolder.mainImageView = view.findViewById(R.id.infohub_news_image_iv);
            return newsViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

}
