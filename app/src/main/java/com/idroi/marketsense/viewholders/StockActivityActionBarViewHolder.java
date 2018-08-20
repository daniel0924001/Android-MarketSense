package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.MarketSenseRendererHelper;

/**
 * Created by daniel.hsieh on 2018/8/13.
 */

public class StockActivityActionBarViewHolder {

    public ActionBar actionBar;

    public ImageView backImageView;
    public TextView titleTextView;

    public ImageView favoriteImageView;

    public ImageView moreImageView;

    static final StockActivityActionBarViewHolder EMPTY_VIEW_HOLDER = new StockActivityActionBarViewHolder();

    private StockActivityActionBarViewHolder() {

    }

    public static StockActivityActionBarViewHolder convertToViewHolder(final View view, ActionBar actionBar) {
        final StockActivityActionBarViewHolder stockActivityActionBarViewHolder = new StockActivityActionBarViewHolder();
        try {
            stockActivityActionBarViewHolder.actionBar = actionBar;
            stockActivityActionBarViewHolder.backImageView = view.findViewById(R.id.action_bar_back);
            stockActivityActionBarViewHolder.titleTextView = view.findViewById(R.id.action_bar_title);
            stockActivityActionBarViewHolder.favoriteImageView = view.findViewById(R.id.action_bar_favorite);
            stockActivityActionBarViewHolder.moreImageView = view.findViewById(R.id.action_bar_more);
            return stockActivityActionBarViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public static void update(Context context, StockActivityActionBarViewHolder viewHolder, float diffNum) {
        update(context, viewHolder, String.valueOf(diffNum));
    }

    public static void update(Context context, StockActivityActionBarViewHolder viewHolder, String diffNum) {
        Resources resources = context.getResources();
        try {
            float diffPercentageFloat = Float.valueOf(diffNum);
            if(diffPercentageFloat > 0) {
                viewHolder.actionBar.setBackgroundDrawable(new ColorDrawable(resources.getColor(R.color.grapefruit_four)));
            } else if(diffPercentageFloat < 0) {
                viewHolder.actionBar.setBackgroundDrawable(new ColorDrawable(resources.getColor(R.color.green_blue)));
            } else {
                viewHolder.actionBar.setBackgroundDrawable(new ColorDrawable(resources.getColor(R.color.cloudy_blue)));
            }
        } catch (NumberFormatException e) {
            viewHolder.actionBar.setBackgroundDrawable(new ColorDrawable(resources.getColor(R.color.cloudy_blue)));
            MSLog.e("NumberFormatException: " + diffNum);
        }
    }
}
