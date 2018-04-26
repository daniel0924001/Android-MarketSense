package com.idroi.marketsense.request;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.Stock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by daniel.hsieh on 2018/4/23.
 */

public class StockRequest extends Request<ArrayList<Stock>> {

    private static final String PARAM_CODE = "Code";
    private static final String PARAM_RESULT = "Result";
    private static final String PARAM_NEWS_RESULT = "Stocks";

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
            if(stockArrayList != null) {
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
                stockArrayList.add(Stock.jsonObjectToStock(stocksJsonArray.getJSONObject(i)));
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

        if(jsonResponse.optInt(PARAM_CODE) == 0 && jsonResponse.opt(PARAM_RESULT) != null) {
            JSONObject jsonResult = jsonResponse.optJSONObject(PARAM_RESULT);
                return jsonResult.optJSONArray(PARAM_NEWS_RESULT);
        }

        return null;
    }

    private static final String API_URL = "http://apiv2.infohubapp.com/v1/stocks";

    public static String queryStockList() {
        return API_URL;
    }
}
