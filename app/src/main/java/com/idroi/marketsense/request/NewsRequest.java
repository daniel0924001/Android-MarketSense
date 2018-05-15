package com.idroi.marketsense.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by daniel.hsieh on 2018/4/22.
 */

public class NewsRequest extends Request<ArrayList<News>> {

    private static final String PARAM_CODE = "status";
    private static final String PARAM_RESULT = "result";

    private final String mUrl;
    private final Map<String, String> mHeader;
    private final Response.Listener<ArrayList<News>> mListener;

    public NewsRequest(int method, String url, Map<String, String> headers, Response.Listener<ArrayList<News>> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mHeader = headers;
        mListener = listener;
        mUrl = url;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeader != null ? mHeader : super.getHeaders();
    }

    @Override
    protected Response<ArrayList<News>> parseNetworkResponse(NetworkResponse response) {

        try {
            ArrayList<News> newsArrayList = null;

            // there are two formats
            if(mUrl.contains(PARAM_KEYWORD_ARRAY)) {
                newsArrayList = multipleNewsParseResponse(response.data);
            } else {
                newsArrayList = newsParseResponse(response.data);
            }

            if (newsArrayList != null && newsArrayList.size() != 0) {
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

    public static ArrayList<News> newsParseResponse(byte[] data) throws JSONException {
        JSONArray newsJsonArray = getNewsResult(new JSONObject(new String(data)));

        if (newsJsonArray != null) {
            ArrayList<News> newsArrayList = new ArrayList<>();
            for (int i = 0; i < newsJsonArray.length(); i++) {
                News news = News.JsonObjectToNews(newsJsonArray.getJSONObject(i));
                if (news != null) {
                    newsArrayList.add(news);
                }
            }
            return newsArrayList;
        }

        return null;
    }

    // "result": []
    @Nullable
    private static JSONArray getNewsResult(JSONObject jsonResponse) {

        if (jsonResponse.optBoolean(PARAM_CODE) && jsonResponse.opt(PARAM_RESULT) != null) {
            return jsonResponse.optJSONArray(PARAM_RESULT);
        }

        return null;
    }

    // "result": {
    //      "全新": [],
    //      "國巨": []
    // }
    @Nullable
    public static ArrayList<News> multipleNewsParseResponse(byte[] data) throws JSONException {
        ArrayList<News> newsArrayList = new ArrayList<>();
        JSONObject multipleNewsJsonObject = getMultipleNewsResult(new JSONObject(new String(data)));

        if(multipleNewsJsonObject != null) {
            Iterator<String> iterator = multipleNewsJsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                JSONArray newsJsonArray = multipleNewsJsonObject.getJSONArray(key);
                if (newsJsonArray != null) {
                    MSLog.d("stock name: " + key + ", size: " + newsJsonArray.length());
                    for (int i = 0; i < newsJsonArray.length(); i++) {
                        News news = News.JsonObjectToNews(newsJsonArray.getJSONObject(i));
                        if (news != null) {
                            newsArrayList.add(news);
                        }
                    }
                }
            }
            return newsArrayList;
        }

        return null;
    }

    private static JSONObject getMultipleNewsResult(JSONObject jsonResponse) {

        if(jsonResponse.optBoolean(PARAM_CODE) && jsonResponse.opt(PARAM_RESULT) != null) {
            return jsonResponse.optJSONObject(PARAM_RESULT);
        }

        return null;
    }

    private static final String API_URL = "http://apiv2.infohubapp.com/v1/stock/news?";
    private static final String PARAM_LIMIT = "&limit=";
    public static final String PARAM_STATUS = "&status=";
    public static final String PARAM_LEVEL = "&level=";
    private static final String PARAM_RANDOM = "&random=";
    private static final String PARAM_TIMESTAMP = "&timestamp=";
    private static final String PARAM_MAGIC_NUM = "&magic_number=";

    private static final String API_KEYWORD_URL = "http://apiv2.infohubapp.com/v1/stock/search?";
    private static final String PARAM_KEYWORD = "&keyword=";
    public static final String PARAM_KEYWORD_ARRAY = "&keywords[]=";

    public static final String PARAM_STATUS_RISING = "r";
    public static final String PARAM_STATUS_FALLING = "f";

    public static String queryNewsURL(String status,
                                      int level) {
        return API_URL + PARAM_LIMIT + 300 +
                PARAM_STATUS + status +
                PARAM_LEVEL + level +
                PARAM_RANDOM + "0" +
                PARAM_TIMESTAMP + (System.currentTimeMillis() / (300 * 1000));
    }

    public static String queryKeywordNewsURL(String keyword) {

        return API_KEYWORD_URL + PARAM_KEYWORD + keyword +
                PARAM_TIMESTAMP + System.currentTimeMillis() / (300 * 1000);
    }


    @Nullable
    public static String queryKeywordArrayNewsURL() {
        StringBuilder url = new StringBuilder(API_KEYWORD_URL);
        ArrayList<String> favoriteStocks =
                ClientData.getInstance().getUserProfile().getFavoriteStocks();

        if(favoriteStocks.size() == 0) {
            // user maybe logout
            return null;
        }

        for(int i = 0; i < favoriteStocks.size(); i++) {
            url.append(PARAM_KEYWORD_ARRAY)
                    .append(ClientData.getInstance().getNameFromCode(favoriteStocks.get(i)));
        }
        url.append(PARAM_TIMESTAMP).append(System.currentTimeMillis() / (300 * 1000));
        return url.toString();
    }

    public static String appendMagicString(String url, int magic) {
        url = url + PARAM_MAGIC_NUM + magic;
        return url;
    }
}
