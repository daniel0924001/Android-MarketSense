package com.idroi.marketsense.datasource;

import android.app.Activity;

import com.idroi.marketsense.data.Stock;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListPlacer {

    public interface StockListListener {
        void onStockListLoaded();
    }

    private ArrayList<Stock> mStockArrayList;

    private Activity mActivity;

    public StockListPlacer(Activity activity) {
        mActivity = activity;
    }

    public void loadStockList() {

    }
}
