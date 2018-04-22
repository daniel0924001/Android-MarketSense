package com.idroi.marketsense.request;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by daniel.hsieh on 2018/4/22.
 */

public class NewsRequest extends Request<ArrayList<News>> {

    private static final String PARAM_CODE = "Code";
    private static final String PARAM_RESULT = "Result";
    private static final String PARAM_MSG = "message";
    private static final String PARAM_NEWS_RESULT = "news_results";
    private static final String PARAM_OK = "OK";

    private final Map<String, String> mHeader;
    private final Response.Listener<ArrayList<News>> mListener;

    public NewsRequest(int method, String url, Map<String, String> headers, Response.Listener<ArrayList<News>> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mHeader = headers;
        mListener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeader != null ? mHeader : super.getHeaders();
    }

    @Override
    protected Response<ArrayList<News>> parseNetworkResponse(NetworkResponse response) {
        try {
            JSONArray newsJsonArray = getNewsResult(new JSONObject(new String(response.data)));
            if(newsJsonArray != null) {
                ArrayList<News> newsArrayList = new ArrayList<>();
                for(int i = 0; i < newsJsonArray.length(); i++) {
                    newsArrayList.add(News.JsonObjectToNews(newsJsonArray.getJSONObject(i)));
                }
                return Response.success(newsArrayList, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_NO_DATA));
            }

        } catch (JSONException e) {
            return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_ERROR));
        }
    }

    @Override
    protected void deliverResponse(ArrayList<News> response) {
        mListener.onResponse(response);
    }

    private JSONArray getNewsResult(JSONObject jsonResponse) {

        if(jsonResponse.optInt(PARAM_CODE) == 0 && jsonResponse.opt(PARAM_RESULT) != null) {
            JSONObject jsonResult = jsonResponse.optJSONObject(PARAM_RESULT);
            if(jsonResult.opt(PARAM_MSG).equals(PARAM_OK)) {
                return jsonResult.optJSONArray(PARAM_NEWS_RESULT);
            }
        }

        return null;
    }

}
