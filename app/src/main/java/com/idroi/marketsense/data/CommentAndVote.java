package com.idroi.marketsense.data;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/5/10.
 */

public class CommentAndVote {

    private ArrayList<Comment> mCommentArray;
    private int mRaiseNumber;
    private int mFallNumber;

    public CommentAndVote() {
    }

    public void addComment(Comment comment) {
        if(mCommentArray == null) {
            mCommentArray = new ArrayList<Comment>();
        }
        mCommentArray.add(comment);
    }

    public void setRaiseNumber(int number) {
        mRaiseNumber = number;
    }

    public void setFallNumber(int number) {
        mFallNumber = number;
    }

    public int getRaiseNumber() {
        return mRaiseNumber;
    }

    public int getFallNumber() {
        return mFallNumber;
    }

    public ArrayList<Comment> getCommentArray() {
        return mCommentArray;
    }

    public int getCommentSize() {
        if(mCommentArray != null) {
            return mCommentArray.size();
        } else {
            return 0;
        }
    }
}
