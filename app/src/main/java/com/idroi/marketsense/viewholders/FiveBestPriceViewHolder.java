package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
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

    public static void update(final FiveBestPriceViewHolder viewHolder,
                              final StockTradeData.BestPriceRow[] bestPriceRows,
                              final float yesterdayPrice) {
        float tempMaxVolume = 0;
        try {
            for (int i = 0; i < 5; i++) {
                StockTradeData.BestPriceRow bestPriceRow = bestPriceRows[i];
                MarketSenseRendererHelper.addNumberStringToTextView(
                        viewHolder.bestPrices[i].buyPrice,
                        String.valueOf(bestPriceRow.getBuyPrice()),
                        "--",
                        bestPriceRow.getBuyPrice(),
                        yesterdayPrice);
                MarketSenseRendererHelper.addNumberStringToTextView(
                        viewHolder.bestPrices[i].sellPrice,
                        String.valueOf(bestPriceRow.getSellPrice()),
                        "--",
                        bestPriceRow.getSellPrice(),
                        yesterdayPrice);

                float buyVolume = bestPriceRow.getBuyVolume();
                float sellVolume = bestPriceRow.getSellVolume();

                if(buyVolume != 0) {
                    MarketSenseRendererHelper.addTextViewWithColor(
                            viewHolder.bestPrices[i].buyVolume,
                            String.valueOf((int) buyVolume),
                            R.color.white);
                } else {
                    MarketSenseRendererHelper.addTextViewWithColor(
                            viewHolder.bestPrices[i].buyVolume,
                            "--",
                            R.color.white);
                }
                if(sellVolume != 0) {
                    MarketSenseRendererHelper.addTextViewWithColor(
                            viewHolder.bestPrices[i].sellVolume,
                            String.valueOf((int) sellVolume),
                            R.color.white);
                } else {
                    MarketSenseRendererHelper.addTextViewWithColor(
                            viewHolder.bestPrices[i].sellVolume,
                            "--",
                            R.color.white);
                }

                if(buyVolume > tempMaxVolume) {
                    tempMaxVolume = buyVolume;
                }
                if(sellVolume > tempMaxVolume) {
                    tempMaxVolume = sellVolume;
                }
            }
            final float maxVolume = tempMaxVolume;
            viewHolder.mainView.setVisibility(View.VISIBLE);

            viewHolder.bestPrices[0].burBar.post(new Runnable() {
                @Override
                public void run() {
                    int width = viewHolder.bestPrices[0].burBar.getWidth();
                    for(int i = 0; i < 5; i++) {
                        StockTradeData.BestPriceRow bestPriceRow = bestPriceRows[i];

                        if(bestPriceRow.getBuyVolume() != 0) {
                            ConstraintLayout.LayoutParams buyParams = (ConstraintLayout.LayoutParams) viewHolder.bestPrices[i].buyColorBar.getLayoutParams();
                            float buyRatio = bestPriceRow.getBuyVolume() / maxVolume;
                            buyParams.width = (int) (width * buyRatio);
                            viewHolder.bestPrices[i].buyColorBar.setLayoutParams(buyParams);
                        } else {
                            viewHolder.bestPrices[i].buyColorBar.setVisibility(View.GONE);
                        }

                        if(bestPriceRow.getSellVolume() != 0) {
                            ConstraintLayout.LayoutParams sellParams = (ConstraintLayout.LayoutParams) viewHolder.bestPrices[i].sellColorBar.getLayoutParams();
                            float sellRatio = bestPriceRow.getSellVolume() / maxVolume;
                            sellParams.width = (int) (width * sellRatio);
                            viewHolder.bestPrices[i].sellColorBar.setLayoutParams(sellParams);
                        } else {
                            viewHolder.bestPrices[i].sellColorBar.setVisibility(View.GONE);
                        }
                    }
                }
            });

        } catch (Exception exception) {
            MSLog.e("update exception in FiveBestPriceViewHolder: " + exception.toString());
            viewHolder.mainView.setVisibility(View.GONE);
        }
    }
}
