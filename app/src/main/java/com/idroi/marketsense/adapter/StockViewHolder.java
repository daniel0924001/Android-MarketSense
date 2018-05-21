package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockViewHolder {

    View mainView;

    TextView nameView;
    TextView codeView;

    TextView priceView;
    TextView diffNumberView;
    TextView diffPercentageView;

    TextView predictNewsScore;
    RatingBar predictNewsStars;
    TextView predictPeopleScore;
    RatingBar predictPeopleStars;
//    TextView predictOurScore;
//    RatingBar predictOurStars;

    static final StockViewHolder EMPTY_VIEW_HOLDER = new StockViewHolder();

    private StockViewHolder() {}

    static StockViewHolder convertToViewHolder(final View view) {
        final StockViewHolder stockViewHolder = new StockViewHolder();
        stockViewHolder.mainView = view;
        try {
            stockViewHolder.nameView = view.findViewById(R.id.marketsense_stock_name_tv);
            stockViewHolder.codeView = view.findViewById(R.id.marketsense_stock_code_tv);
            stockViewHolder.priceView = view.findViewById(R.id.marketsense_stock_price_tv);
            stockViewHolder.diffNumberView = view.findViewById(R.id.marketsense_stock_diff_num_tv);
            stockViewHolder.diffPercentageView = view.findViewById(R.id.marketsense_stock_diff_percentage_tv);
            stockViewHolder.predictNewsScore = view.findViewById(R.id.predict_news_description);
            stockViewHolder.predictNewsStars = view.findViewById(R.id.predict_news_stars);
            stockViewHolder.predictPeopleScore = view.findViewById(R.id.predict_people_description);
            stockViewHolder.predictPeopleStars = view.findViewById(R.id.predict_people_stars);
//            stockViewHolder.predictOurScore = view.findViewById(R.id.predict_our_description);
//            stockViewHolder.predictOurStars = view.findViewById(R.id.predict_our_stars);
            return stockViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
