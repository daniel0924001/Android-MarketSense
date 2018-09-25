package com.idroi.marketsense.request;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.Stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_REQUEST_NAME;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockRequest extends Request<ArrayList<Stock>> {

    private static final String PARAM_STATUS = "status";
    private static final String PARAM_RESULT = "result";

    private static final long STOCK_REQUEST_SOFT_TTL = 30 * 1000;
    private static final long STOCK_REQUEST_TTL = 10 * 60 * 1000;

    private final Map<String, String> mHeader;
    private final Response.Listener<ArrayList<Stock>> mListener;

    public StockRequest(int method, String url, Map<String, String> headers, Response.Listener<ArrayList<Stock>> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mHeader = headers;
        mListener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeader != null ? mHeader : super.getHeaders();
    }

    @Override
    protected Response<ArrayList<Stock>> parseNetworkResponse(NetworkResponse response) {

        try {
            ArrayList<Stock> stockArrayList = stockParseResponse(response.data);
            if(stockArrayList != null && stockArrayList.size() != 0) {
                MSLog.i("Stock Request success: " + new String(response.data));

                ClientData clientData = ClientData.getInstance();
                if(clientData != null) {
                    clientData.updateClockInformation();
                }

                return Response.success(stockArrayList, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_NO_DATA));
            }
        } catch (JSONException e) {
            return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_ERROR));
        }
    }

    public static ArrayList<Stock> stockParseResponse(byte[] data) throws JSONException {
        JSONArray stocksJsonArray = getStockResult(new JSONObject(new String(data)));

        if(stocksJsonArray != null) {
            ArrayList<Stock> stockArrayList = new ArrayList<>();
            for(int i = 0; i < stocksJsonArray.length(); i++) {
                Stock stock = Stock.jsonObjectToStock(stocksJsonArray.getJSONObject(i), true);
                if(stock != null) {
                    stockArrayList.add(stock);
                }
            }
            return stockArrayList;
        }

        return null;
    }

    @Override
    protected void deliverResponse(ArrayList<Stock> response) {
        mListener.onResponse(response);
    }

    private static JSONArray getStockResult(JSONObject jsonResponse) {

        if(jsonResponse.optBoolean(PARAM_STATUS) && jsonResponse.opt(PARAM_RESULT) != null) {
            return jsonResponse.optJSONArray(PARAM_RESULT);
        }

        return null;
    }

    public static final String MODE_WPCT = "wpct";

    public static final String API_URL = "http://apiv2.infohubapp.com/v1/stock/prices";
    public static final String API_URL_WITH_MODE = "http://apiv2.infohubapp.com/v1/stock/prices?mode=%s";

    public static String queryStockListWithMode(Context context, boolean isNetworkUrl, String mode) {

        String url = String.format(API_URL_WITH_MODE, mode);

        if(isNetworkUrl) {
            return url + "&timestamp=" + System.currentTimeMillis() / (STOCK_REQUEST_SOFT_TTL);
        } else {
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(url, null);
        }
    }

    public static String queryStockList(Context context, boolean isNetworkUrl) {
        if(isNetworkUrl && ClientData.getInstance().isWorkDayAndStockMarketIsOpen()) {
            return API_URL + "?timestamp=" + System.currentTimeMillis() / (STOCK_REQUEST_SOFT_TTL);
        } else {
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(API_URL, API_URL + "?timestamp=" + System.currentTimeMillis() / (STOCK_REQUEST_SOFT_TTL));
        }
    }
}
