package com.idroi.marketsense.datasource;

import android.app.Activity;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.data.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class CommentsPlacer {

    public interface CommentsListener {
        void onCommentsLoaded();
        void onCommentsNoneOrFailed();
    }

    private static final int RETRY_TIME_CONST = 2;
    private int mCurrentRetries = 0;

    private ArrayList<Comment> mCommentArrayList;
    private MarketSenseCommentsFetcher.MarketSenseCommentsNetworkListener mMarketSenseCommentNetworkListener;
    private CommentsListener mCommentsListener;

    private Activity mActivity;
    private MarketSenseCommentsFetcher mMarketSenseCommentsFetcher;
    private String mUrl;

    public CommentsPlacer(Activity activity) {
        mActivity = activity;

        mMarketSenseCommentNetworkListener = new MarketSenseCommentsFetcher.MarketSenseCommentsNetworkListener() {
            @Override
            public void onCommentsLoad(ArrayList<Comment> commentArrayList) {
                if(mCommentArrayList != null) {
                    mCommentArrayList.clear();
                }
                mCommentArrayList = commentArrayList;

                Collections.sort(mCommentArrayList, genComparator());

                if(mCommentsListener != null) {
                    if(mCommentArrayList.size() > 0) {
                        mCommentsListener.onCommentsLoaded();
                    } else {
                        mCommentsListener.onCommentsNoneOrFailed();
                    }
                }
            }

            @Override
            public void onCommentsFail(MarketSenseError marketSenseError) {
                increaseRetryTime();
                if(isRetry()) {
                    mMarketSenseCommentsFetcher.makeRequest(mUrl);
                } else {
                    mCommentsListener.onCommentsNoneOrFailed();
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

    public Comment getCommentData(int position) {
        if(mCommentArrayList == null || position >= mCommentArrayList.size() || position < 0) {
            return null;
        }
        return mCommentArrayList.get(position);
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
