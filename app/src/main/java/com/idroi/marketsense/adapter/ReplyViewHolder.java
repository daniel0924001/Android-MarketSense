package com.idroi.marketsense.adapter;

import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.CommentTextView;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/7/4.
 */

public class ReplyViewHolder {

    View mainView;
    SimpleDraweeView avatarView;
    TextView userNameView;
    TextView createTimeView;
    CommentTextView commentBodyTextView;

    static final ReplyViewHolder EMPTY_VIEW_HOLDER = new ReplyViewHolder();

    private ReplyViewHolder() {

    }

    static ReplyViewHolder convertToViewHolder(final View view) {
        final ReplyViewHolder replyViewHolder = new ReplyViewHolder();
        replyViewHolder.mainView = view;
        try {
            replyViewHolder.avatarView = view.findViewById(R.id.comment_avatar_image_iv);
            replyViewHolder.userNameView = view.findViewById(R.id.comment_user_name);
            replyViewHolder.createTimeView = view.findViewById(R.id.comment_create_time);

            // maybe we should use this javascript to adjust the height of webview
            // https://stackoverflow.com/questions/1973565/how-to-resize-a-android-webview-after-adding-data-in-it
            // https://capdroid.wordpress.com/2014/08/07/resizing-webview-to-match-the-content-size/
            replyViewHolder.commentBodyTextView = view.findViewById(R.id.comment_body);
            return replyViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
