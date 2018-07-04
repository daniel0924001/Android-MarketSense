package com.idroi.marketsense.data;

import android.support.annotation.Nullable;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.DateConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class Comment implements Serializable {

    private static final String TIMESTAMP = "created_ts";       // the timestamp of this event
    private static final String COMMENT_ID = "event_id";        // the unique id of comment
    private static final String HTML = "event_value";           // comment body
    private static final String CONTENT_ID = "event_content";   // newsId or stock code
    private static final String COMMENT_TYPE = "event_type";    // raise, fall or normal
    private static final String COMMENT_TARGET = "event_target";// news or stock
    private static final String USER_PROFILE = "user_profile";  // user profile: name, avatar_link

    private static final String REPLIES = "replies";            // replies (comments)
    private static final String LIKE = "like";                  // like number;
    private static final String LIKE_LIST = "like_list";        // user profile of like event

    public static final int VIEW_TYPE_COMMENT = 1;
    public static final int VIEW_TYPE_REPLY = 2;

    private String mUserId;
    private String mCommentId;
    private String mCommentTarget;
    private String mCommentType;
    private String mCommentHtml;
    private String mContentId;
    private String mDateString;
    private int mTimeStamp;

    private ArrayList<Comment> mReplyArrayList;
    private int mLikeNumber;
    private boolean mIsLiked = false;

    private int mViewType;

    private UserProfile mUserProfile;

    public Comment() {
        mViewType = VIEW_TYPE_COMMENT;
    }

    public void setViewType(int viewType) {
        mViewType = viewType;
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

    public void setLikeNumber(int likeNumber) {
        mLikeNumber = likeNumber;
    }

    public void setLikeUserProfile(JSONArray likes) {
        String userId = ClientData.getInstance().getUserProfile().getUserId();
        for(int i = 0; i < likes.length(); i++) {
            try {
                Event event = Event.JsonObjectToEvent(likes.getJSONObject(i));
                if(event.getUserId().equals(userId)) {
                    mIsLiked = true;
                }
            } catch (JSONException e) {
                MSLog.e("JSONException in setLikeUserProfile: " + i);
            }
        }
    }

    public void increaseLike() {
        mLikeNumber += 1;
    }

    public void decreaseLike() {
        mLikeNumber -= 1;
    }

    public void setReplies(JSONArray comments) {
        mReplyArrayList = new ArrayList<>();
        for(int i = 0; i < comments.length(); i++) {
            try {
                Comment comment = Comment.jsonObjectToComment(comments.getJSONObject(i));
                comment.setViewType(VIEW_TYPE_REPLY);
                mReplyArrayList.add(comment);
            } catch (JSONException e) {
                MSLog.e("JSONException in setReplies: " + i);
            }
        }
    }

    public void addReply(Comment comment) {
        if(mReplyArrayList == null) {
            mReplyArrayList = new ArrayList<>();
        }

        mReplyArrayList.add(0, comment);
    }

    public void cloneReplies(ArrayList<Comment> replyArrayList) {
        if(replyArrayList != null) {
            mReplyArrayList = new ArrayList<>(replyArrayList);
        }
    }

    public int getViewType() {
        return mViewType;
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
            if(ClientData.getInstance().getUserProfile() != null) {
                return ClientData.getInstance().getUserProfile().getUserName();
            }
            return "使用者";
        }
    }

    public String getAvatarUrl() {
        if(mUserProfile != null) {
            return mUserProfile.getUserAvatarLink();
        } else {
            if(ClientData.getInstance().getUserProfile() != null) {
                return ClientData.getInstance().getUserProfile().getUserAvatarLink();
            }
            return null;
        }
    }

    public int getTimeStamp() {
        return mTimeStamp;
    }

    @Nullable
    public ArrayList<Comment> getReplyArrayList() {
        return mReplyArrayList;
    }

    public int getReplyNumber() {
        if(mReplyArrayList != null){
            return mReplyArrayList.size();
        } else {
            return 0;
        }
    }

    public int getLikeNumber() {
        return mLikeNumber;
    }

    public String getDateString() {
        if(mDateString != null) {
            return mDateString;
        } else {
            return "剛剛";
        }
    }

    public void setLike(boolean isLike) {
        mIsLiked = isLike;
    }

    public boolean isLiked() {
        return mIsLiked;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Comment) {
            if(this.getCommentId().equals(((Comment)obj).getCommentId())) {
                return true;
            }
        }
        return false;
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
                    case LIKE:
                        comment.setLikeNumber(jsonObject.optInt(LIKE));
                        break;
                    case REPLIES:
                        comment.setReplies(jsonObject.optJSONArray(REPLIES));
                        break;
                    case LIKE_LIST:
                        comment.setLikeUserProfile(jsonObject.optJSONArray(LIKE_LIST));
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                MSLog.e(e.toString());
            }
        }
        return comment;
    }
}
