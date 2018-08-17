package com.idroi.marketsense.viewholders;

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

    public View buyBlock, sellBlock, moneyBlock, yesterdayVolumeBlock;

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

            viewHolder.buyBlock = view.findViewById(R.id.top_2_1_block);
            viewHolder.sellBlock = view.findViewById(R.id.top_2_2_block);
            viewHolder.moneyBlock = view.findViewById(R.id.top_2_3_block);
            viewHolder.yesterdayVolumeBlock = view.findViewById(R.id.top_2_4_block);

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void showSecondRow() {
        buyBlock.setVisibility(View.VISIBLE);
        sellBlock.setVisibility(View.VISIBLE);
        moneyBlock.setVisibility(View.VISIBLE);
        yesterdayVolumeBlock.setVisibility(View.VISIBLE);
    }

    public void hideSecondRow() {
        buyBlock.setVisibility(View.GONE);
        sellBlock.setVisibility(View.GONE);
        moneyBlock.setVisibility(View.GONE);
        yesterdayVolumeBlock.setVisibility(View.GONE);
    }
}
