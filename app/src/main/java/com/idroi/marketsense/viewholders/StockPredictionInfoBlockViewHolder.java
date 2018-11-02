package com.idroi.marketsense.viewholders;

import android.content.Context;
import android.content.res.Resources;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.Stock;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by daniel.hsieh on 2018/11/1.
 */

public class StockPredictionInfoBlockViewHolder {

//    public ConstraintLayout todayBlock;
    public TextView todayTitleTextView;
    public TextView todayStatusTextView;

//    public ConstraintLayout tomorrowBlock;
    public TextView tomorrowTitleTextView;
    public TextView tomorrowStatusTextView;

    public ImageView foundationImageView;
    public ImageView techImageView;
    public ImageView newsImageView;
    public ImageView shortImageView;
    public ImageView middleImageView;
    public ImageView longImageView;

    static final StockPredictionInfoBlockViewHolder EMPTY_VIEW_HOLDER
            = new StockPredictionInfoBlockViewHolder();

    private StockPredictionInfoBlockViewHolder() {}

    public static StockPredictionInfoBlockViewHolder convertToViewHolder(final View view) {
        final StockPredictionInfoBlockViewHolder viewHolder = new StockPredictionInfoBlockViewHolder();
        try {
            // today part
            ConstraintLayout todayBlock = view.findViewById(R.id.today_block);
            viewHolder.todayTitleTextView =
                    todayBlock.findViewById(R.id.date_block_prediction_title);
            viewHolder.todayStatusTextView =
                    todayBlock.findViewById(R.id.date_block_prediction_diff);

            // tomorrow part
            ConstraintLayout tomorrowBlock = view.findViewById(R.id.tomorrow_block);
            viewHolder.tomorrowTitleTextView =
                    tomorrowBlock.findViewById(R.id.date_block_prediction_title);
            viewHolder.tomorrowStatusTextView =
                    tomorrowBlock.findViewById(R.id.date_block_prediction_diff);

            // info part
            ConstraintLayout infoBlock = view.findViewById(R.id.info_block);
            viewHolder.foundationImageView = infoBlock.findViewById(R.id.info_foundation_iv);
            viewHolder.techImageView = infoBlock.findViewById(R.id.info_tech_iv);
            viewHolder.newsImageView = infoBlock.findViewById(R.id.info_news_iv);
            viewHolder.shortImageView = infoBlock.findViewById(R.id.info_short_iv);
            viewHolder.middleImageView = infoBlock.findViewById(R.id.info_middle_iv);
            viewHolder.longImageView = infoBlock.findViewById(R.id.info_long_iv);

            return viewHolder;

        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void render(Context context, Stock stock) {
        renderTodayBlock(context, stock);
        renderTomorrowBlock(context, stock);
        renderInfoBlock(stock);
    }

    public void renderInfoBlock(Stock stock) {
        renderImageViewForDirection(foundationImageView, stock.getPredictFoundationDirection());
        renderImageViewForDirection(techImageView, stock.getPredictTechDirection());
        renderImageViewForDirection(newsImageView, stock.getConfidenceDirection());
        renderImageViewForDirection(shortImageView, stock.get1DPredictionDirection());
        renderImageViewForDirection(middleImageView, stock.get5DPredictionDirection());
        renderImageViewForDirection(longImageView, stock.get20DPredictionDirection());
    }

    private void renderImageViewForDirection(ImageView imageView, int direction) {
        if(direction > 0) {
            imageView.setImageResource(R.mipmap.ic_trend_up_s);
        } else if(direction < 0) {
            imageView.setImageResource(R.mipmap.ic_trend_down_s);
        } else {
            imageView.setImageResource(R.mipmap.ic_trend_draw_s);
        }
    }

    public void renderTodayBlock(Context context, Stock stock) {
        Resources resources = context.getResources();
        ClientData clientData = ClientData.getInstance();

        String todayFormat = resources.getString(R.string.title_month_day_close_predict);
        if(clientData.isWorkDay()) {
            if(clientData.isWorkDayBeforeStockOpen()) {
                // D - 1
                Calendar c = clientData.getWorkDayMinusOne();
                todayTitleTextView.setText(String.format(todayFormat,
                        c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
            } else {
                // D
                Calendar c = clientData.getWorkDay();
                todayTitleTextView.setText(String.format(todayFormat,
                        c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
            }
        } else {
            // D - n
            Calendar c = clientData.getWorkDay();
            todayTitleTextView.setText(String.format(todayFormat,
                    c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
        }

        int direction = stock.getTodayPredictionDiffDirection();
        double diff = stock.getTodayPredictionDiffPercentage();
        if (direction == Stock.TREND_UP) {
            todayStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_up_background));
            todayStatusTextView.setText(String.format(Locale.US, "+%.2f%%", diff));
        } else if (direction == Stock.TREND_DOWN) {
            todayStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_down_background));
            todayStatusTextView.setText(String.format(Locale.US, "-%.2f%%", diff));
        } else {
            todayStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_flat_background));
            todayStatusTextView.setText(R.string.info_flat);
        }
    }

    public void renderTomorrowBlock(Context context, Stock stock) {
        Resources resources = context.getResources();
        ClientData clientData = ClientData.getInstance();

        String todayFormat = resources.getString(R.string.title_month_day_close_predict);
        int direction = stock.getTomorrowPredictionDiffDirection();
        double diff = stock.getTomorrowPredictionDiffPercentage();
        if(clientData.isWorkDay()) {
            if(clientData.isWorkDayBeforeStockOpen()) {
                // D
                Calendar c = clientData.getWorkDay();
                tomorrowTitleTextView.setText(String.format(todayFormat,
                        c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
                if (direction == Stock.TREND_UP) {
                    tomorrowStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_up_background));
                    tomorrowStatusTextView.setText(String.format(Locale.US, "+%.2f%%", diff));
                } else if (direction == Stock.TREND_DOWN) {
                    tomorrowStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_down_background));
                    tomorrowStatusTextView.setText(String.format(Locale.US, "-%.2f%%", diff));
                } else {
                    tomorrowStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_flat_background));
                    todayStatusTextView.setText(R.string.info_flat);
                }
            } else {
                // D + 1
                Calendar c = clientData.getWorkDayPlusOne();
                tomorrowTitleTextView.setText(String.format(todayFormat,
                        c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
                if(clientData.isWorkDayAndStockMarketIsOpen() || clientData.isWorkDayAfterStockClosedBeforeAnswerDisclosure()) {
                    tomorrowStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_flat_background));
                    tomorrowStatusTextView.setText(R.string.title_disclosure_at_1500);
                } else {
                    if (direction == Stock.TREND_UP) {
                        tomorrowStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_up_background));
                        tomorrowStatusTextView.setText(String.format(Locale.US, "+%.2f%%", diff));
                    } else if (direction == Stock.TREND_DOWN) {
                        tomorrowStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_down_background));
                        tomorrowStatusTextView.setText(String.format(Locale.US, "-%.2f%%", diff));
                    } else {
                        tomorrowStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_flat_background));
                        todayStatusTextView.setText(R.string.info_flat);
                    }
                }
            }
        } else {
            // D + n
            Calendar c = clientData.getWorkDayPlusOne();
            tomorrowTitleTextView.setText(String.format(todayFormat,
                    c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
            if (direction == Stock.TREND_UP) {
                tomorrowStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_up_background));
                tomorrowStatusTextView.setText(String.format(Locale.US, "+%.2f%%", diff));
            } else if (direction == Stock.TREND_DOWN) {
                tomorrowStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_down_background));
                tomorrowStatusTextView.setText(String.format(Locale.US, "-%.2f%%", diff));
            } else {
                tomorrowStatusTextView.setBackground(resources.getDrawable(R.drawable.block_predict_flat_background));
                todayStatusTextView.setText(R.string.info_flat);
            }
        }
    }
}
