package com.idroi.marketsense.data;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.DateConverter;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class Comment {

    private static final String TIMESTAMP = "created_ts";       // the timestamp of this event
    private static final String COMMENT_ID = "event_id";        // the unique id of comment
    private static final String HTML = "event_value";           // comment body
    private static final String CONTENT_ID = "event_content";   // newsId or stock code
    private static final String COMMENT_TYPE = "event_type";    // raise, fall or normal
    private static final String COMMENT_TARGET = "event_target";// news or stock
    private static final String USER_PROFILE = "user_profile";  // user profile: name, avatar_link

    private String mUserId;
    private String mCommentId;
    private String mCommentTarget;
    private String mCommentType;
    private String mCommentHtml;
    private String mContentId;
    private String mDateString;
    private int mTimeStamp;

    private UserProfile mUserProfile;

    public Comment() {

    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public void setCommentId(String commentId) {
        mCommentId = commentId;
    }

    public void setCommentTarget(String commentTarget) {
        mCommentTarget = commentTarget;
    }

    public void setCommentType(String commentType) {
        mCommentType = commentType;
    }

    public void setCommentHtml(String commentHtml) {
        mCommentHtml = commentHtml;
    }

    public void setContentId(String contentId) {
        mContentId = contentId;
    }

    public void setUserProfile(UserProfile userProfile) {
        mUserProfile = userProfile;
    }

    public void setTimeStamp(int timeStamp) {
        mTimeStamp = timeStamp;
    }

    public void setDateString(String dateString) {
        mDateString = dateString;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getCommentId() {
        return mCommentId;
    }

    public String getCommentTarget() {
        return mCommentTarget;
    }

    public String getCommentType() {
        return mCommentType;
    }

    public String getCommentHtml() {
        return mCommentHtml;
    }

    public String getContentId() {
        return mContentId;
    }

    public String getUserName() {
        if(mUserProfile != null) {
            return mUserProfile.getUserName();
        } else {
            // TODO: this should be the login user name
            return "使用者";
        }
    }

    public String getAvatarUrl() {
        if(mUserProfile != null) {
            return mUserProfile.getUserAvatarLink();
        } else {
            // TODO: this should be the login user name
            return null;
        }
    }

    public int getTimeStamp() {
        return mTimeStamp;
    }

    public String getDateString() {
        if(mDateString != null) {
            return mDateString;
        } else {
            return "剛剛";
        }
    }

    public static Comment jsonObjectToComment(JSONObject jsonObject) {
        Comment comment = new Comment();
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                switch (key) {
                    case TIMESTAMP:
                        comment.setTimeStamp(jsonObject.optInt(TIMESTAMP));
                        comment.setDateString(DateConverter.convertToDate(jsonObject.optInt(TIMESTAMP)));
                        break;
                    case COMMENT_ID:
                        comment.setCommentId(jsonObject.optString(COMMENT_ID));
                        break;
                    case CONTENT_ID:
                        comment.setContentId(jsonObject.optString(CONTENT_ID));
                        break;
                    case HTML:
                        comment.setCommentHtml(jsonObject.optString(HTML));
                        break;
                    case COMMENT_TYPE:
                        comment.setCommentType(jsonObject.optString(COMMENT_TYPE));
                        break;
                    case COMMENT_TARGET:
                        comment.setCommentTarget(jsonObject.optString(COMMENT_TARGET));
                        break;
                    case USER_PROFILE:
                        comment.setUserProfile(UserProfile.jsonObjectToUserProfile(
                                jsonObject.optJSONObject(USER_PROFILE)));
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
        return comment;
    }
}
