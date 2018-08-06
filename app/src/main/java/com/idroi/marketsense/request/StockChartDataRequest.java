package com.idroi.marketsense.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.StockBaseData;
import com.idroi.marketsense.data.StockTaData;
import com.idroi.marketsense.data.StockTickData;
import com.idroi.marketsense.data.StockTradeData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by daniel.hsieh on 2018/5/21.
 */

public class StockChartDataRequest extends Request<StockTradeData> {

    public static final String PARAM_TICK = "tick";
    public static final String PARAM_TA = "ta";
    private static final String PARAM_MEM = "mem";
    private static final String PARAM_OPEN = "126";
    private static final String PARAM_HIGH = "130";
    private static final String PARAM_LOW = "131";
    private static final String PARAM_YESTERDAY = "129";
    private static final String PARAM_TODAY_TOTAL_VOLUME = "404";
    private static final String PARAM_MIN_PRICE = "133";
    private static final String PARAM_MAX_PRICE = "132";
    private static final String PARAM_REAL_PRICE = "125";
    private static final String PARAM_DIFF_PRICE = "184";
    private static final String PARAM_DIFF_PERCENTAGE = "185";
    private static final String PARAM_TRADE_DAY = "TradeDay";
    private static final String PARAM_T = "t";
    private static final String PARAM_P = "p";
    private static final String PARAM_V = "v";
    private static final String PARAM_H = "h";
    private static final String PARAM_L = "l";
    private static final String PARAM_O = "o";
    private static final String PARAM_C = "c";

    private final Response.Listener<StockTradeData> mListener;

    public StockChartDataRequest(String url, Response.Listener<StockTradeData> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mListener = listener;
    }

    @Override
    protected Response<StockTradeData> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data);
            jsonString = jsonString.substring(5, jsonString.length() - 1);
            JSONObject jsonData = new JSONObject(jsonString);

            MSLog.d("Yahoo stock trade data: " + jsonData);
            StockTradeData stockTradeData = null;
            if(jsonData.has(PARAM_TICK)) {
                stockTradeData = new StockTradeData(StockTradeData.STOCK_TRADE_DATA_TYPE_TICK);
                setTickData(stockTradeData, jsonData);
            } else if(jsonData.has(PARAM_TA)) {
                stockTradeData = new StockTradeData(StockTradeData.STOCK_TRADE_DATA_TYPE_TA);
                setTaData(stockTradeData, jsonData);
            }
            setMemData(stockTradeData, jsonData);

            if(stockTradeData != null && stockTradeData.size() > 0) {
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

    private static void setTaData(StockTradeData stockTradeData, JSONObject jsonResponse) {
        JSONArray taArray = jsonResponse.optJSONArray(PARAM_TA);
        if(taArray != null && taArray.length() > 0) {
            double taMinPrice = Double.MAX_VALUE;
            double taMaxPrice = Double.MIN_VALUE;
            ArrayList<StockBaseData> stockTaDataArrayList = new ArrayList<>(taArray.length());
            for(int i = 0; i < taArray.length(); i++) {
                try {
                    JSONObject ta = taArray.getJSONObject(i);
                    if(ta != null) {
                        double h = ta.getDouble(PARAM_H);
                        double l = ta.getDouble(PARAM_L);
                        JSONObject yesterdayTa = taArray.optJSONObject(i - 1);
                        Double yesterdayClose = null;
                        if(yesterdayTa != null) {
                            yesterdayClose = yesterdayTa.getDouble(PARAM_C);
                        }
                        StockTaData stockTaData = new StockTaData(
                                ta.getLong(PARAM_T),
                                ta.getInt(PARAM_V),
                                h,
                                l,
                                ta.getDouble(PARAM_O),
                                ta.getDouble(PARAM_C),
                                yesterdayClose);
                        stockTaDataArrayList.add(stockTaData);
                        if(h > taMaxPrice) {
                            taMaxPrice = h;
                        }
                        if(l < taMinPrice) {
                            taMinPrice = l;
                        }
                    }
                } catch (Exception e) {
                    MSLog.e("Exception in setTaData: " + e);
                }
            }
            stockTradeData.setStockTransactionData(stockTaDataArrayList);
            stockTradeData.setTaHighPrice((float)taMaxPrice);
            stockTradeData.setTaLowPrice((float)taMinPrice);
        }
    }

    private static void setTickData(StockTradeData stockTradeData, JSONObject jsonResponse) {
        JSONArray tickArray = jsonResponse.optJSONArray(PARAM_TICK);
        if(tickArray != null && tickArray.length() > 0) {
            ArrayList<StockBaseData> stockTickDataArrayList = new ArrayList<>(tickArray.length());
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
                }
            }

            // append no transaction data
            try {
                Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
                int hour = now.get(Calendar.HOUR_OF_DAY);
                int minute = now.get(Calendar.MINUTE);
                int appendNumber = (hour - 9) * 60 + minute;
                JSONObject lastTick = tickArray.getJSONObject(tickArray.length() - 1);
                for (int i = tickArray.length(); i < Math.min(appendNumber, 270); i++) {
                    StockTickData stockTickData
                            = new StockTickData((long)0, lastTick.getDouble(PARAM_P), 0, i);
                    stockTickDataArrayList.add(stockTickData);
                }
            } catch (Exception exception) {
                MSLog.e("Exception in append no transaction data: " + exception);
            }

            stockTradeData.setStockTransactionData(stockTickDataArrayList);
        }
    }

    private static void setMemData(StockTradeData stockTradeData, JSONObject jsonResponse) {
        JSONObject data = jsonResponse.optJSONObject(PARAM_MEM);
        if(data != null) {
            try {
                float yesterdayPrice = (float) data.optDouble(PARAM_YESTERDAY);
                float openPrice = (float) data.optDouble(PARAM_OPEN);
                float highPrice = (float) data.optDouble(PARAM_HIGH);
                float lowPrice = (float) data.optDouble(PARAM_LOW);
                float minPrice = (float) data.optDouble(PARAM_MIN_PRICE);
                float maxPrice = (float) data.optDouble(PARAM_MAX_PRICE);
                float todayTotalVolume = (float) data.optDouble(PARAM_TODAY_TOTAL_VOLUME);
                float realPrice = (float) data.optDouble(PARAM_REAL_PRICE);
                float diffPrice = (float) data.optDouble(PARAM_DIFF_PRICE);
                float diffPercentage = (float) data.optDouble(PARAM_DIFF_PERCENTAGE);
                stockTradeData.setOpenPrice(openPrice);
                stockTradeData.setHighPrice(highPrice);
                stockTradeData.setLowPrice(lowPrice);
                stockTradeData.setYesterdayPrice(yesterdayPrice);
                stockTradeData.setMinPrice(minPrice);
                stockTradeData.setMaxPrice(maxPrice);
                stockTradeData.setRealPrice(realPrice);
                stockTradeData.setDiffPrice(diffPrice);
                stockTradeData.setDiffPercentage(diffPercentage);
                stockTradeData.setTickTotalVolume(todayTotalVolume);

                if(data.has(PARAM_TRADE_DAY)) {
                    stockTradeData.setTickTradeDate(data.optString(PARAM_TRADE_DAY));
                }
            } catch (Exception e) {
                MSLog.e("Exception in setMemData: " + e);
                // pass
            }
        }
    }

    public static final String TA_TYPE_5M = "5m";
    public static final String TA_TYPE_10M = "10m";
    public static final String TA_TYPE_30M = "30m";
    public static final String TA_TYPE_DAY = "d";
    public static final String TA_TYPE_WEEK = "w";
    public static final String TA_TYPE_MONTH = "m";

    private final static String STOCK_REAL_TIME_URL_PREFIX_YAHOO = "https://tw.quote.finance.yahoo.net/quote/q?type=tick&perd=1m&mkt=10&sym=%s";
    private final static String STOCK_TA_URL_PREFIX_YAHOO = "https://tw.quote.finance.yahoo.net/quote/q?type=ta&perd=%s&mkt=10&sym=%s";

    public static String getYahooTickStockPriceUrl(String code) {
        return String.format(Locale.US, STOCK_REAL_TIME_URL_PREFIX_YAHOO, code);
    }

    public static String getYahooTaStockPriceUrl(String type, String code) {
        return String.format(Locale.US, STOCK_TA_URL_PREFIX_YAHOO, type, code);
    }
}
