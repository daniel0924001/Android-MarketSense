package com.idroi.marketsense.data;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.datasource.Networking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel.hsieh on 2018/5/7.
 */

public class PostEvent {

    public interface PostEventListener {
        void onResponse(boolean isSuccessful, Object data);
    }

    public enum PostEventType {
        REGISTER("register", POST_TYPE_REGISTER),
        LOGIN("login", POST_TYPE_LOGIN),
        VOTING("voting", POST_TYPE_EVENT),
        COMMENT("comment", POST_TYPE_EVENT),
        REPLY("reply", POST_TYPE_EVENT),
        LIKE("like", POST_TYPE_EVENT),
        FAVORITE_STOCK_ADD("favorite_stock_add", POST_TYPE_ADD_FAVORITE),
        FAVORITE_STOCK_DELETE("favorite_stock_delete", POST_TYPE_DELETE_FAVORITE);

        private String mEvent;
        private int mPostType;

        PostEventType(String event, int type) {
            mEvent = event;
            mPostType = type;
        }

        public String getEventName() {
            return mEvent;
        }

        public int getPostType() {
            return mPostType;
        }
    }

    public enum EventVars {
        VOTE_RAISE("raise"),
        VOTE_FALL("fall");

        private String mEventVar;

        EventVars(String eventVar) {
            mEventVar = eventVar;
        }

        public String getEventVar() {
            return mEventVar;
        }
    }

    private static final String EVENT_POST_URL = "http://apiv2.infohubapp.com/v1/stock/user/event";
    private static final String LOGIN_POST_URL = "http://apiv2.infohubapp.com/v1/stock/user/login";
    private static final String REGISTER_POST_URL = "http://apiv2.infohubapp.com/v1/stock/user/register";
    private static final String FAVORITE_STOCKS_POST_URL = "http://apiv2.infohubapp.com/v1/stock/user/stock_code";
    private static final String FAVORITE_STOCKS_DELETE_URL = "http://apiv2.infohubapp.com/v1/stock/user/stock_code/%s";


    private static final String POST_FIELD_USER_ID = "user_id";
    private static final String POST_FIELD_EVENT = "event";
    private static final String POST_FIELD_EVENT_CONTENT = "event_content";
    private static final String POST_FIELD_EVENT_DETAIL = "event_detail";
    private static final String POST_FIELD_EVENT_TYPE = "event_type";
    private static final String POST_FIELD_EVENT_VALUE = "event_value";
    private static final String POST_FIELD_EVENT_TARGET = "event_target";

    private static final String POST_FIELD_USER_NAME = "user_name";
    private static final String POST_FIELD_USER_TYPE = "user_type";
    private static final String POST_FIELD_PASSWORD = "password";
    private static final String POST_FIELD_EMAIL = "email";
    private static final String POST_FIELD_USER_AVATAR_LINK = "user_avatar_link";

    private static final String POST_FIELD_STOCK_CODE = "stock_code";

    private static final String RESPONSE_STATUS = "status";
    private static final String RESPONSE_RESULT = "result";
    private static final String RESPONSE_EVENT_ID = "event_id";
    private static final String RESPONSE_USER_TOKEN = "user_token";
    private static final String RESPONSE_DUPLICATE_REGISTER = "You have registered before!";

    private static final int POST_TYPE_EVENT = 1;
    private static final int POST_TYPE_REGISTER = 2;
    private static final int POST_TYPE_LOGIN = 3;
    private static final int POST_TYPE_ADD_FAVORITE = 4;
    private static final int POST_TYPE_DELETE_FAVORITE = 5;

    private static final String NEWS_CONST = "news";
    private static final String STOCK_CONST = "stock";
    private static final String EVENT_CONST = "event";

    private int mPostType;
    private int mMethod;

    // POST_TYPE_EVENT
    private String mUserId;
    private String mEvent;
    private String mEventContent;
    private String mEventType;
    private Object mEventValue; // String or Integer
    private JSONArray mEventDetail;
    private String mEventTarget;

    // POST_TYPE_REGISTER
    // POST_TYPE_LOGIN
    private String mUserName;
    private String mUserType;
    private String mUserPassword;
    private String mUserEmail;
    private String mUserAvatarLink;

    // POST_TYPE_ADD_FAVORITE
    // POST_TYPE_DELETE_FAVORITE
    private String mStockCode;
    private String mUserToken;

    private PostEventListener mListener;

    private PostEvent(String userId, PostEventType postEventType) {
        mMethod = Request.Method.POST;
        mUserId = userId;
        mEvent = postEventType.getEventName();
        mPostType = postEventType.getPostType();
    }

    private PostEvent(int method, String userId, PostEventType postEventType) {
        mMethod = method;
        mUserId = userId;
        mEvent = postEventType.getEventName();
        mPostType = postEventType.getPostType();
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

    private PostEvent setEventDetail(JSONArray eventDetail) {
        mEventDetail = eventDetail;
        return this;
    }

    private PostEvent setEventTarget(String eventTarget) {
        mEventTarget = eventTarget;
        return this;
    }

    private PostEvent setUserName(String name) {
        mUserName = name;
        return this;
    }

    private PostEvent setUserType(String type) {
        mUserType = type;
        return this;
    }

    private PostEvent setUserPassword(String password) {
        mUserPassword = password;
        return this;
    }

    private PostEvent setUserEmail(String email) {
        mUserEmail = email;
        return this;
    }

    private PostEvent setAvatarLink(String avatarLink) {
        mUserAvatarLink = avatarLink;
        return this;
    }

    private PostEvent setStockCode(String code) {
        mStockCode = code;
        return this;
    }

    private PostEvent setUserToken(String token) {
        mUserToken = token;
        return this;
    }

    private PostEvent setPostEventListener(PostEventListener listener) {
        mListener = listener;
        return this;
    }

    public Event convertToEvent() {
        Event event = new Event();
        event.setEvent(mEvent)
            .setEventContent(mEventContent)
            .setEventType(mEventType)
            .setEventDetail(mEventDetail)
            .setEventTarget(mEventTarget)
            .setEventCreatedTs(String.valueOf((int)(System.currentTimeMillis() / 1000)));
        return event;
    }

    private PostEvent send(Context context) {
        JSONObject postJsonObject = new JSONObject();
        String postUrl = null;
        try {
            switch (mPostType) {
                case POST_TYPE_EVENT:
                    postJsonObject.put(POST_FIELD_USER_ID, mUserId);
                    postJsonObject.put(POST_FIELD_EVENT, mEvent);
                    postJsonObject.putOpt(POST_FIELD_EVENT_CONTENT, mEventContent);
                    postJsonObject.put(POST_FIELD_EVENT_DETAIL, mEventDetail);
                    postJsonObject.putOpt(POST_FIELD_EVENT_TYPE, mEventType);
                    postJsonObject.putOpt(POST_FIELD_EVENT_VALUE, mEventValue);
                    postJsonObject.putOpt(POST_FIELD_EVENT_TARGET, mEventTarget);
                    postUrl = EVENT_POST_URL;
                    break;
                case POST_TYPE_LOGIN:
                    postJsonObject.put(POST_FIELD_USER_ID, mUserId);
                    postJsonObject.put(POST_FIELD_PASSWORD, mUserPassword);
                    postJsonObject.putOpt(POST_FIELD_EMAIL, mUserEmail);
                    postUrl = LOGIN_POST_URL;
                    break;
                case POST_TYPE_REGISTER:
                    postJsonObject.put(POST_FIELD_USER_ID, mUserId);
                    postJsonObject.put(POST_FIELD_USER_TYPE, mUserType);
                    postJsonObject.put(POST_FIELD_PASSWORD, mUserPassword);
                    postJsonObject.putOpt(POST_FIELD_USER_NAME, mUserName);
                    postJsonObject.putOpt(POST_FIELD_EMAIL, mUserEmail);
                    postJsonObject.putOpt(POST_FIELD_USER_AVATAR_LINK, mUserAvatarLink);
                    postUrl = REGISTER_POST_URL;
                    break;
                case POST_TYPE_ADD_FAVORITE:
                    postJsonObject.put(POST_FIELD_USER_ID, mUserId);
                    postJsonObject.put(POST_FIELD_STOCK_CODE, mStockCode);
                    postUrl = FAVORITE_STOCKS_POST_URL;
                    break;
                case POST_TYPE_DELETE_FAVORITE:
                    postUrl = String.format(FAVORITE_STOCKS_DELETE_URL, mStockCode);
                    break;
                default:
                    MSLog.e("Unknown post type");
                    return this;
            }

            MSLog.d("Method: " + mMethod + " event type: " + mPostType + ", data: " + postJsonObject.toString() + ", url: " + postUrl);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    mMethod,
                    postUrl,
                    postJsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            MSLog.d("Event Post Request success: " + response);
                            switch (mPostType) {
                                case POST_TYPE_REGISTER:
                                    processRegister(response);
                                    break;
                                case POST_TYPE_LOGIN:
                                    processLogin(response);
                                    break;
                                case POST_TYPE_EVENT:
                                    processEventResponse(response);
                                    break;
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            MSLog.e("Event Post Request error: " + error.getMessage(), error);
                            if(error.networkResponse != null) {
                                MSLog.e("Event Post Request error: " + new String(error.networkResponse.data), error);
                            }

                            switch (mPostType) {
                                case POST_TYPE_LOGIN:
                                    if(mListener != null) {
                                        mListener.onResponse(false, null);
                                    }
                                    break;
                                case POST_TYPE_EVENT:
                                    if(mListener != null) {
                                        mListener.onResponse(false, null);
                                    }
                                    break;

                            }
                        }
                    }){


                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json");
                    if(mUserToken != null && !mUserToken.isEmpty()) {
                        headers.put("User-Token", mUserToken);
                    }
                    return headers;
                }
            };
            jsonObjectRequest.setShouldCache(false);
            Networking.getRequestQueue(context).add(jsonObjectRequest);
        } catch (JSONException e) {
            MSLog.e("JSONException in PostEvent send method.");
        }
        return this;
    }

    private void processRegister(JSONObject jsonObject) {
        if(!jsonObject.optBoolean(RESPONSE_STATUS, false) &&
                jsonObject.optString(RESPONSE_RESULT) != null &&
                jsonObject.optString(RESPONSE_RESULT).equals(RESPONSE_DUPLICATE_REGISTER)) {
            // {"status":false,"result":"You have registered before!"}
            sendLogin(null, mUserId, mUserPassword, mUserEmail, mListener);
        }
    }

    private void processLogin(JSONObject jsonObject) {
        boolean isSuccessful = jsonObject.optBoolean(RESPONSE_STATUS, false);
        if(isSuccessful) {
            JSONObject result = jsonObject.optJSONObject(RESPONSE_RESULT);
            if(result != null) {
                String token = result.optString(RESPONSE_USER_TOKEN);
                MSLog.d("Login success with token: " + token);
                ClientData clientData = ClientData.getInstance();
                clientData.setUserToken(token);
                clientData.loadPreference();
            }
        }

        if(mListener != null) {
            mListener.onResponse(isSuccessful, null);
        }
    }

    private void processEventResponse(JSONObject jsonObject) {
        if(mListener != null) {
            boolean isSuccessful = jsonObject.optBoolean(RESPONSE_STATUS, false);
            JSONObject resultJsonObject = jsonObject.optJSONObject(RESPONSE_RESULT);
            String id = null;
            if(resultJsonObject != null) {
                id = resultJsonObject.optString(RESPONSE_EVENT_ID);
            }
            mListener.onResponse(isSuccessful, id);
        }
    }

    public static void sendStockVote(Context context, String code, EventVars type, int value) {
        ClientData clientData = ClientData.getInstance(context);
        UserProfile userProfile = clientData.getUserProfile();
        PostEvent postEvent = new PostEvent(clientData.getUserProfile().getUserId(), PostEventType.VOTING)
                .setEventContent(code)
                .setEventValueInteger(value)
                .setEventType(type.getEventVar())
                .setEventTarget(STOCK_CONST)
                .setUserToken(clientData.getUserToken())
                .send(context);
        Event event = postEvent.convertToEvent();
        userProfile.addEvent(event);
    }

    public static void sendNewsVote(Context context, String newsId, EventVars type, int value) {
        ClientData clientData = ClientData.getInstance(context);
        UserProfile userProfile = clientData.getUserProfile();
        PostEvent postEvent = new PostEvent(userProfile.getUserId(), PostEventType.VOTING)
                .setEventContent(newsId)
                .setEventValueInteger(value)
                .setEventType(type.getEventVar())
                .setEventTarget(NEWS_CONST)
                .setUserToken(clientData.getUserToken())
                .send(context);
        Event event = postEvent.convertToEvent();
        userProfile.addEvent(event);
    }

    public static void sendStockComment(Context context, String code, String html, ArrayList<String> tags, PostEventListener listener) {
        ClientData clientData = ClientData.getInstance(context);
        UserProfile userProfile = clientData.getUserProfile();
        PostEvent postEvent = new PostEvent(clientData.getUserProfile().getUserId(), PostEventType.COMMENT)
                .setEventContent(code)
                .setEventValueString(html)
                .setEventType("normal")
                .setEventTarget(STOCK_CONST)
                .setEventDetail(new JSONArray(tags))
                .setUserToken(clientData.getUserToken())
                .setPostEventListener(listener)
                .send(context);
        Event event = postEvent.convertToEvent();
        userProfile.addEvent(event);
    }

    public static void sendNewsComment(Context context, String newsId, String html, ArrayList<String> tags, PostEventListener listener) {
        ClientData clientData = ClientData.getInstance(context);
        UserProfile userProfile = clientData.getUserProfile();
        PostEvent postEvent = new PostEvent(clientData.getUserProfile().getUserId(), PostEventType.COMMENT)
                .setEventContent(newsId)
                .setEventValueString(html)
                .setEventType("normal")
                .setEventTarget(NEWS_CONST)
                .setEventDetail(new JSONArray(tags))
                .setUserToken(clientData.getUserToken())
                .setPostEventListener(listener)
                .send(context);
        Event event = postEvent.convertToEvent();
        userProfile.addEvent(event);
    }

    public static void sendReplyComment(Context context, String eventId, String html, ArrayList<String> tags) {
        ClientData clientData = ClientData.getInstance(context);
        UserProfile userProfile = clientData.getUserProfile();
        PostEvent postEvent = new PostEvent(clientData.getUserProfile().getUserId(), PostEventType.REPLY)
                .setEventContent(eventId)
                .setEventValueString(html)
                .setEventType("normal")
                .setEventTarget(EVENT_CONST)
                .setEventDetail(new JSONArray(tags))
                .setUserToken(clientData.getUserToken())
                .send(context);
        Event event = postEvent.convertToEvent();
        userProfile.addEvent(event);
    }

    public static void sendLike(Context context, String eventId) {
        ClientData clientData = ClientData.getInstance(context);
        UserProfile userProfile = clientData.getUserProfile();
        PostEvent postEvent = new PostEvent(clientData.getUserProfile().getUserId(), PostEventType.LIKE)
                .setEventContent(eventId)
                .setEventValueInteger(1)
                .setEventType("normal")
                .setEventTarget(EVENT_CONST)
                .setUserToken(clientData.getUserToken())
                .send(context);
        Event event = postEvent.convertToEvent();
        userProfile.addEvent(event);
    }

    public static void sendRegister(Context context,
                                    String userId, String userName, String userType,
                                    String userPassword, String userEmail, String avatarLink,
                                    PostEventListener listener) {
        new PostEvent(userId, PostEventType.REGISTER)
                .setUserName(userName)
                .setUserType(userType)
                .setUserPassword(userPassword)
                .setUserEmail(userEmail)
                .setAvatarLink(avatarLink)
                .setPostEventListener(listener)
                .send(context);
    }

    public static void sendLogin(Context context, String userId,
                                 String userPassword, String userEmail, PostEventListener listener) {
        new PostEvent(userId, PostEventType.LOGIN)
                .setUserPassword(userPassword)
                .setUserEmail(userEmail)
                .setPostEventListener(listener)
                .send(context);
    }

    public static void sendFavoriteStocksAdd(Context context, String code) {
        ClientData clientData = ClientData.getInstance(context);
        new PostEvent(clientData.getUserProfile().getUserId(), PostEventType.FAVORITE_STOCK_ADD)
                .setStockCode(code)
                .send(context);
    }

    public static void sendFavoriteStocksDelete(Context context, String code) {
        ClientData clientData = ClientData.getInstance(context);
        new PostEvent(Request.Method.DELETE, clientData.getUserProfile().getUserId(), PostEventType.FAVORITE_STOCK_DELETE)
                .setStockCode(code)
                .setUserToken(clientData.getUserToken())
                .send(context);
    }
}
