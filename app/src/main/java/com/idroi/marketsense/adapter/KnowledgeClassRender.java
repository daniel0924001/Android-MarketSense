package com.idroi.marketsense.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.KnowledgeClass;
import com.idroi.marketsense.viewholders.KnowledgeClassViewHolder;

import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/11/8.
 */

public class KnowledgeClassRender implements MarketSenseRenderer<KnowledgeClass> {

    private final WeakHashMap<View, KnowledgeClassViewHolder> mViewHolderMap;

    KnowledgeClassRender() {
        mViewHolderMap = new WeakHashMap<>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.knowledge_class_list_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull KnowledgeClass content) {
        KnowledgeClassViewHolder knowledgeClassViewHolder = mViewHolderMap.get(view);
        if(knowledgeClassViewHolder == null) {
            knowledgeClassViewHolder = KnowledgeClassViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, knowledgeClassViewHolder);
        }

        MarketSenseRendererHelper.addTextView(
                knowledgeClassViewHolder.titleTextView, content.getTitle());
        knowledgeClassViewHolder.iconImageView.setImageDrawable(content.getIcon());
        updateRedDot(view, content);
        view.setVisibility(View.VISIBLE);
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }

    public void updateRedDot(View view, KnowledgeClass knowledgeClass) {
        final KnowledgeClassViewHolder viewHolder = mViewHolderMap.get(view);
        if(viewHolder == null) {
            MSLog.e("knowledgeViewHolder is null in updateRedDot");
            return;
        }

        if(knowledgeClass.isPresent()) {
            viewHolder.presentImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.presentImageView.setVisibility(View.GONE);
        }
    }
}
