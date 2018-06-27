package com.idroi.marketsense.datasource;

import android.app.Activity;
import android.os.Handler;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.StockRequest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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

    public final static int SORT_BY_NAME = 1;
    public final static int SORT_BY_PRICE = 5;
    public final static int SORT_BY_DIFF = 2;
    public final static int SORT_BY_PEOPLE = 3;
    public final static int SORT_BY_NEWS = 4;

    public final static int SORT_UPWARD = 1;
    public final static int SORT_DOWNWARD = 2;

    public interface StockListListener {
        void onStockListLoaded();
    }

    private static final int RETRY_TIME_CONST = 2;
    private int mCurrentRetries = 0;

    private ArrayList<Stock> mStockArrayList;

    private MarketSenseStockFetcher.MarketSenseStockNetworkListener mMarketSenseStockNetworkListener;
    private StockListListener mStockListListener;

    private WeakReference<Activity> mActivity;
    private MarketSenseStockFetcher mMarketSenseStockFetcher;
    private String mNetworkUrl, mCacheUrl;
    private int mTask;
    private int mSortedField;
    private int mSortedDirection;

    private Handler mRefreshHandler;
    private Runnable mRefreshRunnable;
    private static final int REFRESH_TIME = 2 * 60 * 1000;
    private boolean mHasPostRefresh;

    public StockListPlacer(Activity activity) {
        this(activity, PREDICT_WIN_ID, SORT_BY_NEWS, SORT_DOWNWARD);
    }

    public StockListPlacer(Activity activity, int taskId, int field, int direction) {
        mActivity = new WeakReference<Activity>(activity);
        mSortedField = field;
        mSortedDirection = direction;
        mRefreshHandler = new Handler();
        mHasPostRefresh = false;

        mMarketSenseStockNetworkListener = new MarketSenseStockFetcher.MarketSenseStockNetworkListener() {
            @Override
            public void onStockListLoad(ArrayList<Stock> stockArrayList, boolean isAutoRefresh) {

                if(mActivity.get() == null) {
                    return;
                }

                if (mStockArrayList != null) {
                    mStockArrayList.clear();
                }

                updateRealTimeStockPrices(stockArrayList);

                if (mTask == SELF_CHOICES_ID) {

                    ArrayList<Stock> cloneStockArrayList = new ArrayList<>(stockArrayList);

                    mStockArrayList = new ArrayList<>();
                    UserProfile userProfile = ClientData.getInstance().getUserProfile();
                    MSLog.d("favorite stocks: " + userProfile.getFavoriteStocksString());
                    for (int i = 0; i < cloneStockArrayList.size(); i++) {
                        Stock stock = cloneStockArrayList.get(i);
                        String code = cloneStockArrayList.get(i).getCode();
                        if (userProfile.isFavoriteStock(code)) {
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

                }

                Comparator<Stock> comparator = genComparator(mSortedField, mSortedDirection);
                Collections.sort(mStockArrayList, comparator);

                if (mStockListListener != null) {
                    mStockListListener.onStockListLoaded();
                }

                if(!mHasPostRefresh) {
                    MSLog.d("schedule refresh stock price in " + REFRESH_TIME + " millisecond.");
                    mRefreshHandler.postDelayed(mRefreshRunnable, REFRESH_TIME);
                    mHasPostRefresh = true;
                }
            }

            @Override
            public void onStockListFail(MarketSenseError marketSenseError) {

                if(mActivity.get() == null) {
                    return;
                }

                increaseRetryTime();
                if(isRetry()) {
                    mMarketSenseStockFetcher.makeRequest(mNetworkUrl, mCacheUrl);
                } else {
                    if(mStockListListener != null) {
                        mStockListListener.onStockListLoaded();
                    }
                }
            }
        };
        mTask = taskId;
        resetRetryTime();

        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                mHasPostRefresh = false;
                if(mMarketSenseStockFetcher != null) {
                    MSLog.d("[stock price refresh]");

                    Activity activity = mActivity.get();
                    if(activity == null) {
                        return;
                    }

                    mNetworkUrl = StockRequest.queryStockList(activity, true);
                    mMarketSenseStockFetcher.makeRequest(mNetworkUrl, null, true);
                }
            }
        };
    }

    private void updateRealTimeStockPrices(ArrayList<Stock> stockPrices) {
        if(stockPrices != null) {
            for (Stock stock : stockPrices) {
                ClientData.getInstance(mActivity.get()).setRealTimeStockPriceHashMap(stock);
            }
        }
        MSLog.i("refresh real time stock price");
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

    public void loadStockList(String networkUrl, String cacheUrl) {
        Activity activity = mActivity.get();
        if(activity == null) {
            return;
        }

        mNetworkUrl = networkUrl;
        mCacheUrl = cacheUrl;
        loadStockList(new MarketSenseStockFetcher(activity, mMarketSenseStockNetworkListener));
    }

    private void loadStockList(MarketSenseStockFetcher stockFetcher) {
        clear();
        mMarketSenseStockFetcher = stockFetcher;
        mMarketSenseStockFetcher.makeRequest(mNetworkUrl, mCacheUrl);
    }

    public void sortByTask(int field, int direction) {
        mSortedField = field;
        mSortedDirection = direction;
        // There are four fragments in PredictionScreenSlidePagerAdapter
        // 1st: sort by prediction confidence in descending
        // 2nd: sort by prediction confidence in ascending
        // 3rd: sort by stock difference in descending
        // 4th: sort by stock difference in ascending
        // 5th: filter by user favorite stock array
        if(mStockArrayList != null) {
            Comparator<Stock> comparator = genComparator(mSortedField, mSortedDirection);
            Collections.sort(mStockArrayList, comparator);
        }
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
        mRefreshHandler.removeCallbacks(mRefreshRunnable);
    }

    public int getItemCount() {
        if(mStockArrayList != null) {
            return mStockArrayList.size();
        } else {
            return 0;
        }
    }

    public ArrayList<Stock> getStocks() {
        return mStockArrayList;
    }

    public Stock getStockData(int position) {
        if(mStockArrayList == null || position >= mStockArrayList.size() || position < 0) {
            return null;
        }
        String code = mStockArrayList.get(position).getCode();
        return getRealTimeStock(code);
    }

    private Stock getRealTimeStock(String code) {
        return ClientData.getInstance(mActivity.get()).getPriceFromCode(code);
    }

    private Comparator<Stock> genComparator(final int field, final int direction) {
        return new Comparator<Stock>() {
            @Override
            public int compare(Stock s1, Stock s2) {
                Stock stock1, stock2;
                if(direction == SORT_UPWARD) {
                    stock1 = s1;
                    stock2 = s2;
                } else if(direction == SORT_DOWNWARD) {
                    stock1 = s2;
                    stock2 = s1;
                } else {
                    return 0;
                }

                switch (field) {
                    case SORT_BY_NAME:
                        int res = String.CASE_INSENSITIVE_ORDER.compare(stock1.getCode(), stock2.getCode());
                        return (res != 0) ? res : stock1.getCode().compareTo(stock2.getCode());
                    case SORT_BY_PRICE:
                        return compareValue(Double.valueOf(stock1.getPrice()), Double.valueOf(stock2.getPrice()));
                    case SORT_BY_DIFF:
                        return compareValue(stock1.getDiffDirection() * stock1.getDiffPercentageDouble(),
                                stock2.getDiffDirection() * stock2.getDiffPercentageDouble());
                    case SORT_BY_PEOPLE:
                        return compareValue(stock1.getPredictPeopleDirection() * stock1.getPredictPeopleScore(),
                                stock2.getPredictPeopleDirection() * stock2.getPredictPeopleScore());
                    case SORT_BY_NEWS:
                        return compareValue(stock1.getConfidenceDirection() * stock1.getConfidence(),
                                stock2.getConfidenceDirection() * stock2.getConfidence());
                }

                return 0;
            }
        };
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
