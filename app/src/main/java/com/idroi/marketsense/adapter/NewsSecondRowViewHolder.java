package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/5/16.
 */

public class NewsSecondRowViewHolder {

    TextView leftTitleView;
    TextView rightTitleView;

    SimpleDraweeView leftMainImageView;
    SimpleDraweeView rightMainImageView;

    TextView leftDateView;
    TextView rightDateView;

    ImageView leftAlarmImageView;
    ImageView rightAlarmImageView;

    static final NewsSecondRowViewHolder EMPTY_VIEW_HOLDER = new NewsSecondRowViewHolder();

    private NewsSecondRowViewHolder() {}

    static NewsSecondRowViewHolder convertToViewHolder(final View view) {
        final NewsSecondRowViewHolder newsSecondRowViewHolder = new NewsSecondRowViewHolder();
        try {
            newsSecondRowViewHolder.leftTitleView =
                    view.findViewById(R.id.marketsense_news_left_title_tv);
            newsSecondRowViewHolder.rightTitleView =
                    view.findViewById(R.id.marketsense_news_right_title_tv);
            newsSecondRowViewHolder.leftDateView =
                    view.findViewById(R.id.marketsense_news_left_date_tv);
            newsSecondRowViewHolder.rightDateView =
                    view.findViewById(R.id.marketsense_news_right_date_tv);
            newsSecondRowViewHolder.leftMainImageView =
                    view.findViewById(R.id.marketsense_news_left_image_iv);
            newsSecondRowViewHolder.rightMainImageView =
                    view.findViewById(R.id.marketsense_news_right_image_iv);
            newsSecondRowViewHolder.leftAlarmImageView =
                    view.findViewById(R.id.marketsense_news_left_alarm);
            newsSecondRowViewHolder.rightAlarmImageView =
                    view.findViewById(R.id.marketsense_news_right_alarm);
            return newsSecondRowViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
