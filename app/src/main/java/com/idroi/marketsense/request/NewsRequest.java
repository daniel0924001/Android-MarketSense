package com.idroi.marketsense.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

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
import java.util.Locale;
import java.util.Map;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_REQUEST_NAME;

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
            if (mUrl.contains(PARAM_KEYWORD_ARRAY)) {
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
                News news = News.jsonObjectToNews(newsJsonArray.getJSONObject(i));
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

        if (multipleNewsJsonObject != null) {
            Iterator<String> iterator = multipleNewsJsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                JSONArray newsJsonArray = multipleNewsJsonObject.getJSONArray(key);
                if (newsJsonArray != null) {
                    MSLog.d("stock name: " + key + ", size: " + newsJsonArray.length());
                    for (int i = 0; i < newsJsonArray.length(); i++) {
                        News news = News.jsonObjectToNews(newsJsonArray.getJSONObject(i));
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

        if (jsonResponse.optBoolean(PARAM_CODE) && jsonResponse.opt(PARAM_RESULT) != null) {
            return jsonResponse.optJSONObject(PARAM_RESULT);
        }

        return null;
    }

    public static final String PARAM_STATUS = "&status=";
    public static final String PARAM_LEVEL = "&level=";
    private static final String PARAM_TIMESTAMP = "&timestamp=";
    public static final String PARAM_GTS = "&gts=";

    private static final String API_KEYWORD_URL = "http://apiv2.infohubapp.com/v1/stock/search?";
    public static final String PARAM_KEYWORD_ARRAY = "&keywords[]=";

    public static final String PARAM_STATUS_RISING = "r";
    public static final String PARAM_STATUS_FALLING = "f";

    public static final String API_QUERY_NEWS_URL =
            "http://apiv2.infohubapp.com/v1/stock/news?limit=%d&status=%s&level=%d&random=0&gts=%s";
    public static final String API_QUERY_KEYWORD_NEWS_URL =
            "http://apiv2.infohubapp.com/v1/stock/search?keyword=%s";

    private static final int REFRESH_TIME = 10;

    // start of query news
    public static String queryNewsUrlPrefix(String status, int level, String gts) {
        long gtsLong = Long.valueOf(gts) / (REFRESH_TIME * 1000) * (REFRESH_TIME * 1000);
        return String.format(Locale.US, API_QUERY_NEWS_URL, 300, status, level, String.valueOf(gtsLong));
    }

    public static String queryNewsUrl(Context context, String status,
                                      int level,
                                      boolean isNetworkUrl,
                                      String gts) {
        String urlPrefix = queryNewsUrlPrefix(status, level, gts);
        if (isNetworkUrl || context == null) {
            return urlPrefix +
                    PARAM_TIMESTAMP + (System.currentTimeMillis() / (REFRESH_TIME * 1000));
        } else {
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(urlPrefix, urlPrefix +
                    PARAM_TIMESTAMP + (System.currentTimeMillis() / (REFRESH_TIME * 1000)));
        }
    }

    // start of query keyword news
    public static String queryKeywordNewsUrlPrefix(String keyword) {
        return String.format(Locale.US, API_QUERY_KEYWORD_NEWS_URL, keyword);
    }

    public static String queryKeywordNewsUrl(Context context, String keyword, boolean isNetworkUrl) {
        String urlPrefix = queryKeywordNewsUrlPrefix(keyword);
        if (isNetworkUrl || context == null) {
            return urlPrefix +
                    PARAM_TIMESTAMP + System.currentTimeMillis() / (REFRESH_TIME * 1000);
        } else {
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(urlPrefix, urlPrefix +
                    PARAM_TIMESTAMP + System.currentTimeMillis() / (REFRESH_TIME * 1000));
        }
    }


    // start of query keyword array news
    public static String queryKeywordArrayNewsUrlPrefix() {
        StringBuilder stringBuilder = queryKeywordArrayNewsPrefix();
        if(stringBuilder == null) {
            return null;
        } else {
            return stringBuilder.toString();
        }
    }

    private static StringBuilder queryKeywordArrayNewsPrefix() {
        StringBuilder url = new StringBuilder(API_KEYWORD_URL);
        ArrayList<String> favoriteStocks =
                ClientData.getInstance().getUserProfile().getFavoriteStocks();

        if (favoriteStocks.size() == 0) {
            // user maybe logout
            return null;
        }
        long now = System.currentTimeMillis() / 1000;
        url.append(PARAM_GTS).append(now - 7 * 86400);

        for (int i = 0; i < favoriteStocks.size(); i++) {
            url.append(PARAM_KEYWORD_ARRAY)
                    .append(ClientData.getInstance().getNameFromCode(favoriteStocks.get(i)));
        }
        return url;
    }

    @Nullable
    public static String queryKeywordArrayNewsUrl(Context context, boolean isNetworkUrl) {

        StringBuilder url = queryKeywordArrayNewsPrefix();
        if(url == null) {
            return null;
        }

        // prefix
        String urlPrefix = url.toString();
        // prefix + timestamp
        url.append(PARAM_TIMESTAMP).append(System.currentTimeMillis() / (REFRESH_TIME * 1000));

        if(isNetworkUrl || context == null) {
            return url.toString();
        } else {
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(urlPrefix, url.toString());
        }
    }
}