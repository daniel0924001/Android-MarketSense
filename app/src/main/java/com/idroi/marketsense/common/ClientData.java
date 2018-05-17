package com.idroi.marketsense.common;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.datasource.Networking;
import com.idroi.marketsense.request.StocksListRequest;
import com.idroi.marketsense.request.UserEventsAndCodesRequest;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by daniel.hsieh on 2018/5/3.
 */

public class ClientData {

    private static volatile ClientData sInstance;

    private Context mContext;
    private ArrayList<Stock> mAllStocksListInfo;

    private int mScreenWidth, mScreenHeight;
    private int mScreenWidthPixels, mScreenHeightPixels;
    @NonNull private UserProfile mUserProfile;
    private String mUserToken;

    private static final int DEFAULT_RETRY_TIME = 3;
    private int mLoadPreferenceRetryCounter = DEFAULT_RETRY_TIME;
    private int mLoadAllStockListRetryCounter = DEFAULT_RETRY_TIME;

    /**
     * Returns the singleton ClientMetadata object, using the context to obtain data if necessary.
     */
    public static ClientData getInstance(Context context) {
        // Use a local variable so we can reduce accesses of the volatile field.
        ClientData result = sInstance;
        if (result == null) {
            synchronized (ClientData.class) {
                result = sInstance;
                if(result == null) {
                    result = new ClientData(context);
                    sInstance = result;
                }
            }
        }
        return result;
    }

    /**
     * Can be used by background threads and other objects without a context to attempt to get
     * ClientMetadata. If the object has never been referenced from a thread with a context,
     * this will return null.
     */
    public static ClientData getInstance() {
        ClientData result = sInstance;
        if(result == null) {
            // If it's being initialized in another thread, wait for the lock.
            synchronized (ClientData.class) {
                result = sInstance;
            }
        }

        return result;
    }

    private ClientData(Context context) {
        mContext = context.getApplicationContext();
        mUserProfile = new UserProfile(context, true);

        loadAllStocksListTask(true);
    }

    public void setScreenSizeInPixels(int widthPixels, int heightPixels) {
        mScreenWidthPixels = widthPixels;
        mScreenHeightPixels = heightPixels;
    }

    public void setScreenSize(int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
    }

    private void setAllStocksListInfo(ArrayList<Stock> stocksListInfo) {
        mAllStocksListInfo = stocksListInfo;
    }

    public void setUserToken(String token) {
        mUserToken = token;
    }

    public String getUserToken() {
        return mUserToken;
    }

    public UserProfile getUserProfile() {
        return mUserProfile;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public int getScreenWidthPixels() {
        return mScreenWidthPixels;
    }

    public int getScreenHeightPixels() {
        return mScreenHeightPixels;
    }

    public ArrayList<Stock> getAllStocksListInfo() {
        return mAllStocksListInfo;
    }

    public String getCodeFromName(String name) {
        if(mAllStocksListInfo != null) {
            for (int i = 0; i < mAllStocksListInfo.size(); i++) {
                Stock stock = mAllStocksListInfo.get(i);
                if(stock.getName().equals(name)) {
                    return stock.getCode();
                }
            }
        }
        return null;
    }

    public String getNameFromCode(String code) {
        if(mAllStocksListInfo != null) {
            for (int i = 0; i < mAllStocksListInfo.size(); i++) {
                Stock stock = mAllStocksListInfo.get(i);
                if(stock.getCode().equals(code)) {
                    return stock.getName();
                }
            }
        }
        return null;
    }

    public boolean isNameAndCodeAreValid(String name, String code) {
        return (name != null) && (code != null);
    }

    private void loadAllStocksListTask(boolean shouldReadCache) {
        String url = StocksListRequest.queryStockListURL();
        MSLog.i("Loading all stocks list...: " + url);

        if(shouldReadCache) {
            MSLog.i("Loading all stocks list...(cache): " + url);
            Cache cache = Networking.getRequestQueue(mContext).getCache();
            Cache.Entry entry = cache.get(url);
            if (entry != null) {
                try {
                    ArrayList<Stock> stockArrayList = StocksListRequest.stockParseResponse(entry.data);
                    MSLog.i("Loading all stock list...(cache hit): " + new String(entry.data));
                    setAllStocksListInfo(stockArrayList);
                } catch (JSONException e) {
                    MSLog.e("Loading all stocks list...(cache failed JSONException)");
                }
            } else {
                MSLog.i("Loading all stocks list...(cache miss)");
            }
        }

        StocksListRequest stocksListRequest = new StocksListRequest(Request.Method.GET, url, null, new Response.Listener<ArrayList<Stock>>() {
            @Override
            public void onResponse(ArrayList<Stock> response) {
                mLoadAllStockListRetryCounter = DEFAULT_RETRY_TIME;
                MSLog.i("Stocks List Request success");
                setAllStocksListInfo(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MSLog.e("Stocks List Request error: " + error.getMessage(), error);
                if(error.networkResponse != null) {
                    MSLog.e("Stocks List Request error: " + new String(error.networkResponse.data), error);
                }
                if(mLoadAllStockListRetryCounter > 0) {
                    loadAllStocksListTask(false);
                } else {
                    mLoadAllStockListRetryCounter = DEFAULT_RETRY_TIME;
                }
            }
        });

        Networking.getRequestQueue(mContext).add(stocksListRequest);
    }

    public void loadPreference() {
        String url = UserEventsAndCodesRequest.querySelfStockList();
        MSLog.i("Loading user preference...: " + url);

        UserEventsAndCodesRequest userEventsAndCodesRequest =
                new UserEventsAndCodesRequest(Request.Method.GET, url, new Response.Listener<Void>() {
                    @Override
                    public void onResponse(Void response) {
                        mLoadPreferenceRetryCounter = DEFAULT_RETRY_TIME;
                        MSLog.i("User Preference Request success: " +
                                mUserProfile.getFavoriteStocksString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        MSLog.e("User Preference Request error: " + error.getMessage(), error);
                        if(error.networkResponse != null) {
                            MSLog.e("User Preference Request error: " + new String(error.networkResponse.data), error);
                        }
                        mLoadPreferenceRetryCounter--;
                        if(mLoadPreferenceRetryCounter > 0) {
                            loadPreference();
                        } else {
                            mLoadPreferenceRetryCounter = DEFAULT_RETRY_TIME;
                        }
                    }
                });

        userEventsAndCodesRequest.setShouldCache(false);
        Networking.getRequestQueue(mContext).add(userEventsAndCodesRequest);
    }
}
