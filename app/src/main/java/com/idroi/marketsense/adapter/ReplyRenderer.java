package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.NewsWebView;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.FrescoImageHelper;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.Comment;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/7/4.
 */

public class ReplyRenderer implements MarketSenseRenderer<Comment> {

    @NonNull private final WeakHashMap<View, ReplyViewHolder> mViewHolderMap;

    ReplyRenderer() {
        mViewHolderMap = new WeakHashMap<>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.reply_list_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull Comment content) {
        ReplyViewHolder replyViewHolder = mViewHolderMap.get(view);
        if(replyViewHolder == null) {
            replyViewHolder = ReplyViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, replyViewHolder);
        }

        MarketSenseRendererHelper.addTextView(replyViewHolder.userNameView, content.getUserName());
        MarketSenseRendererHelper.addTextView(replyViewHolder.createTimeView, content.getDateString());
        if(content.getAvatarUrl() != null && !content.getAvatarUrl().isEmpty()) {
            FrescoImageHelper.loadImageView(content.getAvatarUrl(),
                    replyViewHolder.avatarView, FrescoImageHelper.ICON_IMAGE_RATIO);
        } else {
            FrescoImageHelper.loadImageView("http://monster.infohubapp.com/monsters/ic_mon_01_y.png",
                    replyViewHolder.avatarView, FrescoImageHelper.ICON_IMAGE_RATIO);
        }

        // chinese characters can not be decoded.
        // https://blog.csdn.net/top_code/article/details/9163597
        // we have a /assets/img.css file.
        String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"img.css\" />" + content.getCommentHtml();
        replyViewHolder.commentBodyView.
                loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);

        setViewVisibility(replyViewHolder, View.VISIBLE);
    }

    private void setViewVisibility(final ReplyViewHolder replyViewHolder, final int visibility) {
        if(replyViewHolder.mainView != null) {
            replyViewHolder.mainView.setVisibility(visibility);
        }
    }

    @Override
    public void clear() {
        for(Map.Entry<View, ReplyViewHolder> entry : mViewHolderMap.entrySet()) {
            ReplyViewHolder replyViewHolder = entry.getValue();
            replyViewHolder.commentBodyView.destroy();
        }
        mViewHolderMap.clear();
    }
}
