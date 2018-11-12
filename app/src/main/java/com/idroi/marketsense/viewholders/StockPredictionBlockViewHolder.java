package com.idroi.marketsense.viewholders;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/8/15.
 */

@Deprecated
public class StockPredictionBlockViewHolder {

    public ConstraintLayout techBlockView;
    public TextView techTitleTextView;
    public TextView techUnavailableTextView;
    public ImageView[] techImageViews;

    public ConstraintLayout newsBlockView;
    public TextView newsTitleTextView;
    public TextView newsUnavailableTextView;
    public ImageView[] newsImageViews;

    public ConstraintLayout todayBlock;
    public TextView todayTitleTextView;
    public TextView todayStatusTextView;

    public ConstraintLayout tomorrowBlock;
    public TextView tomorrowTitleTextView;
    public TextView tomorrowStatusTextView;

    static final StockPredictionBlockViewHolder EMPTY_VIEW_HOLDER = new StockPredictionBlockViewHolder();

    private StockPredictionBlockViewHolder() {}

    public static StockPredictionBlockViewHolder convertToViewHolder(final View view) {
        final StockPredictionBlockViewHolder viewHolder = new StockPredictionBlockViewHolder();
        try {
            // two bottom right blocks
            viewHolder.techBlockView = view.findViewById(R.id.tech_block);
            viewHolder.techTitleTextView = view.findViewById(R.id.tech_block_title);
            viewHolder.techUnavailableTextView = view.findViewById(R.id.tech_block_unavailable);
            viewHolder.techImageViews = new ImageView[]{
                    view.findViewById(R.id.tech_block_icon1),
                    view.findViewById(R.id.tech_block_icon2),
                    view.findViewById(R.id.tech_block_icon3)};

            viewHolder.newsBlockView = view.findViewById(R.id.news_block);
            viewHolder.newsTitleTextView = view.findViewById(R.id.news_block_title);
            viewHolder.newsUnavailableTextView = view.findViewById(R.id.news_block_unavailable);
            viewHolder.newsImageViews = new ImageView[]{
                    view.findViewById(R.id.news_block_icon1),
                    view.findViewById(R.id.news_block_icon2),
                    view.findViewById(R.id.news_block_icon3)};

            // today part
            viewHolder.todayBlock = view.findViewById(R.id.today_block);
            viewHolder.todayTitleTextView = view.findViewById(R.id.today_block_predict_title);
            viewHolder.todayStatusTextView = view.findViewById(R.id.today_block_predict_status);

            // tomorrow part
            viewHolder.tomorrowBlock = view.findViewById(R.id.tomorrow_block);
            viewHolder.tomorrowTitleTextView = view.findViewById(R.id.tomorrow_block_predict_title);
            viewHolder.tomorrowStatusTextView = view.findViewById(R.id.tomorrow_block_predict_status);

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
