package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/8/15.
 */

public class ChartPeriodSelectorViewHolder {

    public TextView chartType1M;
    public TextView chartTypeD;
    public TextView chartTypeW;
    public TextView chartTypeM;
    private TextView[] chartTypesTextView;

    static final ChartPeriodSelectorViewHolder EMPTY_VIEW_HOLDER = new ChartPeriodSelectorViewHolder();

    private ChartPeriodSelectorViewHolder() {

    }

    public static ChartPeriodSelectorViewHolder convertToViewHolder(final View view) {
        final ChartPeriodSelectorViewHolder chartPeriodSelectorViewHolder = new ChartPeriodSelectorViewHolder();
        try {
            chartPeriodSelectorViewHolder.chartType1M = view.findViewById(R.id.chart_type_1m);
            chartPeriodSelectorViewHolder.chartTypeD = view.findViewById(R.id.chart_type_d);
            chartPeriodSelectorViewHolder.chartTypeW = view.findViewById(R.id.chart_type_w);
            chartPeriodSelectorViewHolder.chartTypeM = view.findViewById(R.id.chart_type_m);

            chartPeriodSelectorViewHolder.chartTypesTextView = new TextView[] {
                    chartPeriodSelectorViewHolder.chartType1M,
                    chartPeriodSelectorViewHolder.chartTypeD,
                    chartPeriodSelectorViewHolder.chartTypeW,
                    chartPeriodSelectorViewHolder.chartTypeM
            };

            return chartPeriodSelectorViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void setSelected(Context context, TextView selectedTextView) {
        for(TextView other : chartTypesTextView) {
            other.setTextColor(context.getResources().getColor(R.color.text_black));
            other.setBackground(context.getDrawable(R.drawable.border_selector));
        }
        selectedTextView.setTextColor(context.getResources().getColor(R.color.trend_red));
        selectedTextView.setBackground(context.getDrawable(R.drawable.border_selector_selected));
    }
}
