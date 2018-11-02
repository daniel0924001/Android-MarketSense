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

    public View underline1M;
    public View underlineD;
    public View underlineW;
    public View underlineM;

    private TextView[] chartTypesTextView;
    private View[] underlineViews;

    static final ChartPeriodSelectorViewHolder EMPTY_VIEW_HOLDER = new ChartPeriodSelectorViewHolder();

    private ChartPeriodSelectorViewHolder() {

    }

    public static ChartPeriodSelectorViewHolder convertToViewHolder(final View view) {
        final ChartPeriodSelectorViewHolder viewHolder = new ChartPeriodSelectorViewHolder();
        try {
            viewHolder.chartType1M = view.findViewById(R.id.chart_type_1m);
            viewHolder.chartTypeD = view.findViewById(R.id.chart_type_d);
            viewHolder.chartTypeW = view.findViewById(R.id.chart_type_w);
            viewHolder.chartTypeM = view.findViewById(R.id.chart_type_m);

            viewHolder.underline1M = view.findViewById(R.id.chart_type_1m_underline);
            viewHolder.underlineD = view.findViewById(R.id.chart_type_d_underline);
            viewHolder.underlineW = view.findViewById(R.id.chart_type_w_underline);
            viewHolder.underlineM = view.findViewById(R.id.chart_type_m_underline);

            viewHolder.chartTypesTextView = new TextView[] {
                    viewHolder.chartType1M,
                    viewHolder.chartTypeD,
                    viewHolder.chartTypeW,
                    viewHolder.chartTypeM
            };

            viewHolder.underlineViews = new View[] {
                    viewHolder.underline1M,
                    viewHolder.underlineD,
                    viewHolder.underlineW,
                    viewHolder.underlineM
            };

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void setSelected(Context context, TextView selectedTextView, View selectUnderlineView) {
        for(TextView other : chartTypesTextView) {
            other.setTextColor(context.getResources().getColor(R.color.text_third));
        }
        for(View other : underlineViews) {
            other.setVisibility(View.GONE);
        }
        selectedTextView.setTextColor(context.getResources().getColor(R.color.text_first));
        selectUnderlineView.setVisibility(View.VISIBLE);
    }
}
