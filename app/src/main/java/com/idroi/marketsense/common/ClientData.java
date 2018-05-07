package com.idroi.marketsense.common;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.datasource.Networking;
import com.idroi.marketsense.request.StockRequest;
import com.idroi.marketsense.request.StocksListRequest;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/5/3.
 */

public class ClientData {

    private static volatile ClientData sInstance;

    private Context mContext;
    private ArrayList<Stock> mAllStocksListInfo;

    private StocksListRequest mStocksListRequest;
    private int mScreenWidth, mScreenHeight;

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

        loadAllStocksListTask();
    }

    public void setScreenSize(int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
    }

    private void setAllStocksListInfo(ArrayList<Stock> stocksListInfo) {
        mAllStocksListInfo = stocksListInfo;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public ArrayList<Stock> getAllStocksListInfo() {
        return mAllStocksListInfo;
    }

    private void loadAllStocksListTask() {
        String url = StocksListRequest.queryStockListURL();
        MSLog.i("Loading all stocks list...: " + url);

        MSLog.i("Loading all stocks list...(cache): " + url);
        Cache cache = Networking.getRequestQueue(mContext).getCache();
        Cache.Entry entry = cache.get(url);
        if(entry != null) {
            try {
                ArrayList<Stock> stockArrayList = StocksListRequest.stockParseResponse(entry.data);
                MSLog.i("Loading stock list...(cache hit): " + new String(entry.data));
                setAllStocksListInfo(stockArrayList);
            } catch (JSONException e) {
                MSLog.e("Loading all stocks list...(cache failed JSONException)");
            }
        } else {
            MSLog.i("Loading all stocks list...(cache miss)");
        }

        mStocksListRequest = new StocksListRequest(Request.Method.GET, url, null, new Response.Listener<ArrayList<Stock>>() {
            @Override
            public void onResponse(ArrayList<Stock> response) {
                setAllStocksListInfo(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MSLog.e("Stocks List Request error: " + error.getMessage(), error);
                if(error.networkResponse != null) {
                    MSLog.e("Stocks List Request error: " + new String(error.networkResponse.data), error);
                }
            }
        });

        Networking.getRequestQueue(mContext).add(mStocksListRequest);
    }
}
