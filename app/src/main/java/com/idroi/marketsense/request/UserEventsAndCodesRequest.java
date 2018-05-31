package com.idroi.marketsense.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.Event;
import com.idroi.marketsense.util.MarketSenseUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;

/**
 * Created by daniel.hsieh on 2018/5/11.
 */

public class UserEventsAndCodesRequest extends Request<Void> {

    private static final String PARAM_STATUS = "status";
    private static final String PARAM_RESULT = "result";

    private static final String PARAM_EVENTS = "events";

    private static final String PARAM_STOCK_CODES = "stock_codes";
    private static final String PARAM_STOCK_CODE = "stock_code";

    private final Response.Listener<Void> mListener;

    public UserEventsAndCodesRequest(int method, String url, Response.Listener<Void> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    @Override
    protected Response<Void> parseNetworkResponse(NetworkResponse response) {

        try {
            boolean setPreferenceSuccess = setUserEventAndCodes(new JSONObject(new String(response.data)));
            if(setPreferenceSuccess) {
                return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_NO_DATA));
            }
        } catch (JSONException e) {
            return Response.error(new MarketSenseNetworkError(MarketSenseError.JSON_PARSED_ERROR));
        }
    }

    @Override
    protected void deliverResponse(Void response) {
        mListener.onResponse(response);
    }

    private static boolean setUserEventAndCodes(JSONObject jsonResponse) {
        MSLog.d("user preference: " + jsonResponse);
        if(jsonResponse.optBoolean(PARAM_STATUS) &&
                jsonResponse.optJSONObject(PARAM_RESULT) != null &&
                jsonResponse.optJSONObject(PARAM_RESULT).optJSONArray(PARAM_STOCK_CODES) != null) {

            final ClientData clientData = ClientData.getInstance();
            clientData.getUserProfile().clearFavoriteStock();

            // favorite stock list
            JSONArray codesJsonArray = jsonResponse.optJSONObject(PARAM_RESULT).optJSONArray(PARAM_STOCK_CODES);
            for(int i = 0; i < codesJsonArray.length(); i++) {
                if(codesJsonArray.optJSONObject(i) != null) {
                    String code = codesJsonArray.optJSONObject(i).optString(PARAM_STOCK_CODE);
                    if(code != null) {
                        MSLog.d("add favorite code: " + code);
                        clientData.getUserProfile().addFavoriteStock(code);
                    }
                }
            }

            MarketSenseUtils.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clientData.getUserProfile().notifyUserProfile(NOTIFY_ID_FAVORITE_LIST);
                }
            });

            // event
            JSONArray eventsJsonArray = jsonResponse.optJSONObject(PARAM_RESULT).optJSONArray(PARAM_EVENTS);
            for(int i = 0; i < eventsJsonArray.length(); i++) {
                if(eventsJsonArray.optJSONObject(i) != null) {
                    Event event = Event.JsonObjectToEvent(eventsJsonArray.optJSONObject(i));
//                    MSLog.d("event: " + event.toString());
                    clientData.getUserProfile().addEvent(event);
                }
            }
            return true;
        }
        return false;
    }

    private static final String API_URL_SELF_CHOICES = "http://apiv2.infohubapp.com/v1/stock/user/";

    public static String querySelfStockList() {
        String token = ClientData.getInstance().getUserToken();
        return API_URL_SELF_CHOICES + token + "?timestamp=" + System.currentTimeMillis();
    }
}
