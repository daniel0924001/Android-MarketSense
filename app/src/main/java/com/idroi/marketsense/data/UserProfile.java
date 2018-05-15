package com.idroi.marketsense.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.SharedPreferencesCompat;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static com.idroi.marketsense.data.Event.EVENT_TARGET_NEWS;
import static com.idroi.marketsense.data.Event.EVENT_TARGET_STOCK;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class UserProfile {

    public static final int NOTIFY_ID_FAVORITE_LIST = 1;

    public interface UserProfileChangeListener {
        void onUserProfileChange(int notifyId);
    }

    private static final String USER_PROFILE_SHARE_PREFERENCE = "user_profile";

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

    public static final String FB_USER_ID_KEY = "id";
    public static final String FB_USER_NAME_KEY = "name";
    public static final String FB_USER_EMAIL_KEY = "email";

    private String mUserId;
    private String mUserType;
    private String mUserEmail;
    private String mUserName;
    private String mUserAvatarLink;

    private ArrayList<UserProfileChangeListener> mUserProfileChangeListeners;

    @Nullable private ArrayList<String> mFavoriteStocks;
    @Nullable private ArrayList<Event> mEventsArrayList;

    private UserProfile() {
        this(null, false);
    }

    public UserProfile(Context context, boolean initUserDataFromCache) {
        mUserProfileChangeListeners = new ArrayList<>();
        if(initUserDataFromCache) {
            mFavoriteStocks = new ArrayList<String>(){
                @Override
                public String toString() {
                    return Arrays.toString(this.toArray());
                }
            };
            mEventsArrayList = new ArrayList<>();
            initUserData(context);
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

    public void addUserProfileChangeListener(UserProfileChangeListener listener) {
        mUserProfileChangeListeners.add(listener);
    }

    public void deleteUserProfileChangeListener(UserProfileChangeListener listener) {
        mUserProfileChangeListeners.remove(listener);
    }

    public void notifyUserProfile(int notifyId) {
        for(int i = 0; i < mUserProfileChangeListeners.size(); i++) {
            mUserProfileChangeListeners.get(i).onUserProfileChange(notifyId);
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

//    public String getUserToken() {
//        return mUserToken;
//    }

    public String getUserAvatarLink() {
        return mUserAvatarLink;
    }

    /* favorite stock list */
    public void clearFavoriteStock() {
        if(mFavoriteStocks != null) {
            mFavoriteStocks.clear();
        }
    }

    public void addFavoriteStock(String code) {
        if(mFavoriteStocks != null) {
            mFavoriteStocks.add(code);
        }
    }

    public void deleteFavoriteStock(String code) {
        if(mFavoriteStocks != null) {
            mFavoriteStocks.remove(code);
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
    /* end of favorite stock list */

    /* event */
    public void addEvent(Event event) {
        if(mEventsArrayList != null) {
            mEventsArrayList.add(event);
        }
    }

    public boolean hasVoteForStock(String code) {
        if(mEventsArrayList != null) {
            for (int i = 0; i < mEventsArrayList.size(); i++) {
                Event event = mEventsArrayList.get(i);
                if(event.getEventTarget().equals(EVENT_TARGET_STOCK)
                        && event.getEventContent().equals(code)) {
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
                if(event.getEventTarget().equals(EVENT_TARGET_NEWS)
                        && event.getEventContent().equals(newsId)) {
                    return true;
                }
            }
        }
        return false;
    }
    /* end of event */

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

    private void initUserData(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(USER_PROFILE_SHARE_PREFERENCE, Context.MODE_PRIVATE);
        mUserId = sharedPreferences.getString(SHARE_PREF_ID_KEY, null);
        mUserName = sharedPreferences.getString(SHARE_PREF_NAME_KEY,
                context.getResources().getString(R.string.default_user_name));
        mUserEmail = sharedPreferences.getString(SHARE_PREF_EMAIL_KEY, null);
        mUserType = sharedPreferences.getString(SHARE_PREF_USER_TYPE, null);
        mUserAvatarLink = sharedPreferences.getString(SHARE_PREF_AVATAR_URL_KEY,
                context.getResources().getString(R.string.default_user_avatar_link));
        MSLog.d(String.format("init user data from share preference: %s %s %s %s",
                mUserId, mUserName, mUserEmail, mUserAvatarLink));
    }

    static UserProfile jsonObjectToUserProfile(JSONObject jsonObject) {
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
