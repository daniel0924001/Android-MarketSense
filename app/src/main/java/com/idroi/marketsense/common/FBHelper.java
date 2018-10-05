package com.idroi.marketsense.common;

import android.content.Context;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import static com.idroi.marketsense.common.Constants.FACEBOOK_CONSTANTS;

/**
 * Created by daniel.hsieh on 2018/5/11.
 */

public class FBHelper {

    public interface FBHelperListener {
        void onTaskCompleted(JSONObject data, String avatarLink);
    }

    public static boolean checkFBLogin() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && !accessToken.isExpired();
    }

    public static boolean isExpired() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null) {
            MSLog.d("check accessToken is expired: " + accessToken.isExpired());
            return accessToken.isExpired();
        } else {
            MSLog.d("check accessToken is expired: null");
            return false;
        }
    }

    public static void getFBUserProfile(final Context context,
                                        final FBHelperListener listener) {
        if(!FBHelper.checkFBLogin()) {
            MSLog.d("facebook not login");
            return;
        }

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        if(object == null){
                            return;
                        }

                        String url = null;
                        try {
                            //http://stackoverflow.com/questions/24176456/facebook-graph-api-always-returns-small-pictures
                            String id = object.getString("id");
                            url = String.format(Locale.US,
                                    "http://graph.facebook.com/%s/picture?type=large", id);
                            MSLog.d("The user's avatar image url: "+ url);
                        }
                        catch(org.json.JSONException e){
                            MSLog.e("JSONException occurs in getFBUserProfile");
                        }

                        updateUserInfoFB(context, object, url, listener);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, name, link, age_range, email, gender");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private static void updateUserInfoFB (
            Context context, JSONObject jsonObject, String avatarUrl, final FBHelperListener listener) {
        MSLog.d("updateUserInfoFB object: " + jsonObject.toString());
        MSLog.d("updateUserInfoFB avatar's url: " + avatarUrl);

        ClientData clientData = ClientData.getInstance(context);
        UserProfile userProfile = clientData.getUserProfile();

        String userId = fetchFbData(jsonObject, UserProfile.FB_USER_ID_KEY);
        userProfile.setUserId(userId);

        String userName = fetchFbData(jsonObject, UserProfile.FB_USER_NAME_KEY);
        userProfile.setUserName(userName);

        String userEmail = fetchFbData(jsonObject, UserProfile.FB_USER_EMAIL_KEY);
        userProfile.setUserEmail(userEmail);
        userProfile.setUserAvatarLink(avatarUrl);
        userProfile.setUserType(FACEBOOK_CONSTANTS);
        userProfile.updateUserData(context);

        listener.onTaskCompleted(jsonObject, avatarUrl);
    }

    public static String fetchFbData(JSONObject jsonObject, String key) {
        try {
            return jsonObject.getString(key);
        } catch (JSONException e) {
            MSLog.e("JSONException in fetchFbData of key: " + key + ", data: " + jsonObject.toString());
        }
        return null;
    }
}
