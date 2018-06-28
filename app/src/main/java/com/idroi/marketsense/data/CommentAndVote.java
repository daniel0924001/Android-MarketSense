package com.idroi.marketsense.data;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by daniel.hsieh on 2018/5/10.
 */

public class CommentAndVote {

    private ArrayList<Comment> mCommentArray;
    private int mRaiseNumber;
    private int mFallNumber;
    private double mVoting;
    private double mPrediction;

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

    public void setVoting(double number) {
        mVoting = number;
    }

    public void setPrediction(double number) {
        mPrediction = number;
    }

    public int getRaiseNumber() {
        return mRaiseNumber;
    }

    public int getFallNumber() {
        return mFallNumber;
    }

    public void setVotingText(TextView scoreTextView, TextView maxTextView) {
        if(mVoting == 0) {
            scoreTextView.setText(R.string.title_no_prediction);
            maxTextView.setVisibility(View.INVISIBLE);
        } else {
            scoreTextView.setText(String.format(Locale.US, "%.1f", ((float) mVoting * 5 / 3)));
            maxTextView.setVisibility(View.VISIBLE);
        }
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
        float scale = context.getResources().getDisplayMetrics().density;
        int padding = (int) (8 * scale + 0.5f); // 8dp
        if(mVoting > 0) {
            imageView.setBackground(context.getDrawable(R.drawable.btn_round_red));
            imageView.setImageDrawable(context.getDrawable(R.mipmap.ic_arrow_up_l));
            imageView.setPadding(padding, padding, padding, padding);
        } else if(mVoting < 0) {
            imageView.setBackground(context.getDrawable(R.drawable.btn_round_green));
            imageView.setImageDrawable(context.getDrawable(R.mipmap.ic_arrow_down_l));
            imageView.setPadding(padding, padding, padding, padding);
        } else {
            imageView.setBackground(context.getDrawable(R.drawable.btn_round_theme_color));
            imageView.setImageDrawable(context.getDrawable(R.mipmap.ic_0score));
            imageView.setPadding(0, 0, 0, 0);
        }
    }

    public void setPredictionText(TextView scoreTextView, TextView maxTextView) {
        if(mPrediction == 0) {
            scoreTextView.setText(R.string.title_no_prediction);
            maxTextView.setVisibility(View.INVISIBLE);
        } else {
            scoreTextView.setText(String.format(Locale.US, "%.1f", ((float) mPrediction * 5 / 3)));
            maxTextView.setVisibility(View.VISIBLE);
        }
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
        float scale = context.getResources().getDisplayMetrics().density;
        int padding = (int) (8 * scale + 0.5f); // 8dp
        if(mPrediction > 0) {
            imageView.setBackground(context.getDrawable(R.drawable.btn_round_red));
            imageView.setImageDrawable(context.getDrawable(R.mipmap.ic_arrow_up_l));
            imageView.setPadding(padding, padding, padding, padding);
        } else if(mPrediction < 0) {
            imageView.setBackground(context.getDrawable(R.drawable.btn_round_green));
            imageView.setImageDrawable(context.getDrawable(R.mipmap.ic_arrow_down_l));
            imageView.setPadding(padding, padding, padding, padding);
        } else {
            imageView.setBackground(context.getDrawable(R.drawable.btn_round_theme_color));
            imageView.setImageDrawable(context.getDrawable(R.mipmap.ic_0score));
            imageView.setPadding(0, 0, 0, 0);
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
