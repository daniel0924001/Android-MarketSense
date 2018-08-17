package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.MarketSenseRendererHelper;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by daniel.hsieh on 2018/8/15.
 */

public class ChartTickTopItemsViewHolder {

    public View mainView;
    public TextView lastRefreshTimeTextView;
    public TextView tradeVolumeTextView;

    static final ChartTickTopItemsViewHolder EMPTY_VIEW_HOLDER = new ChartTickTopItemsViewHolder();

    private ChartTickTopItemsViewHolder() {}

    public static ChartTickTopItemsViewHolder convertToViewHolder(final View view) {
        final ChartTickTopItemsViewHolder viewHolder = new ChartTickTopItemsViewHolder();
        try {
            viewHolder.mainView = view;
            viewHolder.lastRefreshTimeTextView = view.findViewById(R.id.last_refresh_time);
            viewHolder.tradeVolumeTextView = view.findViewById(R.id.stock_trade_volume);
            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public static void update(Context context, ChartTickTopItemsViewHolder viewHolder, String volume) {
        MarketSenseRendererHelper.addTextView(viewHolder.tradeVolumeTextView, volume);

        String format = context.getResources().getString(R.string.title_last_refresh_time);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
        MarketSenseRendererHelper.addTextView(
                        viewHolder.lastRefreshTimeTextView,
                        String.format(format,
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE)));
    }
}
