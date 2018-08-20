package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.view.View;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.StockTradeData;

/**
 * Created by daniel.hsieh on 2018/8/20.
 */

public class FiveBestPriceViewHolder {

    public View mainView;
    public BestPriceRowViewHolder[] bestPrices;

    static final FiveBestPriceViewHolder EMPTY_VIEW_HOLDER = new FiveBestPriceViewHolder();

    private FiveBestPriceViewHolder() {}

    public static FiveBestPriceViewHolder convertToViewHolder(final View view) {
        final FiveBestPriceViewHolder viewHolder = new FiveBestPriceViewHolder();
        try {
            viewHolder.mainView = view;
            BestPriceRowViewHolder viewHolder1 =
                    BestPriceRowViewHolder.convertToViewHolder(view.findViewById(R.id.best_price_1));
            BestPriceRowViewHolder viewHolder2 =
                    BestPriceRowViewHolder.convertToViewHolder(view.findViewById(R.id.best_price_2));
            BestPriceRowViewHolder viewHolder3 =
                    BestPriceRowViewHolder.convertToViewHolder(view.findViewById(R.id.best_price_3));
            BestPriceRowViewHolder viewHolder4 =
                    BestPriceRowViewHolder.convertToViewHolder(view.findViewById(R.id.best_price_4));
            BestPriceRowViewHolder viewHolder5 =
                    BestPriceRowViewHolder.convertToViewHolder(view.findViewById(R.id.best_price_5));
            viewHolder.bestPrices = new BestPriceRowViewHolder[] {
                    viewHolder1, viewHolder2, viewHolder3, viewHolder4, viewHolder5
            };
            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public static void update(Context context,
                              FiveBestPriceViewHolder viewHolder,
                              StockTradeData.BestPriceRow[] bestPriceRows,
                              float yesterdayPrice) {
        try {
            for (int i = 0; i < 5; i++) {
                StockTradeData.BestPriceRow bestPriceRow = bestPriceRows[i];
                MarketSenseRendererHelper.addTextViewWithAutoColor(
                        viewHolder.bestPrices[i].buyPrice,
                        String.valueOf(bestPriceRow.getBuyPrice()),
                        bestPriceRow.getBuyPrice(),
                        yesterdayPrice);
                MarketSenseRendererHelper.addTextViewWithAutoColor(
                        viewHolder.bestPrices[i].sellPrice,
                        String.valueOf(bestPriceRow.getSellPrice()),
                        bestPriceRow.getSellPrice(),
                        yesterdayPrice);
                MarketSenseRendererHelper.addTextViewWithColor(
                        viewHolder.bestPrices[i].buyVolume,
                        String.valueOf(bestPriceRow.getBuyVolume()),
                        R.color.white);
                MarketSenseRendererHelper.addTextViewWithColor(
                        viewHolder.bestPrices[i].sellVolume,
                        String.valueOf(bestPriceRow.getSellVolume()),
                        R.color.white);
            }
            viewHolder.mainView.setVisibility(View.VISIBLE);
        } catch (Exception exception) {
            MSLog.e("update exception in FiveBestPriceViewHolder: " + exception.toString());
            viewHolder.mainView.setVisibility(View.GONE);
        }
    }
}
