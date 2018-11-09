package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.KnowledgeClass;
import com.idroi.marketsense.datasource.KnowledgeClassSource;

/**
 * Created by daniel.hsieh on 2018/11/8.
 */

public class KnowledgeClassAdapter extends RecyclerView.Adapter {

    private Activity mActivity;
    private Handler mHandler;

    private KnowledgeClassSource mKnowledgeClassSource;
    private KnowledgeClassRender mKnowledgeRender;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(KnowledgeClass knowledgeClass);
    }

    public KnowledgeClassAdapter(final Activity activity, KnowledgeClassSource knowledgeClassSource) {
        mActivity = activity;
        mKnowledgeClassSource = knowledgeClassSource;
        mKnowledgeRender = new KnowledgeClassRender();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mKnowledgeRender.createView(mActivity, parent));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final KnowledgeClass knowledgeClass = mKnowledgeClassSource.getItem(position);
        if(knowledgeClass != null) {
            mKnowledgeRender.renderView(holder.itemView, knowledgeClass);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mKnowledgeClassSource.getItem(holder.getAdapterPosition()));
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
}
