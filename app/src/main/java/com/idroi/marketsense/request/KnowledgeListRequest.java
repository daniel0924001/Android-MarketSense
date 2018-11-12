package com.idroi.marketsense.request;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.Knowledge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_REQUEST_NAME;

/**
 * Created by daniel.hsieh on 2018/9/19.
 */

public class KnowledgeListRequest extends Request<HashMap<String, ArrayList<Knowledge>>> {

    private static final String PARAM_STATUS = "status";
    private static final String PARAM_RESULT = "result";

    private final Response.Listener<HashMap<String, ArrayList<Knowledge>>> mListener;

    public KnowledgeListRequest(String url, Response.Listener<HashMap<String, ArrayList<Knowledge>>> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        mListener = listener;
    }

    @Override
    protected Response<HashMap<String, ArrayList<Knowledge>>> parseNetworkResponse(NetworkResponse response) {
        try {
            HashMap<String, ArrayList<Knowledge>> knowledgeArrayList = parseKnowledgeList(response.data);
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

    public static HashMap<String, ArrayList<Knowledge>> parseKnowledgeList(byte[] data) throws JSONException {
        JSONObject jsonObject = new JSONObject(new String(data));

        if(jsonObject.optBoolean(PARAM_STATUS)) {
            ArrayList<Knowledge> knowledgeArrayList = new ArrayList<>();
            JSONArray jsonArray = jsonObject.optJSONArray(PARAM_RESULT);

            ClientData clientData = ClientData.getInstance();

            for(int i = 0; i < jsonArray.length(); i++) {
                Knowledge knowledge = parseKnowledge(jsonArray.getJSONObject(i));
                if(knowledge != null) {
                    knowledgeArrayList.add(knowledge);
                    clientData.setKnowledgeHashMap(knowledge);
                }
            }
            return convertListToMapByCategory(knowledgeArrayList);
        } else {
            MSLog.e("knowledge response status is false: " + jsonObject);
            return null;
        }
    }

    public static HashMap<String, ArrayList<Knowledge>> convertListToMapByCategory(ArrayList<Knowledge> knowledgeArrayList) {
        HashMap<String, ArrayList<Knowledge>> knowledgeHashMap = new HashMap<>();
        for(Knowledge knowledge : knowledgeArrayList) {
            String category = knowledge.getCategory();
            ArrayList<Knowledge> categoryKnowledge = knowledgeHashMap.get(category);
            if(categoryKnowledge == null) {
                categoryKnowledge = new ArrayList<>();
            }
            categoryKnowledge.add(knowledge);
            knowledgeHashMap.put(category, categoryKnowledge);
        }
        return knowledgeHashMap;
    }

    private static Knowledge parseKnowledge(JSONObject jsonObject) {
        try {
            return Knowledge.jsonObjectToKnowledge(jsonObject);
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    protected void deliverResponse(HashMap<String, ArrayList<Knowledge>> response) {
        mListener.onResponse(response);
    }

    public static final String API_URL_KNOWLEDGE_LIST
            = "http://apiv2.infohubapp.com/v1/stock/keywords";
    public static final String API_URL_KNOWLEDGE_KEYWORD
            = "http://apiv2.infohubapp.com/v1/stock/keyword/%s";

    public static String queryKnowledgeList(Context context, boolean isNetworkUrl) {
        if(isNetworkUrl) {
            return API_URL_KNOWLEDGE_LIST + "?timestamp=" + System.currentTimeMillis() / (86400 * 1000);
        } else {
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(API_URL_KNOWLEDGE_LIST,
                    API_URL_KNOWLEDGE_LIST + "?timestamp=" + System.currentTimeMillis() / (86400 * 1000));
        }
    }

    public static String queryKnowledgeKeyword(String keyword) {
        return String.format(API_URL_KNOWLEDGE_KEYWORD, keyword);
    }
}
