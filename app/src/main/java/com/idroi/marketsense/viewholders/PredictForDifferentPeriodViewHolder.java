package com.idroi.marketsense.viewholders;

import android.view.View;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/8/20.
 */

@Deprecated
public class PredictForDifferentPeriodViewHolder {

    public PredictSquareResultViewHolder shortPrediction;
    public PredictSquareResultViewHolder middlePrediction;
    public PredictSquareResultViewHolder longPrediction;

    static final PredictForDifferentPeriodViewHolder EMPTY_VIEW_HOLDER = new PredictForDifferentPeriodViewHolder();

    private PredictForDifferentPeriodViewHolder() {}

    public static PredictForDifferentPeriodViewHolder convertToViewHolder(final View view) {
        final PredictForDifferentPeriodViewHolder viewHolder = new PredictForDifferentPeriodViewHolder();
        try {
            viewHolder.shortPrediction = PredictSquareResultViewHolder
                    .convertToViewHolder(view.findViewById(R.id.period_1d), R.string.title_predict_1d);
            viewHolder.middlePrediction = PredictSquareResultViewHolder
                    .convertToViewHolder(view.findViewById(R.id.period_5d), R.string.title_predict_5d);
            viewHolder.longPrediction = PredictSquareResultViewHolder
                    .convertToViewHolder(view.findViewById(R.id.period_20d), R.string.title_predict_20d);

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public static void update(PredictForDifferentPeriodViewHolder viewHolder,
                              int shortDirection,
                              int middleDirection,
                              int longDirection) {
        PredictSquareResultViewHolder
                .setIconResult(viewHolder.shortPrediction.iconResult, shortDirection);
        PredictSquareResultViewHolder
                .setIconResult(viewHolder.middlePrediction.iconResult, middleDirection);
        PredictSquareResultViewHolder
                .setIconResult(viewHolder.longPrediction.iconResult, longDirection);
    }
}
