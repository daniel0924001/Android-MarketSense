package com.idroi.marketsense;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * Created by daniel.hsieh on 2018/8/8.
 */

public class CommentTextView extends android.support.v7.widget.AppCompatTextView {

    public interface OnReachMaxHeightListener {
        void onReachMaxHeight();
    }

    private int mMaxLineCount = -1;
    private OnReachMaxHeightListener mOnReachMaxHeightListener;
    private boolean mIsReachMaxHeight = false;

    public CommentTextView(Context context) {
        super(context);
    }


    public CommentTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public CommentTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnReachMaxHeightListener(OnReachMaxHeightListener listener) {
        mIsReachMaxHeight = false;
        mOnReachMaxHeightListener = listener;
    }

    public void setMaxLineCount(int lineCount) {
        mMaxLineCount = lineCount;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(mMaxLineCount > -1 && getLineCount() > mMaxLineCount && !mIsReachMaxHeight) {
            mIsReachMaxHeight = true;
            setMaxLines(mMaxLineCount);
            if(mOnReachMaxHeightListener != null) {
                mOnReachMaxHeightListener.onReachMaxHeight();
            }
        }
    }
}
