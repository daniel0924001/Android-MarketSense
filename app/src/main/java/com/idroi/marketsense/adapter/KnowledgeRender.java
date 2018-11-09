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
import com.idroi.marketsense.data.Knowledge;
import com.idroi.marketsense.viewholders.KnowledgeViewHolder;

import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/9/19.
 */

public class KnowledgeRender implements MarketSenseRenderer<Knowledge> {

    @NonNull private final WeakHashMap<View, KnowledgeViewHolder> mViewHolderMap;

    KnowledgeRender() {
        mViewHolderMap = new WeakHashMap<>();
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(R.layout.knowledge_list_item, parent, false);
    }

    @Override
    public void renderView(@NonNull View view, @NonNull Knowledge content) {
        KnowledgeViewHolder knowledgeViewHolder = mViewHolderMap.get(view);
        if(knowledgeViewHolder == null) {
            knowledgeViewHolder = KnowledgeViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, knowledgeViewHolder);
        }

        MarketSenseRendererHelper.addTextView(knowledgeViewHolder.titleTextView, content.getKeyword());
        MarketSenseRendererHelper.addTextView(knowledgeViewHolder.descriptionTextView, content.getDefinition());
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
