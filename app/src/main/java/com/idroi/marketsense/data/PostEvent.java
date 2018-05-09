package com.idroi.marketsense.data;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.datasource.Networking;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by daniel.hsieh on 2018/5/7.
 */

public class PostEvent {

    public enum Event {
        VOTING("voting"),
        VOTE_RAISE("raise"),
        VOTE_FALL("fall"),
        COMMENT("comment");

        private String mEvent;

        Event(String event) {
            mEvent = event;
        }

        public String getEventName() {
            return mEvent;
        }
    }

    private static final String EVENT_POST_URL = "http://apiv2.infohubapp.com/v1/stock/user/event";
    private static final String POST_FIELD_USER_ID = "user_id";
    private static final String POST_FIELD_EVENT = "event";
    private static final String POST_FIELD_EVENT_CONTENT = "event_content";
    private static final String POST_FIELD_EVENT_DETAIL = "event_detail";
    private static final String POST_FIELD_EVENT_TYPE = "event_type";
    private static final String POST_FIELD_EVENT_VALUE = "event_value";
    private static final String POST_FIELD_EVENT_TARGET = "event_target";

    private static final String NEWS_CONST = "news";
    private static final String STOCK_CONST = "stock";

    private String mUserId;
    private String mEvent;
    private String mEventContent;
    private String mEventType;
    private Object mEventValue; // String or Integer
    private String mEventDetail;
    private String mEventTarget;

    private PostEvent(String userId, Event event) {
        mUserId = userId;
        mEvent = event.getEventName();
    }

    private PostEvent setEventContent(String eventContent) {
        mEventContent = eventContent;
        return this;
    }

    private PostEvent setEventType(String eventType) {
        mEventType = eventType;
        return this;
    }

    private PostEvent setEventValueInteger(Integer eventValue) {
        mEventValue = eventValue;
        return this;
    }

    private PostEvent setEventValueString(String eventValue) {
        mEventValue = eventValue;
        return this;
    }

    private PostEvent setEventDetail(String eventDetail) {
        mEventDetail = eventDetail;
        return this;
    }

    private PostEvent setEventTarget(String eventTarget) {
        mEventTarget = eventTarget;
        return this;
    }

    private void send(Context context) {
        JSONObject postJsonObject = new JSONObject();
        try {
            postJsonObject.put(POST_FIELD_USER_ID, mUserId);
            postJsonObject.put(POST_FIELD_EVENT, mEvent);
            postJsonObject.putOpt(POST_FIELD_EVENT_CONTENT, mEventContent);
            postJsonObject.putOpt(POST_FIELD_EVENT_DETAIL, mEventDetail);
            postJsonObject.putOpt(POST_FIELD_EVENT_TYPE, mEventType);
            postJsonObject.putOpt(POST_FIELD_EVENT_VALUE, mEventValue);
            postJsonObject.putOpt(POST_FIELD_EVENT_TARGET, mEventTarget);

            MSLog.d(postJsonObject.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    EVENT_POST_URL,
                    postJsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            MSLog.d("Event Post Request success: " + response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            MSLog.e("Event Post Request error: " + error.getMessage(), error);
                            if(error.networkResponse != null) {
                                MSLog.e("Event Post Request error: " + new String(error.networkResponse.data), error);
                            }
                        }
                    });
            jsonObjectRequest.setShouldCache(false);
            Networking.getRequestQueue(context).add(jsonObjectRequest);
        } catch (JSONException e) {
            MSLog.e("JSONException in PostEvent send method.");
        }
    }

    public static void sendStockVote(Context context, String code, String type, int value) {
        new PostEvent("Terry", Event.VOTING)
                .setEventContent(code)
                .setEventValueInteger(value)
                .setEventType(type)
                .setEventTarget(STOCK_CONST)
                .send(context);
    }

    public static void sendNewsVote(Context context, String newsId, String type, int value) {
        new PostEvent("Terry", Event.VOTING)
                .setEventContent(newsId)
                .setEventValueInteger(value)
                .setEventType(type)
                .setEventTarget(NEWS_CONST)
                .send(context);
    }

    public static void sendStockComment(Context context, String code, String html) {
        new PostEvent("Terry", Event.COMMENT)
                .setEventContent(code)
                .setEventValueString(html)
                .setEventType("normal")
                .setEventTarget(STOCK_CONST)
                .send(context);
    }

    public static void sendNewsComment(Context context, String newsId, String html) {
        new PostEvent("Terry", Event.COMMENT)
                .setEventContent(newsId)
                .setEventValueString(html)
                .setEventType("normal")
                .setEventTarget(NEWS_CONST)
                .send(context);
    }
}
