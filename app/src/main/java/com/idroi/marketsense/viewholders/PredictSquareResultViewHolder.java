package com.idroi.marketsense.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.Stock;

/**
 * Created by daniel.hsieh on 2018/8/20.
 */

public class PredictSquareResultViewHolder {

    TextView title;
    ImageView iconResult;

    static final PredictSquareResultViewHolder EMPTY_VIEW_HOLDER = new PredictSquareResultViewHolder();

    private PredictSquareResultViewHolder() {}

    public static PredictSquareResultViewHolder convertToViewHolder(final View view, int stringResourceId) {
        final PredictSquareResultViewHolder viewHolder = new PredictSquareResultViewHolder();
        try {
            viewHolder.title = view.findViewById(R.id.title);
            viewHolder.iconResult = view.findViewById(R.id.result_icon);

            MarketSenseRendererHelper
                    .addTextView(viewHolder.title, view.getContext().getString(stringResourceId));

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    static void setIconResult(ImageView iconResult, int direction) {

        if(direction == Stock.TREND_DOWN) {
            iconResult.setImageResource(R.mipmap.ic_direction_down);
        } else if(direction == Stock.TREND_UP){
            iconResult.setImageResource(R.mipmap.ic_direction_up);
        } else {
            iconResult.setImageResource(R.mipmap.ic_trend_draw);
        }

    }
}
