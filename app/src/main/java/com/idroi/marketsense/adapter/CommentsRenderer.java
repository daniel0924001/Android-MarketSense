package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    CommentsRenderer() {
        mViewHolderMap = new WeakHashMap<>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.comment_list_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull Comment content) {
        CommentViewHolder commentViewHolder = mViewHolderMap.get(view);
        if(commentViewHolder == null) {
            commentViewHolder = CommentViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, commentViewHolder);
        }

        MarketSenseRendererHelper.addTextView(commentViewHolder.userNameView, content.getUserName());
        MarketSenseRendererHelper.addTextView(commentViewHolder.createTimeView, content.getDateString());
        if(content.getAvatarUrl() != null) {
            FrescoImageHelper.loadImageView(content.getAvatarUrl(),
                    commentViewHolder.avatarView, FrescoImageHelper.ICON_IMAGE_RATIO);
        } else {
            FrescoImageHelper.loadImageView("http://monster.infohubapp.com/monsters/ic_mon_01_y.png",
                    commentViewHolder.avatarView, FrescoImageHelper.ICON_IMAGE_RATIO);
        }

        // chinese characters can not be decoded.
        // https://blog.csdn.net/top_code/article/details/9163597
        commentViewHolder.commentBodyView.
                loadDataWithBaseURL(null, content.getCommentHtml(), "text/html", "UTF-8", null);

        setViewVisibility(commentViewHolder, View.VISIBLE);
    }

    private void setViewVisibility(final CommentViewHolder stockViewHolder, final int visibility) {
        if(stockViewHolder.mainView != null) {
            stockViewHolder.mainView.setVisibility(visibility);
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
