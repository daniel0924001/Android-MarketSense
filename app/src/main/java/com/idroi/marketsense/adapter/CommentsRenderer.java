package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.FrescoImageHelper;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.Comment;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/5/9.
 */

public class CommentsRenderer implements MarketSenseRenderer<Comment> {

    @NonNull private final WeakHashMap<View, CommentViewHolder> mViewHolderMap;
    @Nullable private final CommentsRecyclerViewAdapter.OnItemClickListener mOnItemClickListener;

    CommentsRenderer(@Nullable CommentsRecyclerViewAdapter.OnItemClickListener listener) {
        mViewHolderMap = new WeakHashMap<>();
        mOnItemClickListener = listener;
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.comment_list_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull final Comment content) {
        CommentViewHolder commentViewHolder = mViewHolderMap.get(view);
        if(commentViewHolder == null) {
            commentViewHolder = CommentViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, commentViewHolder);
        }

        MarketSenseRendererHelper.addTextView(commentViewHolder.userNameView, content.getUserName());
        MarketSenseRendererHelper.addTextView(commentViewHolder.createTimeView, content.getDateString());
        if(content.getAvatarUrl() != null && !content.getAvatarUrl().isEmpty()) {
            FrescoImageHelper.loadImageView(content.getAvatarUrl(),
                    commentViewHolder.avatarView, FrescoImageHelper.ICON_IMAGE_RATIO);
        } else {
            FrescoImageHelper.loadImageView("http://monster.infohubapp.com/monsters/ic_mon_01_y.png",
                    commentViewHolder.avatarView, FrescoImageHelper.ICON_IMAGE_RATIO);
        }

        // chinese characters can not be decoded.
        // https://blog.csdn.net/top_code/article/details/9163597
        // we have a /assets/img.css file.
        String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"img.css\" />" + content.getCommentHtml();
        commentViewHolder.commentBodyView.
                loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);

        int likeNum = content.getLikeNumber();
        int replyNum = content.getReplyNumber();
        setNumberVisibility(view.getContext(), commentViewHolder, likeNum, replyNum);

        commentViewHolder.replyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onReplyItemClick(content);
                }
            }
        });
        if(content.isLiked()) {
            commentViewHolder.likeView.setText(view.getContext().getResources().getString(R.string.title_good_already));
            commentViewHolder.likeView.setOnClickListener(null);
        } else {
            commentViewHolder.likeView.setText(view.getContext().getResources().getString(R.string.title_good));
            commentViewHolder.likeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onSayLikeItemClick(content);
                    }
                }
            });
        }

        setViewVisibility(commentViewHolder, View.VISIBLE);
    }

    private void setNumberVisibility(Context context, final CommentViewHolder commentViewHolder,
                                     final int likeNumber, final int replyNumber) {

        MSLog.d("like, reply: " + likeNumber + ", " + replyNumber);
        if(replyNumber > 0) {
            commentViewHolder.replyNumberView.setVisibility(View.VISIBLE);
            String replyNumberString = String.format(context.getResources().getString(R.string.title_reply_number), replyNumber);
            commentViewHolder.replyNumberView.setText(replyNumberString);
        } else {
            commentViewHolder.replyNumberView.setVisibility(View.GONE);
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) commentViewHolder.socialFunctionView.getLayoutParams();
        if(likeNumber > 0) {
            commentViewHolder.likeNumberView.setVisibility(View.VISIBLE);
            String likeNumberString = String.format(context.getResources().getString(R.string.title_good_number), likeNumber);
            commentViewHolder.likeNumberView.setText(likeNumberString);
            params.topToBottom = R.id.tv_like_number;
        } else {
            commentViewHolder.likeNumberView.setVisibility(View.GONE);
            if(replyNumber > 0) {
                params.topToBottom = R.id.tv_reply_number;
            } else {
                params.topToBottom = R.id.comment_body;
            }
        }
        commentViewHolder.socialFunctionView.setLayoutParams(params);
    }

    private void setViewVisibility(final CommentViewHolder commentViewHolder, final int visibility) {
        if(commentViewHolder.mainView != null) {
            commentViewHolder.mainView.setVisibility(visibility);
        }
    }

    @Override
    public void clear() {
        for(Map.Entry<View, CommentViewHolder> entry : mViewHolderMap.entrySet()) {
            CommentViewHolder commentViewHolder = entry.getValue();
            commentViewHolder.commentBodyView.destroy();
        }
        mViewHolderMap.clear();
    }
}
