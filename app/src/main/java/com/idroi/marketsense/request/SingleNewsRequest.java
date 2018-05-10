package com.idroi.marketsense.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.Comment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class SingleNewsRequest extends Request<ArrayList<Comment>> {

    private static final String PARAM_STATUS = "status";
    private static final String PARAM_RESULT = "result";
    private static final String PARAM_COMMENT = "comment";
    private static final String PARAM_COMMENTS = "comments";
    private static final String PARAM_EVENT = "event";

    final static int NEWS_COMMENT_ID = 1;
    final static int STOCK_COMMENT_ID = 2;

    public enum TASK {
        NEWS_COMMENT(NEWS_COMMENT_ID),
        STOCK_COMMENT(STOCK_COMMENT_ID);

        int taskId;
        TASK(int id) {
            taskId = id;
        }

        public int getTaskId() {
            return taskId;
        }
    }

    private final Response.Listener<ArrayList<Comment>> mListener;

    public SingleNewsRequest(int method, String url, Response.Listener<ArrayList<Comment>> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    @Override
    protected Response<ArrayList<Comment>> parseNetworkResponse(NetworkResponse response) {
        try {
            ArrayList<Comment> commentArrayList = commentsParseResponse(response.data);
            if(commentArrayList != null) {
                MSLog.i("Single News Request success and has comment size: " + commentArrayList.size());
                return Response.success(commentArrayList, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_NO_DATA));
            }
        } catch (JSONException e) {
            return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_ERROR));
        }
    }

    private static ArrayList<Comment> commentsParseResponse(byte[] data) throws JSONException {
        JSONArray commentsJsonArray = getCommentResult(new JSONObject(new String(data)));

        if(commentsJsonArray != null) {
            ArrayList<Comment> commentArrayList = new ArrayList<>();
            for(int i = 0; i < commentsJsonArray.length(); i++) {
                if(commentsJsonArray.getJSONObject(i).optString(PARAM_EVENT).equals(PARAM_COMMENT)) {
                    Comment comment = Comment.jsonObjectToComment(commentsJsonArray.getJSONObject(i));
                    if (comment != null) {
                        commentArrayList.add(comment);
                    }
                }
            }
            return commentArrayList;
        }

        return null;
    }

    private static JSONArray getCommentResult(JSONObject jsonResponse) {

        if(jsonResponse.optBoolean(PARAM_STATUS) &&
                jsonResponse.optJSONObject(PARAM_RESULT) != null &&
                jsonResponse.optJSONObject(PARAM_RESULT).optJSONArray(PARAM_COMMENTS) != null) {
            return jsonResponse.optJSONObject(PARAM_RESULT).optJSONArray(PARAM_COMMENTS);
        }

        return null;
    }

    @Override
    protected void deliverResponse(ArrayList<Comment> response) {
        mListener.onResponse(response);
    }

    private static final String API_URL_NEWS = "http://apiv2.infohubapp.com/v1/stock/news/";
    private static final String API_URL_STOCK = "http://apiv2.infohubapp.com/v1/stock/code/";

    public static String querySingleNewsUrl(String newsId, TASK task) {
        switch (task.getTaskId()) {
            case NEWS_COMMENT_ID:
                return API_URL_NEWS + newsId + "?timestamp=" + System.currentTimeMillis() / (300 * 1000);
            case STOCK_COMMENT_ID:
                return API_URL_STOCK + newsId + "?timestamp=" + System.currentTimeMillis() / (300 * 1000);
        }
        return null;
    }
}
