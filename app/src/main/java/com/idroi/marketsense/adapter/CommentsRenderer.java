package com.idroi.marketsense.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.NewsWebView;
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
        CommentViewHolder temp = mViewHolderMap.get(view);
        if(temp == null) {
            temp = CommentViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, temp);
        }
        final CommentViewHolder commentViewHolder = temp;

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
        commentViewHolder.commentBodyView.setOnReachMaxHeightListener(new NewsWebView.OnReachMaxHeightListener() {
            @Override
            public void onReachMaxHeight() {
                setReadMoreTextView(commentViewHolder, true);
            }
        });
        commentViewHolder.commentBodyView.
                loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
        setReadMoreTextView(commentViewHolder, false);

        int likeNum = content.getLikeNumber();
        int replyNum = content.getReplyNumber();

        commentViewHolder.replyView.setText(String.valueOf(replyNum));
        commentViewHolder.replyBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onReplyItemClick(content);
                }
            }
        });
        commentViewHolder.likeView.setText(String.valueOf(likeNum));
        if(content.isLiked()) {
            commentViewHolder.likeBlock.setOnClickListener(null);
            commentViewHolder.likeImageView.setImageResource(R.mipmap.ic_like_on);
        } else {
            commentViewHolder.likeBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onSayLikeItemClick(content);
                    }
                }
            });
            commentViewHolder.likeImageView.setImageResource(R.mipmap.ic_like_off);
        }

        setViewVisibility(commentViewHolder, View.VISIBLE);
    }

    private void setReadMoreTextView(final CommentViewHolder commentViewHolder, final boolean show) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                float density = commentViewHolder.readMoreView.getContext().getResources().getDisplayMetrics().density;
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) commentViewHolder.horizontalLineView.getLayoutParams();
                if(show) {
                    params.topMargin = (int) (37 * density);
                    commentViewHolder.readMoreView.setVisibility(View.VISIBLE);
                } else {
                    params.topMargin = (int) (16 * density);
                    commentViewHolder.readMoreView.setVisibility(View.GONE);
                }
                commentViewHolder.horizontalLineView.setLayoutParams(params);
            }
        });
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
