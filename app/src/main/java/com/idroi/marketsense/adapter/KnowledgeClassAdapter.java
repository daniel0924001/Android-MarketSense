package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.data.KnowledgeClass;
import com.idroi.marketsense.datasource.KnowledgeClassSource;

import java.util.List;

/**
 * Created by daniel.hsieh on 2018/11/8.
 */

public class KnowledgeClassAdapter extends RecyclerView.Adapter {

    private Activity mActivity;

    private KnowledgeClassSource mKnowledgeClassSource;
    private KnowledgeClassRender mKnowledgeRender;
    private OnItemClickListener mOnItemClickListener;
    private RedDotChangeData mRedDotChangeData;

    public interface OnItemClickListener {
        void onItemClick(KnowledgeClass knowledgeClass);
    }

    public KnowledgeClassAdapter(final Activity activity, KnowledgeClassSource knowledgeClassSource) {
        mActivity = activity;
        mKnowledgeClassSource = knowledgeClassSource;
        mKnowledgeRender = new KnowledgeClassRender();
        mRedDotChangeData = new RedDotChangeData();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mKnowledgeRender.createView(mActivity, parent));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        if(payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            RedDotChangeData redDotChangeData = (RedDotChangeData) payloads.get(0);
            final KnowledgeClass knowledgeClass = mKnowledgeClassSource.getItem(position);

            if(position == redDotChangeData.closePosition) {
                knowledgeClass.setPresent(false);
            } else if(position == redDotChangeData.openPosition) {
                knowledgeClass.setPresent(true);
            }
            mKnowledgeRender.updateRedDot(holder.itemView, knowledgeClass);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final KnowledgeClass knowledgeClass = mKnowledgeClassSource.getItem(position);
        if(knowledgeClass != null) {
            mKnowledgeRender.renderView(holder.itemView, knowledgeClass);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRedDotChangeData.closePosition = mRedDotChangeData.openPosition;
                mRedDotChangeData.openPosition = holder.getAdapterPosition();

                notifyItemChanged(mRedDotChangeData.closePosition, mRedDotChangeData);
                notifyItemChanged(mRedDotChangeData.openPosition, mRedDotChangeData);

                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mKnowledgeClassSource.getItem(mRedDotChangeData.openPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mKnowledgeClassSource.getSize();
    }

    public void destroy() {
        mKnowledgeRender.clear();
        mKnowledgeClassSource.destroy();
    }

    private static class RedDotChangeData {
        private int closePosition;
        private int openPosition;

        RedDotChangeData() {
            openPosition = 0;
            closePosition = -1;
        }
    }
}
