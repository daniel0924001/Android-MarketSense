package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/7/23.
 */

public class CriticalStatistics5ColumnsViewHolder {

    TextView titleTextView;
    TextView value1TextView;
    TextView value2TextView;
    TextView value3TextView;
    TextView value4TextView;

    static final CriticalStatistics5ColumnsViewHolder EMPTY_VIEW_HOLDER = new CriticalStatistics5ColumnsViewHolder();

    private CriticalStatistics5ColumnsViewHolder() {

    }

    static CriticalStatistics5ColumnsViewHolder convertToViewHolder(final View view) {
        final CriticalStatistics5ColumnsViewHolder viewHolder = new CriticalStatistics5ColumnsViewHolder();
        try {
            viewHolder.titleTextView = view.findViewById(R.id.statistics_key);
            viewHolder.value1TextView = view.findViewById(R.id.statistics_value1);
            viewHolder.value2TextView = view.findViewById(R.id.statistics_value2);
            viewHolder.value3TextView = view.findViewById(R.id.statistics_value3);
            viewHolder.value4TextView = view.findViewById(R.id.statistics_value4);
            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
