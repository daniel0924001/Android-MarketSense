package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
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

    public View mainView;

    public TextView priceTextView;
    public TextView diffTextView;

    public TextView tradingTextView;
    public TextView timeTextView;

    static final StockActivityRealPriceBlockViewHolder EMPTY_VIEW_HOLDER = new StockActivityRealPriceBlockViewHolder();

    private StockActivityRealPriceBlockViewHolder() {

    }

    public static StockActivityRealPriceBlockViewHolder convertToViewHolder(final View view) {
        final StockActivityRealPriceBlockViewHolder viewHolder = new StockActivityRealPriceBlockViewHolder();
        try {
            viewHolder.mainView = view;
            viewHolder.priceTextView = view.findViewById(R.id.stock_price_tv);
            viewHolder.diffTextView = view.findViewById(R.id.stock_diff_tv);
            viewHolder.tradingTextView = view.findViewById(R.id.stock_trade_now_tv);
            viewHolder.timeTextView = view.findViewById(R.id.stock_time_tv);

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
                MarketSenseRendererHelper.setBackgroundColor(viewHolder.mainView, R.color.grapefruit_four);
                MarketSenseRendererHelper.addTextViewWithIcon(
                        viewHolder.diffTextView,
                        String.format(format, diffNum, diffPercentage),
                        R.mipmap.ic_trend_arrow_up_white);
            } else if(diffPercentageFloat < 0) {
                MarketSenseRendererHelper.setBackgroundColor(viewHolder.mainView, R.color.green_blue);
                MarketSenseRendererHelper.addTextViewWithIcon(
                        viewHolder.diffTextView,
                        String.format(format, diffNum, diffPercentage),
                        R.mipmap.ic_trend_arrow_down_white);
            } else {
                MarketSenseRendererHelper.setBackgroundColor(viewHolder.mainView, R.color.cloudy_blue);
                MarketSenseRendererHelper.addTextViewWithIcon(
                        viewHolder.diffTextView,
                        String.format(format, diffNum, diffPercentage),
                        0);
            }
        } catch (NumberFormatException e) {
            MarketSenseRendererHelper.setBackgroundColor(viewHolder.mainView, R.color.cloudy_blue);
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
}
