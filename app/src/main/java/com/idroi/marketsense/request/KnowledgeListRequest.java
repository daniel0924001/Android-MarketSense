package com.idroi.marketsense.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.Knowledge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/9/19.
 */

public class KnowledgeListRequest extends Request<ArrayList<Knowledge>> {

    private static final String PARAM_STATUS = "status";
    private static final String PARAM_RESULT = "result";

    private final Response.Listener<ArrayList<Knowledge>> mListener;

    public KnowledgeListRequest(String url, Response.Listener<ArrayList<Knowledge>> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mListener = listener;
    }

    @Override
    protected Response<ArrayList<Knowledge>> parseNetworkResponse(NetworkResponse response) {
        try {
            ArrayList<Knowledge> knowledgeArrayList = parseKnowledgeList(response.data);
            if(knowledgeArrayList != null && knowledgeArrayList.size() != 0) {
                MSLog.i("Knowledge list request success: " + new String(response.data));
                return Response.success(knowledgeArrayList, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_ERROR));
            }
        } catch (Exception exception) {
            return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_ERROR));
        }
    }

    public static ArrayList<Knowledge> parseKnowledgeList(byte[] data) throws JSONException {
        JSONObject jsonObject = new JSONObject(new String(data));

        if(jsonObject.optBoolean(PARAM_STATUS)) {
            ArrayList<Knowledge> knowledgeArrayList = new ArrayList<>();
            JSONArray jsonArray = jsonObject.optJSONArray(PARAM_RESULT);
            for(int i = 0; i < jsonArray.length(); i++) {
                Knowledge knowledge = parseKnowledge(jsonArray.getJSONObject(i));
                if(knowledge != null) {
                    knowledgeArrayList.add(knowledge);
                }
            }
            return knowledgeArrayList;
        } else {
            MSLog.e("knowledge response status is false: " + jsonObject);
            return null;
        }
    }

    private static Knowledge parseKnowledge(JSONObject jsonObject) {
        try {
            return Knowledge.jsonObjectToKnowledge(jsonObject);
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    protected void deliverResponse(ArrayList<Knowledge> response) {
        mListener.onResponse(response);
    }

    public static final String API_URL_KNOWLEDGE_LIST
            = "http://apiv2.infohubapp.com/v1/stock/keywords";
    public static final String API_URL_KNOWLEDGE_KEYWORD
            = "http://apiv2.infohubapp.com/v1/stock/keyword/%s";

    public static String queryKnowledgeList() {
        return API_URL_KNOWLEDGE_LIST + "?timestamp=" + System.currentTimeMillis() / 86400;
    }

    public static String queryKnowledgeKeyword(String keyword) {
        return String.format(API_URL_KNOWLEDGE_KEYWORD, keyword);
    }
}
