package com.idroi.marketsense.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessaging;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.SharedPreferencesCompat;
import com.idroi.marketsense.notification.NotificationHelper;
import com.idroi.marketsense.util.DateUtils;
import com.idroi.marketsense.util.NewsReadRecordHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_LAST_STAR_TIMESTAMP;
import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_USER_SETTING;
import static com.idroi.marketsense.data.Event.EVENT_TARGET_NEWS;
import static com.idroi.marketsense.data.Event.EVENT_TARGET_STOCK;
import static com.idroi.marketsense.data.Event.EVENT_VOTING;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class UserProfile implements Serializable {

    public static final int NOTIFY_ID_FAVORITE_LIST = 1;
    public static final int NOTIFY_ID_STOCK_COMMENT_CLICK = 2;
    public static final int NOTIFY_ID_NEWS_COMMENT_CLICK = 3;
    public static final int NOTIFY_ID_REPLY_COMMENT_CLICK = 7;
    public static final int NOTIFY_ID_DISCUSSION_COMMENT_CLICK = 8;
    public static final int NOTIFY_ID_MAIN_ACTIVITY_FUNCTION_CLICK = 9;
    public static final int NOTIFY_USER_HAS_LOGIN = 4;
    public static final int NOTIFY_ID_EVENT_LIST = 5;
    public static final int NOTIFY_ID_NEWS_READ_RECORD_LIST = 14;
    public static final int NOTIFY_USER_LOGIN_FAILED = 6;

    public static final int NOTIFY_ID_FUNCTION_SEARCH_COMMENT = 9;
    public static final int NOTIFY_ID_FUNCTION_INSERT_COMMENT = 10;

    public static final int NOTIFY_ID_PRICE_CHANGED = 11;
    public static final int NOTIFY_ID_NEED_TO_APK_UPDATED = 12;
    public static final int NOTIFY_ID_RIGHT_PART_CHANGE = 13;

    public interface GlobalBroadcastListener {
        void onGlobalBroadcast(int notifyId, Object payload);
    }

    private static final String USER_PROFILE_SHARE_PREFERENCE = "user_profile";
    private static final String FAVORITE_STOCKS_AND_EVENT_SHARE_PREFERENCE = "favorite_stocks_and_events_%s";

    private static final String USER_ID = "user_id";
    private static final String USER_TYPE = "user_type";
    private static final String USER_EMAIL = "email";
    private static final String USER_NAME = "user_name";
    private static final String USER_AVATAR_LINK = "user_avatar_link";

    private static final String SHARE_PREF_ID_KEY = "user_profile_id";
    private static final String SHARE_PREF_USER_TYPE = "user_profile_user_type";
    private static final String SHARE_PREF_NAME_KEY = "user_profile_name";
    private static final String SHARE_PREF_EMAIL_KEY = "user_profile_email";
    private static final String SHARE_PREF_AVATAR_URL_KEY = "user_profile_avatar_link";

    private static final String SHARE_PREF_FAVORITE_STOCKS = "favorite_stocks";
    private static final String SHARE_PREF_EVENTS = "events";
    private boolean mIsInitFavoriteStocksAndEvents = false;

    public static final String FB_USER_ID_KEY = "id";
    public static final String FB_USER_NAME_KEY = "name";
    public static final String FB_USER_EMAIL_KEY = "email";

    private String mUserId;
    private String mUserType;
    private String mUserEmail;
    private String mUserName;
    private String mUserAvatarLink;

    private String mUserAllEventsString;

    private boolean mLoginOnGoing, mHasLogin;

    private ArrayList<GlobalBroadcastListener> mGlobalBroadcastListeners;

    private static final int RETRY_TIME_CONST = 2;
    private int mCurrentRetries = 0;

    @Nullable private ArrayList<String> mFavoriteStocks;
    @Nullable private transient ArrayList<Event> mEventsArrayList;

    private ArrayList<NewsReadRecord> mNewsReadRecordsArrayList;
    private HashMap<String, NewsReadRecord> mNewsReadRecordsMap;

    private UserProfile() {
        this(null, false);
    }

    public UserProfile(Context context, boolean initUserDataFromCache) {
        mGlobalBroadcastListeners = new ArrayList<>();
        mLoginOnGoing = false;
        mHasLogin = false;
        if(initUserDataFromCache) {
            mFavoriteStocks = new ArrayList<String>(){
                @Override
                public String toString() {
                    return Arrays.toString(this.toArray());
                }
            };
            mEventsArrayList = new ArrayList<>();

            if(FBHelper.checkFBLogin()) {
                tryToLoginAndInitUserData(context);
            }
        }
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public void setUserType(String userType) {
        mUserType = userType;
    }

    public void setUserEmail(String userEmail) {
        mUserEmail = userEmail;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public void setUserAvatarLink(String avatarLink) {
        mUserAvatarLink = avatarLink;
    }

    public void setUserAllEventsString(String eventsString) {
        mUserAllEventsString = eventsString;
    }

    public void addGlobalBroadcastListener(GlobalBroadcastListener listener) {
        mGlobalBroadcastListeners.add(listener);
    }

    public void deleteGlobalBroadcastListener(GlobalBroadcastListener listener) {
        mGlobalBroadcastListeners.remove(listener);
    }

    public void globalBroadcast(int notifyId) {
        globalBroadcast(notifyId, null);
    }

    public void globalBroadcast(int notifyId, Object payload) {
        MSLog.i("notify for id: " + notifyId);
        for(int i = 0; i < mGlobalBroadcastListeners.size(); i++) {
            mGlobalBroadcastListeners.get(i).onGlobalBroadcast(notifyId, payload);
        }
    }

    public String getUserId() {
        return mUserId;
    }

    public String getUserType() {
        return mUserType;
    }

    public String getUserEmail() {
        return mUserEmail;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getUserAvatarLink() {
        return mUserAvatarLink;
    }

    public ArrayList<NewsReadRecord> getNewsReadRecords() {
        return mNewsReadRecordsArrayList;
    }

    private boolean isRetry() {
        return mCurrentRetries <= RETRY_TIME_CONST;
    }

    private void increaseRetryTime() {
        if(mCurrentRetries <= RETRY_TIME_CONST) {
            mCurrentRetries++;
        }
    }

    private void resetRetryTime() {
        mCurrentRetries = 0;
    }

    /* favorite stock list */
    public void clearFavoriteStock() {
        if(mFavoriteStocks != null) {
            mFavoriteStocks.clear();
        }
    }

    public boolean addFavoriteStock(String code) {
        if(!isFavoriteStock(code)) {
            // subscribe FCM topic
            MSLog.d("subscribeToTopic: " + NotificationHelper.getTopicForStockCode(code));
            FirebaseMessaging.getInstance().subscribeToTopic(NotificationHelper.getTopicForStockCode(code));
            if (mFavoriteStocks != null) {
                mFavoriteStocks.add(code);
            }
            return true;
        } else {
            MSLog.w("already add favorite stock of this code in UserProfile: " + code);
            return false;
        }
    }

    public void deleteFavoriteStock(String code) {
        // un-subscribe FCM topic
        MSLog.d("unsubscribeFromTopic: " + NotificationHelper.getTopicForStockCode(code));
        FirebaseMessaging.getInstance().unsubscribeFromTopic(NotificationHelper.getTopicForStockCode(code));
        if(mFavoriteStocks != null) {
            mFavoriteStocks.remove(code);
        }
    }

    public boolean canShowStarDialog(Context context) {
        Date date = new Date(System.currentTimeMillis());

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SHARED_PREFERENCE_USER_SETTING, Context.MODE_PRIVATE);
        boolean show;
        if(!sharedPreferences.contains(SHARED_PREFERENCE_LAST_STAR_TIMESTAMP)) {
            show = true;
        } else {
            Date lastDate = new Date(sharedPreferences.getLong(SHARED_PREFERENCE_LAST_STAR_TIMESTAMP, 0));
            show = !(DateUtils.isWithinDaysFuture(lastDate, 7) || DateUtils.isToday(lastDate));
        }

        if(mFavoriteStocks != null && mFavoriteStocks.size() >= 2 && show) {
            SharedPreferences.Editor editor =
                    context.getSharedPreferences(SHARED_PREFERENCE_USER_SETTING, Context.MODE_PRIVATE).edit();
            editor.putLong(SHARED_PREFERENCE_LAST_STAR_TIMESTAMP, date.getTime());
            MSLog.d("save SHARED_PREFERENCE_LAST_STAR_TIMESTAMP: " + date.getTime());
            SharedPreferencesCompat.apply(editor);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    public String getFavoriteStocksString() {
        if(mFavoriteStocks != null) {
            return mFavoriteStocks.toString();
        } else {
            return null;
        }
    }

    public ArrayList<String> getFavoriteStocks() {
        return mFavoriteStocks;
    }

    public boolean isFavoriteStock(String code) {
        return mFavoriteStocks != null && mFavoriteStocks.contains(code);
    }

    public boolean isEmptyFavoriteStock() {
        return mFavoriteStocks == null || mFavoriteStocks.size() == 0;
    }
    /* end of favorite stock list */

    /* event */
    public void clearEvents() {
        if(mEventsArrayList != null) {
            mEventsArrayList.clear();
        }
    }

    public void addEvent(Event event) {
        if(mEventsArrayList != null) {
            mEventsArrayList.add(event);
        }
    }

    public Event getRecentVoteForStockEvent(String code) {
        long max = -1;
        Event result = null;
        if(mEventsArrayList != null) {
            for (int i = 0; i < mEventsArrayList.size(); i++) {
                Event event = mEventsArrayList.get(i);
                if(event.getEvent().equals(EVENT_VOTING) &&
                        event.getEventTarget().equals(EVENT_TARGET_STOCK) &&
                        event.getEventContent().equals(code)) {
                    long temp = Long.valueOf(event.getEventCreatedTs());
                    if(temp > max) {
                        max = temp;
                        result = event;
                    }
                }
            }
            return result;
        }
        return null;
    }

    public Event getRecentVoteForNewsEvent(String newsId) {
        long max = -1;
        Event result = null;
        if(mEventsArrayList != null) {
            for (int i = 0; i < mEventsArrayList.size(); i++) {
                Event event = mEventsArrayList.get(i);
                if(event.getEvent().equals(EVENT_VOTING) &&
                        event.getEventTarget().equals(EVENT_TARGET_NEWS) &&
                        event.getEventContent().equals(newsId)) {
                    long temp = Long.valueOf(event.getEventCreatedTs());
                    if(temp > max) {
                        max = temp;
                        result = event;
                    }
                }
            }
            return result;
        }
        return null;
    }

    public boolean canVoteAgain(String code) {
        Event event = getRecentVoteForStockEvent(code);
        if(event == null) {
            return true;
        }

        String timestamp = event.getEventCreatedTs();
        if(timestamp != null) {
            // 8 am
            long shift = 8 * 60 * 60 * 1000;

            Date thatDate = new Date(Long.parseLong(timestamp) * 1000 - shift);
            Timestamp todayTs = new Timestamp(System.currentTimeMillis() - shift);
            Date today = new Date(todayTs.getTime());
            return DateUtils.isAfterDay(today, thatDate);
        } else {
            return true;
        }
    }

    public boolean hasVoteForStock(String code) {
        if(mEventsArrayList != null) {
            for (int i = 0; i < mEventsArrayList.size(); i++) {
                Event event = mEventsArrayList.get(i);
                if(event.getEvent().equals(EVENT_VOTING) &&
                        event.getEventTarget().equals(EVENT_TARGET_STOCK) &&
                        event.getEventContent().equals(code)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasVoteForNews(String newsId) {
        if(mEventsArrayList != null) {
            for (int i = 0; i < mEventsArrayList.size(); i++) {
                Event event = mEventsArrayList.get(i);
                if(event.getEvent().equals(EVENT_VOTING) &&
                        event.getEventTarget().equals(EVENT_TARGET_NEWS) &&
                        event.getEventContent().equals(newsId)) {
                    return true;
                }
            }
        }
        return false;
    }
    /* end of event */

    public void clearUserProfile() {
        MSLog.d("clear userId...");
        mUserId = null;
        mUserName = null;
        mUserEmail = null;
        mUserType = null;
        mUserAvatarLink = null;
        MSLog.d("clear events...");
        clearEvents();
        MSLog.d("clear favorite stocks...");
        clearFavoriteStock();
        clearNewsReadRecords();
    }

    public void clearNewsReadRecords() {
        mNewsReadRecordsMap.clear();
        mNewsReadRecordsArrayList.clear();
        globalBroadcast(NOTIFY_ID_NEWS_READ_RECORD_LIST);
    }

    public void saveFavoriteStocksAndEvents(Context context) {
        String s = String.format(FAVORITE_STOCKS_AND_EVENT_SHARE_PREFERENCE, mUserId);
        SharedPreferences.Editor editor =
                context.getSharedPreferences(s, Context.MODE_PRIVATE).edit();
        if(mFavoriteStocks != null) {
            editor.putStringSet(SHARE_PREF_FAVORITE_STOCKS, new HashSet<String>(mFavoriteStocks));
        }
        if(mUserAllEventsString != null) {
            editor.putString(SHARE_PREF_EVENTS, mUserAllEventsString);
        }
        editor.apply();
        MSLog.i("[user logout] save favorite stocks and events to cache: " + s);
        setIsInitFavoriteStocksAndEvents(false);
    }

    public void setIsInitFavoriteStocksAndEvents(boolean isInit) {
        mIsInitFavoriteStocksAndEvents = isInit;
    }

    public void addNewsReadRecord(String newsId) {
        MSLog.d("addNewsReadRecord newsId: " + newsId);
        NewsReadRecord newsReadRecord = new NewsReadRecord(newsId);
        mNewsReadRecordsMap.put(newsId, newsReadRecord);
        mNewsReadRecordsArrayList.add(newsReadRecord);
    }

    public boolean hasReadThisNews(String newsId) {
        return mNewsReadRecordsMap != null && mNewsReadRecordsMap.containsKey(newsId);
    }

    public void getFavoriteStocksAndEvents(Context context, String userId) {
        if(!mIsInitFavoriteStocksAndEvents) {
            mIsInitFavoriteStocksAndEvents = true;

            String s = String.format(FAVORITE_STOCKS_AND_EVENT_SHARE_PREFERENCE, userId);
            final SharedPreferences sharedPreferences =
                    context.getSharedPreferences(s, Context.MODE_PRIVATE);

            // favorite stocks part
            Set<String> set = sharedPreferences.getStringSet(SHARE_PREF_FAVORITE_STOCKS, null);
            if (set != null) {
                mFavoriteStocks = new ArrayList<>(set);
                MSLog.i("[user login] notify we get favorite stocks from cache: " + mFavoriteStocks);
                globalBroadcast(NOTIFY_ID_FAVORITE_LIST);
            }

            // events part
            mUserAllEventsString = sharedPreferences.getString(SHARE_PREF_EVENTS, null);
            if (mUserAllEventsString != null) {
                try {
                    JSONArray eventsJsonArray = new JSONArray(mUserAllEventsString);
                    for (int i = 0; i < eventsJsonArray.length(); i++) {
                        if (eventsJsonArray.optJSONObject(i) != null) {
                            Event event = Event.JsonObjectToEvent(eventsJsonArray.optJSONObject(i));
                            addEvent(event);
                        }
                    }
                    MSLog.i("[user login] notify we get events from cache");
                    globalBroadcast(NOTIFY_ID_EVENT_LIST);
                } catch (JSONException e) {
                    MSLog.e("JSONException in getFavoriteStocksAndEvents: " + e.toString());
                }
            }

            // news read records
            mNewsReadRecordsMap = NewsReadRecordHelper.readFromInternalStorage(context, userId);
            mNewsReadRecordsArrayList = new ArrayList<>(mNewsReadRecordsMap.values());
            globalBroadcast(NOTIFY_ID_NEWS_READ_RECORD_LIST);
        }
    }

    public void updateUserData(Context context) {
        MSLog.d(String.format("update user data to share preference: %s %s %s %s %s",
                mUserId, mUserType, mUserName, mUserEmail, mUserAvatarLink));
        SharedPreferences.Editor editor =
                context.getSharedPreferences(USER_PROFILE_SHARE_PREFERENCE, Context.MODE_PRIVATE).edit();
        editor.putString(SHARE_PREF_ID_KEY, mUserId);
        editor.putString(SHARE_PREF_NAME_KEY, mUserName);
        editor.putString(SHARE_PREF_EMAIL_KEY, mUserEmail);
        editor.putString(SHARE_PREF_USER_TYPE, mUserType);
        editor.putString(SHARE_PREF_AVATAR_URL_KEY, mUserAvatarLink);
        SharedPreferencesCompat.apply(editor);
    }

    public void tryToLoginAndInitUserData(final Context context) {

        if(mHasLogin) {
            MSLog.i("[user login]: start to login but has login");
            globalBroadcast(NOTIFY_USER_HAS_LOGIN);
            return;
        }

        if(!mLoginOnGoing) {
            mLoginOnGoing = true;
            MSLog.i("[user login]: start to login");
            final SharedPreferences sharedPreferences =
                    context.getSharedPreferences(USER_PROFILE_SHARE_PREFERENCE, Context.MODE_PRIVATE);
            mUserId = sharedPreferences.getString(SHARE_PREF_ID_KEY, null);
            mUserType = sharedPreferences.getString(SHARE_PREF_USER_TYPE, null);
            getFavoriteStocksAndEvents(context, mUserId);
            String password = UserProfile.generatePassword(mUserId, mUserType);
            PostEvent.sendLogin(context, mUserId, password, mUserEmail, new PostEvent.PostEventListener() {
                @Override
                public void onResponse(boolean isSuccessful, Object data) {
                    MSLog.i("[user login]: end login and the result is: " + isSuccessful);
                    mLoginOnGoing = false;
                    mHasLogin = isSuccessful;

                    if (mHasLogin) {
                        resetRetryTime();
                        mUserName = sharedPreferences.getString(SHARE_PREF_NAME_KEY,
                                context.getResources().getString(R.string.default_user_name));
                        mUserEmail = sharedPreferences.getString(SHARE_PREF_EMAIL_KEY, null);
                        mUserAvatarLink = sharedPreferences.getString(SHARE_PREF_AVATAR_URL_KEY,
                                context.getResources().getString(R.string.default_user_avatar_link));
                        MSLog.d(String.format("init user data from share preference: %s %s %s %s",
                                mUserId, mUserName, mUserEmail, mUserAvatarLink));
                        globalBroadcast(NOTIFY_USER_HAS_LOGIN);
                    } else {
                        increaseRetryTime();
                        if(isRetry()) {
                            tryToLoginAndInitUserData(context);
                        } else {
                            globalBroadcast(NOTIFY_USER_LOGIN_FAILED);
                        }
                    }
                }
            });
        }
    }

    static UserProfile jsonObjectToUserProfile(JSONObject jsonObject) {
        if(jsonObject != null) {
            UserProfile userProfile = new UserProfile();
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                try {
                    switch (key) {
                        case USER_ID:
                            userProfile.setUserId(jsonObject.optString(USER_ID));
                            break;
                        case USER_TYPE:
                            userProfile.setUserType(jsonObject.optString(USER_TYPE));
                            break;
                        case USER_EMAIL:
                            userProfile.setUserEmail(jsonObject.optString(USER_EMAIL));
                            break;
                        case USER_NAME:
                            userProfile.setUserName(jsonObject.optString(USER_NAME));
                            break;
                        case USER_AVATAR_LINK:
                            userProfile.setUserAvatarLink(jsonObject.optString(USER_AVATAR_LINK));
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    MSLog.e(e.toString());
                }
            }

            return userProfile;
        } else {
            return null;
        }
    }

    public static String generatePassword(String id, String type) {
        String s = id + "|" + type;
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest)
                hexString.append(Integer.toHexString(0xFF & aMessageDigest));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "DEFAULT_PASSWORD";
    }
}
