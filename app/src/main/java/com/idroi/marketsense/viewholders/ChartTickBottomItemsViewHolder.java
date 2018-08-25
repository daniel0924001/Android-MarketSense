package com.idroi.marketsense.viewholders;

import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/8/15.
 */

public class ChartTickBottomItemsViewHolder {

    public View mainView;
    public TextView openTextView;
    public TextView yesterdayCloseTextView;
    public TextView highTextView;
    public TextView lowTextView;

    public TextView buyTextView;
    public TextView sellTextView;
    public TextView moneyTextView;
    public TextView yesterdayVolumeTextView;

    private Group secondRowGroup;

    static final ChartTickBottomItemsViewHolder EMPTY_VIEW_HOLDER = new ChartTickBottomItemsViewHolder();

    private ChartTickBottomItemsViewHolder() {}

    public static ChartTickBottomItemsViewHolder convertToViewHolder(final View view) {
        final ChartTickBottomItemsViewHolder viewHolder = new ChartTickBottomItemsViewHolder();
        try {
            viewHolder.openTextView = view.findViewById(R.id.top_1_1_value);
            viewHolder.yesterdayCloseTextView = view.findViewById(R.id.top_1_2_value);
            viewHolder.highTextView = view.findViewById(R.id.top_1_3_value);
            viewHolder.lowTextView = view.findViewById(R.id.top_1_4_value);

            viewHolder.buyTextView = view.findViewById(R.id.top_2_1_value);
            viewHolder.sellTextView = view.findViewById(R.id.top_2_2_value);
            viewHolder.moneyTextView = view.findViewById(R.id.top_2_3_value);
            viewHolder.yesterdayVolumeTextView = view.findViewById(R.id.top_2_4_value);

            viewHolder.secondRowGroup = view.findViewById(R.id.second_row_group);

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void showSecondRow() {
        secondRowGroup.setVisibility(View.VISIBLE);
    }

    public void hideSecondRow() {
        secondRowGroup.setVisibility(View.GONE);
    }
}
