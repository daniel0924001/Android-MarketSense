package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/7/23.
 */

public class CriticalStatisticsNormalViewHolder {

    TextView titleTextView;
    TextView valueTextView;

    static final CriticalStatisticsNormalViewHolder EMPTY_VIEW_HOLDER = new CriticalStatisticsNormalViewHolder();

    private CriticalStatisticsNormalViewHolder() {

    }

    static CriticalStatisticsNormalViewHolder convertToViewHolder(final View view) {
        final CriticalStatisticsNormalViewHolder viewHolder = new CriticalStatisticsNormalViewHolder();
        try {
            viewHolder.titleTextView = view.findViewById(R.id.statistics_key);
            viewHolder.valueTextView = view.findViewById(R.id.statistics_value);
            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
