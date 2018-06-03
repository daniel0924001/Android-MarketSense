package com.idroi.marketsense.data;

import android.content.Context;
import android.widget.ImageView;

import com.idroi.marketsense.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/5/10.
 */

public class CommentAndVote {

    private ArrayList<Comment> mCommentArray;
    private int mRaiseNumber;
    private int mFallNumber;
    private int mVoting;
    private int mPrediction;

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

    public void setVoting(int number) {
        mVoting = number;
    }

    public void setPrediction(int number) {
        mPrediction = number;
    }

    public int getRaiseNumber() {
        return mRaiseNumber;
    }

    public int getFallNumber() {
        return mFallNumber;
    }

    public String getVotingScore() {
        return String.valueOf(mVoting);
    }

    public String getVotingAttitude(Context context) {
        if(mVoting > 0) {
            return context.getResources().getString(R.string.title_people_look_good);
        } else if(mVoting == 0) {
            return context.getResources().getString(R.string.title_people_look_flat);
        } else {
            return context.getResources().getString(R.string.title_people_look_bad);
        }
    }

    public void setVotingIcon(Context context, ImageView imageView) {
        if(mVoting >= 0) {
            imageView.setBackground(context.getDrawable(R.drawable.btn_round_red));
            imageView.setImageDrawable(context.getDrawable(R.mipmap.ic_arrow_up_l));
        } else {
            imageView.setBackground(context.getDrawable(R.drawable.btn_round_green));
            imageView.setImageDrawable(context.getDrawable(R.mipmap.ic_arrow_down_l));
        }
    }

    public String getPredictionScore() {
        return String.valueOf(mPrediction);
    }

    public String getPredictionAttitude(Context context) {
        if(mPrediction > 0) {
            return context.getResources().getString(R.string.title_news_look_good);
        } else if(mPrediction == 0) {
            return context.getResources().getString(R.string.title_news_look_flat);
        } else {
            return context.getResources().getString(R.string.title_news_look_bad);
        }
    }

    public void setPredictionIcon(Context context, ImageView imageView) {
        if(mPrediction >= 0) {
            imageView.setBackground(context.getDrawable(R.drawable.btn_round_red));
            imageView.setImageDrawable(context.getDrawable(R.mipmap.ic_arrow_up_l));
        } else {
            imageView.setBackground(context.getDrawable(R.drawable.btn_round_green));
            imageView.setImageDrawable(context.getDrawable(R.mipmap.ic_arrow_down_l));
        }
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
