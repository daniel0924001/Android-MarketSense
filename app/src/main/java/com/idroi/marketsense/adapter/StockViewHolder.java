package com.idroi.marketsense.adapter;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
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
    TextView diffView;

    ImageView predictNewsImageView;
    ImageView predictTechImageView;
    ImageView predictFundamentalImageView;

    TextView predictNewsAttitude;
    TextView predictTechAttitude;
    TextView predictFundamentalAttitude;

    ConstraintLayout rightBlock;
    TextView rightTitle;
    TextView rightValue;

    ImageView hitImageView;

    static final StockViewHolder EMPTY_VIEW_HOLDER = new StockViewHolder();

    private StockViewHolder() {}

    static StockViewHolder convertToViewHolder(final View view) {
        final StockViewHolder stockViewHolder = new StockViewHolder();
        stockViewHolder.mainView = view;
        try {
            stockViewHolder.nameView = view.findViewById(R.id.marketsense_stock_name_tv);
            stockViewHolder.codeView = view.findViewById(R.id.marketsense_stock_code_tv);
            stockViewHolder.priceView = view.findViewById(R.id.marketsense_stock_price_tv);
            stockViewHolder.diffView = view.findViewById(R.id.marketsense_stock_diff_tv);

            stockViewHolder.predictNewsImageView = view.findViewById(R.id.marketsense_stock_news_iv);
            stockViewHolder.predictTechImageView = view.findViewById(R.id.marketsense_stock_tech_iv);
            stockViewHolder.predictFundamentalImageView = view.findViewById(R.id.marketsense_stock_fundamental_iv);

            stockViewHolder.predictNewsAttitude = view.findViewById(R.id.marketsense_stock_news_attitude_tv);
            stockViewHolder.predictTechAttitude = view.findViewById(R.id.marketsense_stock_people_attitude_tv);
            stockViewHolder.predictFundamentalAttitude = view.findViewById(R.id.marketsense_stock_fundamental_attitude_tv);

            stockViewHolder.rightBlock = view.findViewById(R.id.predict_block);
            stockViewHolder.rightTitle = view.findViewById(R.id.predict_title);
            stockViewHolder.rightValue = view.findViewById(R.id.predict_value);

            stockViewHolder.hitImageView = view.findViewById(R.id.ic_hit);

            return stockViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
