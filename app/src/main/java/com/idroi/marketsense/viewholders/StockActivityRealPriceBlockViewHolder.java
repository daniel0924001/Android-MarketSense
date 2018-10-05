package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.MarketSenseRendererHelper;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by daniel.hsieh on 2018/8/13.
 */

public class StockActivityRealPriceBlockViewHolder {

    private ViewGroup container;
    public View mainView;

    public StockActivityBottomSelector stockActivityBottomSelector;

    public TextView priceTextView;
    public TextView diffTextView;

    public TextView tradingTextView;
    public TextView timeTextView;

    private AutoTransition autoTransition;

    static final StockActivityRealPriceBlockViewHolder EMPTY_VIEW_HOLDER = new StockActivityRealPriceBlockViewHolder();

    private StockActivityRealPriceBlockViewHolder() {

    }

    public static StockActivityRealPriceBlockViewHolder convertToViewHolder(final View view) {
        final StockActivityRealPriceBlockViewHolder viewHolder = new StockActivityRealPriceBlockViewHolder();
        try {
            viewHolder.mainView = view;
            viewHolder.container = (ViewGroup) view;
            viewHolder.priceTextView = view.findViewById(R.id.stock_price_tv);
            viewHolder.diffTextView = view.findViewById(R.id.stock_diff_tv);
            viewHolder.tradingTextView = view.findViewById(R.id.stock_trade_now_tv);
            viewHolder.timeTextView = view.findViewById(R.id.stock_time_tv);

            viewHolder.stockActivityBottomSelector = StockActivityBottomSelector
                    .convertToViewHolder(view.findViewById(R.id.bottom_content_selector));

            viewHolder.autoTransition = new AutoTransition();
            viewHolder.autoTransition.setDuration(50);

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public static void update(Context context, StockActivityRealPriceBlockViewHolder viewHolder,
                              float price, float diffNum, float diffPercentage) {
        String diffNumberString = null;
        String diffPercentageString = null;
        if(diffPercentage > 0) {
            diffNumberString = String.format(Locale.US, "+%s", diffNum);
            diffPercentageString = String.format(Locale.US, "+%.2f%%", diffPercentage);
        } else {
            diffNumberString = String.format(Locale.US, "%s", diffNum);
            diffPercentageString = String.format(Locale.US, "%.2f%%", diffPercentage);
        }
        update(context, viewHolder,
                String.valueOf(price),
                diffNumberString,
                diffPercentageString);
    }

    public static void update(Context context, StockActivityRealPriceBlockViewHolder viewHolder,
                       String price, String diffNum, String diffPercentage) {
        Resources resources = context.getResources();
        String format = resources.getString(R.string.title_diff_format);

        MarketSenseRendererHelper.addTextView(viewHolder.priceTextView, price);

        try {
            float diffPercentageFloat = Float.valueOf(diffNum);
            if(diffPercentageFloat > 0) {
                MarketSenseRendererHelper.setBackgroundColor(viewHolder.mainView, R.color.trend_red);
                MarketSenseRendererHelper.addTextViewWithIcon(
                        viewHolder.diffTextView,
                        String.format(format, diffNum, diffPercentage),
                        R.mipmap.ic_trend_arrow_up_white);
            } else if(diffPercentageFloat < 0) {
                MarketSenseRendererHelper.setBackgroundColor(viewHolder.mainView, R.color.trend_green);
                MarketSenseRendererHelper.addTextViewWithIcon(
                        viewHolder.diffTextView,
                        String.format(format, diffNum, diffPercentage),
                        R.mipmap.ic_trend_arrow_down_white);
            } else {
                MarketSenseRendererHelper.setBackgroundColor(viewHolder.mainView, R.color.draw_grey);
                MarketSenseRendererHelper.addTextViewWithIcon(
                        viewHolder.diffTextView,
                        String.format(format, diffNum, diffPercentage),
                        0);
            }
        } catch (NumberFormatException e) {
            MarketSenseRendererHelper.setBackgroundColor(viewHolder.mainView, R.color.draw_grey);
            MarketSenseRendererHelper.addTextViewWithIcon(
                    viewHolder.diffTextView,
                    String.format(format, diffNum, diffPercentage),
                    0);
            MSLog.e("NumberFormatException: " + diffNum);
        }

        if(ClientData.getInstance().isWorkDayAndStockMarketIsOpen()) {
            MarketSenseRendererHelper.addTextView(viewHolder.tradingTextView, context.getString(R.string.title_trade_now));
            viewHolder.tradingTextView.setBackground(context.getDrawable(R.drawable.btn_oval_small_corner_yellow));
        } else {
            MarketSenseRendererHelper.addTextView(viewHolder.tradingTextView, context.getString(R.string.title_not_trade_now));
            viewHolder.tradingTextView.setBackgroundResource(android.R.color.transparent);
        }

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
        String timeFormat = resources.getString(R.string.title_date_time_format);
        MarketSenseRendererHelper.addTextView(
                viewHolder.timeTextView,
                String.format(timeFormat,
                        c.get(Calendar.MONTH) + 1,
                        c.get(Calendar.DAY_OF_MONTH),
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE)));
    }

    public void shrink() {
        TransitionManager.beginDelayedTransition(container, autoTransition);

        float density = ClientData.getInstance().getScreenDensity();
        priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        ConstraintLayout.LayoutParams priceLayoutParams =
                (ConstraintLayout.LayoutParams) priceTextView.getLayoutParams();
        priceLayoutParams.setMargins(0, 0, 0, 0);
        priceLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        priceLayoutParams.topToTop = diffTextView.getId();
        priceLayoutParams.bottomToBottom = diffTextView.getId();
        priceTextView.setLayoutParams(priceLayoutParams);

        diffTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        ConstraintLayout.LayoutParams diffLayoutParams =
                (ConstraintLayout.LayoutParams) diffTextView.getLayoutParams();
        diffLayoutParams.setMargins(18, 0, 0, (int) (density * 13));
        diffLayoutParams.setMarginStart((int) (18 * density));
        diffLayoutParams.startToEnd = priceTextView.getId();
        diffLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        diffLayoutParams.topToBottom = ConstraintLayout.LayoutParams.UNSET;
        diffLayoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
        diffLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        diffTextView.setLayoutParams(diffLayoutParams);
    }

    public void expand() {
        TransitionManager.beginDelayedTransition(container, autoTransition);

        float density = ClientData.getInstance().getScreenDensity();
        priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        ConstraintLayout.LayoutParams priceLayoutParams =
                (ConstraintLayout.LayoutParams) priceTextView.getLayoutParams();
        priceLayoutParams.setMargins((int) (density * 16), 0, 0, 0);
        priceLayoutParams.setMarginStart((int) (density * 16));
        priceLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        priceLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        priceLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        priceTextView.setLayoutParams(priceLayoutParams);

        diffTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        ConstraintLayout.LayoutParams diffLayoutParams =
                (ConstraintLayout.LayoutParams) diffTextView.getLayoutParams();
        diffLayoutParams.setMargins(0, (int) (density * 8), 0, (int) (density * 15));
        diffLayoutParams.setMarginStart(0);
        diffLayoutParams.startToEnd = ConstraintLayout.LayoutParams.UNSET;
        diffLayoutParams.topToTop = ConstraintLayout.LayoutParams.UNSET;
        diffLayoutParams.topToBottom = priceTextView.getId();
        diffLayoutParams.startToStart = priceTextView.getId();
        diffLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        diffTextView.setLayoutParams(diffLayoutParams);
    }

    public void showSelector() {
        stockActivityBottomSelector.setVisibility(View.VISIBLE);
    }

    public void hideSelector() {
        // in order to address the problem when the hideSelector is triggered by click comment
        // and automatically hide it.
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                stockActivityBottomSelector.setVisibility(View.GONE);
            }
        });
    }
}
