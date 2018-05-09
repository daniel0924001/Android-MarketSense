package com.idroi.marketsense.data;

import com.idroi.marketsense.Logging.MSLog;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class UserProfile {

    private static final String USER_ID = "user_id";
    private static final String USER_TYPE = "user_type";
    private static final String USER_EMAIL = "email";
    private static final String USER_NAME = "user_name";
    private static final String USER_TOKEN = "user_token";
    private static final String USER_AVATAR_LINK = "user_avatar_link";

    private String mUserId;
    private String mUserType;
    private String mUserEmail;
    private String mUserName;
    private String mUserToken;
    private String mUserAvatarLink;

    public UserProfile() {
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

    public void setUserToken(String token) {
        mUserToken = token;
    }

    public void setUserAvatarLink(String avatarLink) {
        mUserAvatarLink = avatarLink;
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

    public String getUserToken() {
        return mUserToken;
    }

    public String getUserAvatarLink() {
        return mUserAvatarLink;
    }

    public static UserProfile jsonObjectToUserProfile(JSONObject jsonObject) {
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
                    case USER_TOKEN:
                        userProfile.setUserToken(jsonObject.optString(USER_TOKEN));
                        break;
                    case USER_AVATAR_LINK:
                        userProfile.setUserAvatarLink(jsonObject.optString(USER_AVATAR_LINK));
                        break;
                    default:
                        break;
                }
            } catch (ClassCastException e) {
                MSLog.e(e.toString());
            } catch (Exception e) {
                MSLog.e(e.toString());
            }
        }

        return userProfile;
    }
}
