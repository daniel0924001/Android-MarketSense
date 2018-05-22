package com.idroi.marketsense.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.StockTickData;
import com.idroi.marketsense.data.StockTradeData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by daniel.hsieh on 2018/5/21.
 */

public class StockChartDataRequest extends Request<StockTradeData> {

    private static final String PARAM_TICK = "tick";
    private static final String PARAM_MEM = "mem";
    private static final String PARAM_YESTERDAY = "129";
    private static final String PARAM_MIN_PRICE = "133";
    private static final String PARAM_MAX_PRICE = "132";
    private static final String PARAM_T = "t";
    private static final String PARAM_P = "p";
    private static final String PARAM_V = "v";

    private final String mUrl;
    private final Response.Listener<StockTradeData> mListener;

    public StockChartDataRequest(String url, Response.Listener<StockTradeData> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mUrl = url;
        mListener = listener;
    }

    @Override
    protected Response<StockTradeData> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data);
            jsonString = jsonString.substring(5, jsonString.length() - 1);
            JSONObject jsonData = new JSONObject(jsonString);

            MSLog.d("" + jsonData);
            StockTradeData stockTradeData = new StockTradeData();
            setTickData(stockTradeData, jsonData);
            setMemData(stockTradeData, jsonData);


            if(stockTradeData.getStockTickData().size() > 0) {
                return Response.success(stockTradeData, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_NO_DATA));
            }
        } catch (JSONException e) {
            return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_ERROR));
        }
    }

    @Override
    protected void deliverResponse(StockTradeData response) {
        mListener.onResponse(response);
    }

    private static void setTickData(StockTradeData stockTradeData, JSONObject jsonResponse) {
        JSONArray tickArray = jsonResponse.optJSONArray(PARAM_TICK);
        if(tickArray != null && tickArray.length() > 0) {
            ArrayList<StockTickData> stockTickDataArrayList = new ArrayList<>(tickArray.length());
            for(int i = 0; i < tickArray.length(); i++) {
                try {
                    JSONObject tick = tickArray.getJSONObject(i);
                    if (tick != null) {
                        StockTickData stockTickData
                                = new StockTickData(
                                        tick.getLong(PARAM_T),
                                        tick.getDouble(PARAM_P),
                                        tick.getInt(PARAM_V), i);
                        stockTickDataArrayList.add(stockTickData);
                    }
                } catch (Exception e) {
                    MSLog.e("Exception in setTickData: " + e);
                    // pass
                }
            }
            stockTradeData.setStockTickData(stockTickDataArrayList);
        }
    }

    private static void setMemData(StockTradeData stockTradeData, JSONObject jsonResponse) {
        JSONObject data = jsonResponse.optJSONObject(PARAM_MEM);
        if(data != null) {
            try {
                float yesterdayPrice = (float) data.getDouble(PARAM_YESTERDAY);
                float minPrice = (float) data.getDouble(PARAM_MIN_PRICE);
                float maxPrice = (float) data.getDouble(PARAM_MAX_PRICE);
                MSLog.d("" +yesterdayPrice + ", " + minPrice + ", " + maxPrice);
                stockTradeData.setYesterdayPrice(yesterdayPrice);
                stockTradeData.setMinPrice(minPrice);
                stockTradeData.setMaxPrice(maxPrice);
            } catch (Exception e) {
                MSLog.e("Exception in setMemData: " + e);
                // pass
            }
        }
    }

    private final static String STOCK_REAL_TIME_URL_PREFIX_YAHOO = "https://tw.quote.finance.yahoo.net/quote/q?type=tick&perd=1m&mkt=10&sym=%s";

    public static String getYahooStockPriceUrl(String code) {
        return String.format(Locale.US, STOCK_REAL_TIME_URL_PREFIX_YAHOO, code);
    }
}
