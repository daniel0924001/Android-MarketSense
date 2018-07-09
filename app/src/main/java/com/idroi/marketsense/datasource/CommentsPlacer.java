package com.idroi.marketsense.datasource;

import android.app.Activity;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class CommentsPlacer {

    public interface CommentsListener {
        void onCommentsLoaded(CommentAndVote commentAndVote);
        void onCommentsFailed();
    }

    private static final int RETRY_TIME_CONST = 2;
    private int mCurrentRetries = 0;

    private ArrayList<Comment> mCommentArrayList;
    private int mRaiseNumber;
    private int mFallNumber;
    private MarketSenseCommentsFetcher.MarketSenseCommentsNetworkListener mMarketSenseCommentNetworkListener;
    private CommentsListener mCommentsListener;

    private Activity mActivity;
    private MarketSenseCommentsFetcher mMarketSenseCommentsFetcher;
    private String mUrl;

    public CommentsPlacer(Activity activity) {
        mActivity = activity;

        mMarketSenseCommentNetworkListener = new MarketSenseCommentsFetcher.MarketSenseCommentsNetworkListener() {
            @Override
            public void onCommentsLoad(CommentAndVote commentAndVote) {

                if(mMarketSenseCommentsFetcher == null) {
                    return;
                }

                if(mCommentArrayList != null) {
                    mCommentArrayList.clear();
                }
                mCommentArrayList = commentAndVote.getCommentArray();
                mRaiseNumber = commentAndVote.getRaiseNumber();
                mFallNumber = commentAndVote.getFallNumber();

                if(mCommentArrayList != null) {
                    Collections.sort(mCommentArrayList, genComparator());
                }

                if(mCommentsListener != null) {
                    mCommentsListener.onCommentsLoaded(commentAndVote);
                }
            }

            @Override
            public void onCommentsFail(MarketSenseError marketSenseError) {

                if(mMarketSenseCommentsFetcher == null) {
                    return;
                }

                increaseRetryTime();
                if(isRetry()) {
                    mMarketSenseCommentsFetcher.makeRequest(mUrl);
                } else {
                    mCommentsListener.onCommentsFailed();
                }
            }
        };
        resetRetryTime();
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

    public void setCommentsListener(CommentsListener listener) {
        mCommentsListener = listener;
    }

    public void addOneComment(Comment comment) {
        if(mCommentArrayList != null) {
            mCommentArrayList.add(0, comment);
        } else {
            mCommentArrayList = new ArrayList<>();
            mCommentArrayList.add(comment);
        }
    }

    public void setCommentArrayList(ArrayList<Comment> arrayList) {
        if(arrayList != null) {
            mCommentArrayList = new ArrayList<Comment>(arrayList);
        }
    }

    public void loadComments(String url) {
        mUrl = url;
        loadComments(new MarketSenseCommentsFetcher(mActivity, mMarketSenseCommentNetworkListener));
    }

    private void loadComments(MarketSenseCommentsFetcher commentsFetcher) {
        clear();
        mMarketSenseCommentsFetcher = commentsFetcher;
        mMarketSenseCommentsFetcher.makeRequest(mUrl);
    }

    public void clear() {
        if(mCommentArrayList != null) {
            mCommentArrayList.clear();
            mCommentArrayList = null;
        }
        if(mMarketSenseCommentsFetcher != null) {
            mMarketSenseCommentsFetcher.destroy();
            mMarketSenseCommentsFetcher = null;
        }
    }

    public int getItemCount() {
        if(mCommentArrayList != null) {
            return mCommentArrayList.size();
        } else {
            return 0;
        }
    }

    public void updateCommentsLike() {
        MSLog.d("update comments' like information");
        for(int i = 0; i < mCommentArrayList.size(); i++) {
            Comment comment = mCommentArrayList.get(i);
            comment.updateLikeUserProfile();
        }
    }

    public Comment getCommentData(int position) {
        if(mCommentArrayList == null || position >= mCommentArrayList.size() || position < 0) {
            return null;
        }
        return mCommentArrayList.get(position);
    }

    public int getRaiseNumber() {
        return mRaiseNumber;
    }

    public int getFallNumber() {
        return mFallNumber;
    }

    private Comparator<Comment> genComparator() {
        return new Comparator<Comment>() {
            @Override
            public int compare(Comment comment1, Comment comment2) {
                return comment2.getTimeStamp() - comment1.getTimeStamp();
            }
        };
    }
}
