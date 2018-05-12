package com.idroi.marketsense.data;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.datasource.Networking;

import org.json.JSONException;
import org.json.JSONObject;

import static com.idroi.marketsense.data.PostEvent.Event.FAVORITE_STOCK_ADD;
import static com.idroi.marketsense.data.PostEvent.Event.FAVORITE_STOCK_DELETE;

/**
 * Created by daniel.hsieh on 2018/5/7.
 */

public class PostEvent {

    public enum Event {
        REGISTER("register", POST_TYPE_REGISTER),
        LOGIN("login", POST_TYPE_LOGIN),
        VOTING("voting", POST_TYPE_EVENT),
        COMMENT("comment", POST_TYPE_EVENT),
        FAVORITE_STOCK_ADD("favorite_stock_add", POST_TYPE_ADD_FAVORITE),
        FAVORITE_STOCK_DELETE("favorite_stock_delete", POST_TYPE_DELETE_FAVORITE);

        private String mEvent;
        private int mPostType;

        Event(String event, int type) {
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
    private static final String FAVORITE_STOCKS_DELETE_URL = "http://apiv2.infohubapp.com/v1/stock/user/stock_code/%s/%s";


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
    private static final String RESPONSE_USER_TOKEN = "user_token";
    private static final String RESPONSE_DUPLICATE_REGISTER = "You have registered before!";

    private static final int POST_TYPE_EVENT = 1;
    private static final int POST_TYPE_REGISTER = 2;
    private static final int POST_TYPE_LOGIN = 3;
    private static final int POST_TYPE_ADD_FAVORITE = 4;
    private static final int POST_TYPE_DELETE_FAVORITE = 5;

    private static final String NEWS_CONST = "news";
    private static final String STOCK_CONST = "stock";

    private int mPostType;
    private int mMethod;

    // POST_TYPE_EVENT
    private String mUserId;
    private String mEvent;
    private String mEventContent;
    private String mEventType;
    private Object mEventValue; // String or Integer
    private String mEventDetail;
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

    private PostEvent(String userId, Event event) {
        mMethod = Request.Method.POST;
        mUserId = userId;
        mEvent = event.getEventName();
        mPostType = event.getPostType();
    }

    private PostEvent(int method, String userId, Event event) {
        mMethod = method;
        mUserId = userId;
        mEvent = event.getEventName();
        mPostType = event.getPostType();
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

    private void send(Context context) {
        JSONObject postJsonObject = new JSONObject();
        String postUrl = null;
        try {
            switch (mPostType) {
                case POST_TYPE_EVENT:
                    postJsonObject.put(POST_FIELD_USER_ID, mUserId);
                    postJsonObject.put(POST_FIELD_EVENT, mEvent);
                    postJsonObject.putOpt(POST_FIELD_EVENT_CONTENT, mEventContent);
                    postJsonObject.putOpt(POST_FIELD_EVENT_DETAIL, mEventDetail);
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
                    postUrl = String.format(FAVORITE_STOCKS_DELETE_URL, mStockCode, mUserToken);
                    break;
                default:
                    MSLog.e("Unknown post type");
                    return;
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
                        }
                    });
            jsonObjectRequest.setShouldCache(false);
            Networking.getRequestQueue(context).add(jsonObjectRequest);
        } catch (JSONException e) {
            MSLog.e("JSONException in PostEvent send method.");
        }
    }

    private void processRegister(JSONObject jsonObject) {
        if(!jsonObject.optBoolean(RESPONSE_STATUS, false) &&
                jsonObject.optString(RESPONSE_RESULT) != null &&
                jsonObject.optString(RESPONSE_RESULT).equals(RESPONSE_DUPLICATE_REGISTER)) {
            // {"status":false,"result":"You have registered before!"}
            sendLogin(null, mUserId, mUserPassword, mUserEmail);
        }
    }

    private void processLogin(JSONObject jsonObject) {
        if(jsonObject.optBoolean(RESPONSE_STATUS, false)) {
            JSONObject result = jsonObject.optJSONObject(RESPONSE_RESULT);
            if(result != null) {
                String token = result.optString(RESPONSE_USER_TOKEN);
                MSLog.d("Login success with token: " + token);
                ClientData.getInstance().setUserToken(token);

                ClientData.getInstance().loadPreference();
            }
        }
    }

    public static void sendStockVote(Context context, String code, EventVars type, int value) {
        ClientData clientData = ClientData.getInstance(context);
        new PostEvent(clientData.getUserProfile().getUserId(), Event.VOTING)
                .setEventContent(code)
                .setEventValueInteger(value)
                .setEventType(type.getEventVar())
                .setEventTarget(STOCK_CONST)
                .send(context);
    }

    public static void sendNewsVote(Context context, String newsId, EventVars type, int value) {
        ClientData clientData = ClientData.getInstance(context);
        new PostEvent(clientData.getUserProfile().getUserId(), Event.VOTING)
                .setEventContent(newsId)
                .setEventValueInteger(value)
                .setEventType(type.getEventVar())
                .setEventTarget(NEWS_CONST)
                .send(context);
    }

    public static void sendStockComment(Context context, String code, String html) {
        ClientData clientData = ClientData.getInstance(context);
        new PostEvent(clientData.getUserProfile().getUserId(), Event.COMMENT)
                .setEventContent(code)
                .setEventValueString(html)
                .setEventType("normal")
                .setEventTarget(STOCK_CONST)
                .send(context);
    }

    public static void sendNewsComment(Context context, String newsId, String html) {
        ClientData clientData = ClientData.getInstance(context);
        new PostEvent(clientData.getUserProfile().getUserId(), Event.COMMENT)
                .setEventContent(newsId)
                .setEventValueString(html)
                .setEventType("normal")
                .setEventTarget(NEWS_CONST)
                .send(context);
    }

    public static void sendRegister(Context context,
                                    String userId, String userName, String userType,
                                    String userPassword, String userEmail, String avatarLink) {
        new PostEvent(userId, Event.REGISTER)
                .setUserName(userName)
                .setUserType(userType)
                .setUserPassword(userPassword)
                .setUserEmail(userEmail)
                .setAvatarLink(avatarLink)
                .send(context);
    }

    public static void sendLogin(Context context, String userId,
                                 String userPassword, String userEmail) {
        new PostEvent(userId, Event.LOGIN)
                .setUserPassword(userPassword)
                .setUserEmail(userEmail)
                .send(context);
    }

    public static void sendFavoriteStocksAdd(Context context, String code) {
        ClientData clientData = ClientData.getInstance(context);
        new PostEvent(clientData.getUserProfile().getUserId(), FAVORITE_STOCK_ADD)
                .setStockCode(code)
                .send(context);
    }

    public static void sendFavoriteStocksDelete(Context context, String code) {
        ClientData clientData = ClientData.getInstance(context);
        new PostEvent(Request.Method.DELETE, clientData.getUserProfile().getUserId(), FAVORITE_STOCK_DELETE)
                .setStockCode(code)
                .setUserToken(clientData.getUserToken())
                .send(context);
    }
}
