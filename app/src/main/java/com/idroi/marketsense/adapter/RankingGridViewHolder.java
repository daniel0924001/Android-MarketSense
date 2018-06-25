package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/6/25.
 */

public class RankingGridViewHolder {

    View mainView;
    TextView nameView;
    TextView priceView;
    ImageView iconView;

    static final RankingGridViewHolder EMPTY_VIEW_HOLDER = new RankingGridViewHolder();

    private RankingGridViewHolder() {}

    static RankingGridViewHolder convertToViewHolder(final View view) {
        final RankingGridViewHolder rankingGridViewHolder = new RankingGridViewHolder();
        rankingGridViewHolder.mainView = view;
        try {
            rankingGridViewHolder.nameView = view.findViewById(R.id.rank_name_tv);
            rankingGridViewHolder.priceView = view.findViewById(R.id.rank_price_tv);
            rankingGridViewHolder.iconView = view.findViewById(R.id.rank_iv);
            return rankingGridViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
