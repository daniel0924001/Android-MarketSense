package com.idroi.marketsense.datasource;

import android.app.Activity;
import android.content.pm.PackageManager;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.Constants;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static com.idroi.marketsense.fragments.StockListFragment.ACTUAL_LOSE_ID;
import static com.idroi.marketsense.fragments.StockListFragment.ACTUAL_WIN_ID;
import static com.idroi.marketsense.fragments.StockListFragment.PREDICT_LOSE_ID;
import static com.idroi.marketsense.fragments.StockListFragment.PREDICT_WIN_ID;
import static com.idroi.marketsense.fragments.StockListFragment.SELF_CHOICES_ID;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockListPlacer {

    public interface StockListListener {
        void onStockListLoaded();
    }

    private static final int RETRY_TIME_CONST = 2;
    private int mCurrentRetries = 0;

    private ArrayList<Stock> mStockArrayList;
    private MarketSenseStockFetcher.MarketSenseStockNetworkListener mMarketSenseStockNetworkListener;
    private StockListListener mStockListListener;

    private Activity mActivity;
    private MarketSenseStockFetcher mMarketSenseStockFetcher;
    private String mUrl;
    private int mTask;

    public StockListPlacer(Activity activity, int taskId) {
        mActivity = activity;

        mMarketSenseStockNetworkListener = new MarketSenseStockFetcher.MarketSenseStockNetworkListener() {
            @Override
            public void onStockListLoad(ArrayList<Stock> stockArrayList) {
                if(mStockArrayList != null) {
                    mStockArrayList.clear();
                }

                if(mTask == SELF_CHOICES_ID) {

                    ArrayList<Stock> cloneStockArrayList = new ArrayList<>(stockArrayList);

                    mStockArrayList = new ArrayList<>();
                    UserProfile userProfile = ClientData.getInstance().getUserProfile();
                    MSLog.d("favorite stocks: " + userProfile.getFavoriteStocksString());
                    for(int i = 0; i < cloneStockArrayList.size(); i++) {
                        Stock stock = cloneStockArrayList.get(i);
                        String code = cloneStockArrayList.get(i).getCode();
                        if(userProfile.isFavoriteStock(code)) {
                            mStockArrayList.add(stock);
                        }
                    }
                } else {
                    mStockArrayList = new ArrayList<>(stockArrayList);

                    // There are four fragments in PredictionScreenSlidePagerAdapter
                    // 1st: sort by prediction confidence in descending
                    // 2nd: sort by prediction confidence in ascending
                    // 3rd: sort by stock difference in descending
                    // 4th: sort by stock difference in ascending
                    // 5th: filter by user favorite stock array
                    Comparator<Stock> comparator = genComparator(mTask);
                    if (comparator != null) {
                        Collections.sort(mStockArrayList, comparator);
                    }
                }

                if(mStockListListener != null) {
                    mStockListListener.onStockListLoaded();
                }
            }

            @Override
            public void onStockListFail(MarketSenseError marketSenseError) {
                increaseRetryTime();
                if(isRetry()) {
                    mMarketSenseStockFetcher.makeRequest(mUrl);
                } else {
                    generateDefaultStockList();
                    if(mStockListListener != null) {
                        mStockListListener.onStockListLoaded();
                    }
                }
            }
        };
        mTask = taskId;
        resetRetryTime();
    }

    private boolean isRetry() {
        return mCurrentRetries <= RETRY_TIME_CONST;
    }

    private void increaseRetryTime() {
        if(mCurrentRetries <= RETRY_TIME_CONST) {
            mCurrentRetries++;
        }
    }

    private void resetRetryTime() {
        mCurrentRetries = 0;
    }

    public boolean isEmpty() {
        return mStockArrayList == null || mStockArrayList.size() == 0;
    }

    public void setStockListListener(StockListListener listener) {
        mStockListListener = listener;
    }

    public void loadStockList(String url) {
        mUrl = url;
        loadStockList(new MarketSenseStockFetcher(mActivity, mMarketSenseStockNetworkListener));
    }

    private void loadStockList(MarketSenseStockFetcher stockFetcher) {
        clear();
        mMarketSenseStockFetcher = stockFetcher;
        mMarketSenseStockFetcher.makeRequest(mUrl);
    }

    public void clear() {
        if(mStockArrayList != null) {
            mStockArrayList.clear();
            mStockArrayList = null;
        }
        if(mMarketSenseStockFetcher != null) {
            mMarketSenseStockFetcher.destroy();
            mMarketSenseStockFetcher = null;
        }
    }

    private void generateDefaultStockList() {
        MSLog.e("Stock generate default stock list");
        if(mStockArrayList == null) {
            mStockArrayList = new ArrayList<Stock>();
        }
        mStockArrayList.addAll(Arrays.asList(Constants.HOT_STOCKS_KEYWORDS));
    }

    public int getItemCount() {
        if(mStockArrayList != null) {
            return mStockArrayList.size();
        } else {
            return 0;
        }
    }

    public Stock getStockData(int position) {
        if(mStockArrayList == null || position >= mStockArrayList.size() || position < 0) {
            return null;
        }
        return mStockArrayList.get(position);
    }

    private Comparator<Stock> genComparator(int taskId) {
        switch (taskId) {
            case PREDICT_LOSE_ID:
                return new Comparator<Stock>() {
                    @Override
                    public int compare(Stock stock1, Stock stock2) {
                        return compareValue(
                                stock1.getConfidenceDirection() * stock1.getConfidence(),
                                stock2.getConfidenceDirection() * stock2.getConfidence());
                    }
                };
            case PREDICT_WIN_ID:
                return new Comparator<Stock>() {
                    @Override
                    public int compare(Stock stock1, Stock stock2) {
                        return compareValue(
                                stock2.getConfidenceDirection() * stock2.getConfidence(),
                                stock1.getConfidenceDirection() * stock1.getConfidence());
                    }
                };
            case ACTUAL_LOSE_ID:
                return new Comparator<Stock>() {
                    @Override
                    public int compare(Stock stock1, Stock stock2) {
                        return compareValue(
                                stock1.getDiffDirection() * stock1.getDiffPercentageDouble(),
                                stock2.getDiffDirection() * stock2.getDiffPercentageDouble());
                    }
                };
            case ACTUAL_WIN_ID:
                return new Comparator<Stock>() {
                    @Override
                    public int compare(Stock stock1, Stock stock2) {
                        return compareValue(
                                stock2.getDiffDirection() * stock2.getDiffPercentageDouble(),
                                stock1.getDiffDirection() * stock1.getDiffPercentageDouble());
                    }
                };
            case SELF_CHOICES_ID:
                return null;
            default:
                return null;
        }
    }

    private int compareValue(double double1, double double2) {
        if(double1 > double2) {
            return 1;
        } else if(double1 == double2) {
            return 0;
        } else {
            return -1;
        }
    }
}
