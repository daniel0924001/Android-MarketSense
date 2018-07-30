package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/7/23.
 */

public class CriticalStatisticsTitleViewHolder {

    TextView titleTextView;

    static final CriticalStatisticsTitleViewHolder EMPTY_VIEW_HOLDER = new CriticalStatisticsTitleViewHolder();

    private CriticalStatisticsTitleViewHolder() {

    }

    static CriticalStatisticsTitleViewHolder convertToViewHolder(final View view) {
        final CriticalStatisticsTitleViewHolder viewHolder = new CriticalStatisticsTitleViewHolder();
        try {
            viewHolder.titleTextView = view.findViewById(R.id.statistics_key);
            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
