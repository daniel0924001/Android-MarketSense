package com.idroi.marketsense.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.StatisticDataItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/7/24.
 */

public class CriticalStatisticsRequest extends Request<ArrayList<StatisticDataItem>> {

    private static final String COUNT = "count";
    private static final String ITEMS = "items";

    private final Response.Listener<ArrayList<StatisticDataItem>> mListener;
    private final ArrayList<StatisticDataItem> mDataItems;

    public CriticalStatisticsRequest(String url,
                                     ArrayList<StatisticDataItem> dataItemsWithOnlyKey,
                                     Response.Listener<ArrayList<StatisticDataItem>> listener,
                                     Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mListener = listener;
        mDataItems = dataItemsWithOnlyKey;
    }

    @Override
    protected Response<ArrayList<StatisticDataItem>> parseNetworkResponse(NetworkResponse response) {
        try {
            JSONObject dataJsonObject = getJsonObjectForData(new JSONObject(new String(response.data)), 0);
            convertJsonObjectToCriticalStatisticalData(dataJsonObject, mDataItems);
            MSLog.i("Critical Statistics Request success: " + dataJsonObject.toString());
            return Response.success(mDataItems, HttpHeaderParser.parseCacheHeaders(response));
        } catch (JSONException e) {
            return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_ERROR));
        }
    }

    private static JSONObject getJsonObjectForData(JSONObject jsonObject, int index) throws JSONException{
        int size = jsonObject.getInt(COUNT);
        JSONArray jsonArray = jsonObject.getJSONArray(ITEMS);

        if(index < 0 || index >= size || index >= jsonArray.length()) {
            throw new JSONException("index is invalid");
        }

        return jsonArray.getJSONObject(index);
    }

    private static void convertJsonObjectToCriticalStatisticalData(
            JSONObject jsonObject,
            ArrayList<StatisticDataItem> dataItemsWithOnlyKey) {
        StatisticDataItem.initStatisticData(dataItemsWithOnlyKey, jsonObject);
    }

    @Override
    protected void deliverResponse(ArrayList<StatisticDataItem> response) {
        mListener.onResponse(response);
    }

    private static final String URL_STOCK_CRITICAL_STATISTICS
            = "https://tw.screener.finance.yahoo.net/screener/ws?f=j&ShowID=%s";

    public static String getUrlStockCriticalStatistics(String code) {
        return String.format(URL_STOCK_CRITICAL_STATISTICS , code);
    }
}
