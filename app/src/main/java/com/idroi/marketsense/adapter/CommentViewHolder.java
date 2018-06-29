package com.idroi.marketsense.adapter;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.NewsWebView;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/5/9.
 */

public class CommentViewHolder {

    View mainView;
    SimpleDraweeView avatarView;
    TextView userNameView;
    TextView createTimeView;
    NewsWebView commentBodyView;

    TextView replyNumberView;
    TextView likeNumberView;

    TextView replyView;
    TextView likeView;

    ConstraintLayout socialFunctionView;

    static final CommentViewHolder EMPTY_VIEW_HOLDER = new CommentViewHolder();

    private CommentViewHolder() {

    }

    static CommentViewHolder convertToViewHolder(final View view) {
        final CommentViewHolder commentViewHolder = new CommentViewHolder();
        commentViewHolder.mainView = view;
        try {
            commentViewHolder.avatarView = view.findViewById(R.id.comment_avatar_image_iv);
            commentViewHolder.userNameView = view.findViewById(R.id.comment_user_name);
            commentViewHolder.createTimeView = view.findViewById(R.id.comment_create_time);

            // maybe we should use this javascript to adjust the height of webview
            // https://stackoverflow.com/questions/1973565/how-to-resize-a-android-webview-after-adding-data-in-it
            // https://capdroid.wordpress.com/2014/08/07/resizing-webview-to-match-the-content-size/
            commentViewHolder.commentBodyView = view.findViewById(R.id.comment_body);
            commentViewHolder.commentBodyView.getSettings().setLoadWithOverviewMode(false);
            commentViewHolder.commentBodyView.getSettings().setUseWideViewPort(false);

            commentViewHolder.replyNumberView = view.findViewById(R.id.tv_reply_number);
            commentViewHolder.likeNumberView = view.findViewById(R.id.tv_like_number);

            commentViewHolder.replyView = view.findViewById(R.id.tv_reply);
            commentViewHolder.likeView = view.findViewById(R.id.tv_like);

            commentViewHolder.socialFunctionView = view.findViewById(R.id.social_function_block);

            return commentViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
