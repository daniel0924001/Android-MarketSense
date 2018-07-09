package com.idroi.marketsense.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class CommentAndVoteRequest extends Request<CommentAndVote> {

    private static final String PARAM_STATUS = "status";
    private static final String PARAM_RESULT = "result";
    private static final String PARAM_COMMENT = "comment";
    private static final String PARAM_COMMENTS = "comments";
    private static final String PARAM_EVENT = "event";
    private static final String PARAM_RAISE_NUMBER = "raise";
    private static final String PARAM_FALL_NUMBER = "fall";
    private static final String PARAM_PRED = "prediction";
    private static final String PARAM_VOTING = "voting";

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

    private final Response.Listener<CommentAndVote> mListener;

    public CommentAndVoteRequest(int method, String url, Response.Listener<CommentAndVote> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    @Override
    protected Response<CommentAndVote> parseNetworkResponse(NetworkResponse response) {
        try {
            CommentAndVote commentAndVote = commentsParseResponse(response.data);
            if(commentAndVote != null) {
                MSLog.i("Comment Request success and has comment size: " + commentAndVote.getCommentSize());
                return Response.success(commentAndVote, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_NO_DATA));
            }
        } catch (JSONException e) {
            return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_ERROR));
        }
    }

    private static CommentAndVote commentsParseResponse(byte[] data) throws JSONException {
        CommentAndVote commentAndVote = new CommentAndVote();
        JSONArray commentsJsonArray = getCommentResult(new JSONObject(new String(data)), commentAndVote);

        if(commentsJsonArray != null) {
            for(int i = 0; i < commentsJsonArray.length(); i++) {
                if(commentsJsonArray.getJSONObject(i).optString(PARAM_EVENT).equals(PARAM_COMMENT)) {
                    Comment comment = Comment.jsonObjectToComment(commentsJsonArray.getJSONObject(i));
                    if (comment != null) {
                        commentAndVote.addComment(comment);
                    }
                }
            }
            return commentAndVote;
        }

        return null;
    }

    private static JSONArray getCommentResult(JSONObject jsonResponse, CommentAndVote commentAndVote) {

        if(jsonResponse.optBoolean(PARAM_STATUS) && jsonResponse.optJSONArray(PARAM_RESULT) != null) {
            // API_URL_COMMENT_EVENT or API_URL_COMMENT_EVENT_FOR_CODE
            return jsonResponse.optJSONArray(PARAM_RESULT);
        }

        if(jsonResponse.optBoolean(PARAM_STATUS) && jsonResponse.optJSONObject(PARAM_RESULT) != null) {
            // API_URL_NEWS or API_URL_STOCK
            commentAndVote.setRaiseNumber(jsonResponse.optJSONObject(PARAM_RESULT).optInt(PARAM_RAISE_NUMBER));
            commentAndVote.setFallNumber(jsonResponse.optJSONObject(PARAM_RESULT).optInt(PARAM_FALL_NUMBER));
            commentAndVote.setPrediction(jsonResponse.optJSONObject(PARAM_RESULT).optDouble(PARAM_PRED));
            commentAndVote.setVoting(jsonResponse.optJSONObject(PARAM_RESULT).optDouble(PARAM_VOTING));
            if(jsonResponse.optJSONObject(PARAM_RESULT).optJSONArray(PARAM_COMMENTS) != null) {
                return jsonResponse.optJSONObject(PARAM_RESULT).optJSONArray(PARAM_COMMENTS);
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    protected void deliverResponse(CommentAndVote response) {
        mListener.onResponse(response);
    }

    private static final String API_URL_NEWS = "http://apiv2.infohubapp.com/v1/stock/news/";
    private static final String API_URL_STOCK = "http://apiv2.infohubapp.com/v1/stock/code/";
    private static final String API_URL_COMMENT_EVENT
            = "http://apiv2.infohubapp.com/v1/stock/event/comments?timestamp=%s&limit=%d";
    private static final String API_URL_COMMENT_EVENT_FOR_CODE
            = "http://apiv2.infohubapp.com/v1/stock/event/comments?timestamp=%s&limit=%d&s=%s";

    public static String querySingleNewsUrl(String newsId, TASK task) {
        switch (task.getTaskId()) {
            case NEWS_COMMENT_ID:
                return API_URL_NEWS + newsId + "?timestamp=" + System.currentTimeMillis();
            case STOCK_COMMENT_ID:
                return API_URL_STOCK + newsId + "?timestamp=" + System.currentTimeMillis();
        }
        return null;
    }

    public static String queryCommentsEvent() {
        return String.format(Locale.US,
                API_URL_COMMENT_EVENT, (System.currentTimeMillis() / 1000), 300);
    }

    public static String queryCommentsEventForStockCode(String code) {
        return String.format(Locale.US,
                API_URL_COMMENT_EVENT_FOR_CODE,
                (System.currentTimeMillis() / 1000), 300, code);
    }
}
