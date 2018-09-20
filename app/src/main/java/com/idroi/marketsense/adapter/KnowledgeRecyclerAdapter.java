package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.Knowledge;
import com.idroi.marketsense.datasource.KnowledgeFetcher;
import com.idroi.marketsense.datasource.KnowledgePlacer;

/**
 * Created by daniel.hsieh on 2018/9/19.
 */

public class KnowledgeRecyclerAdapter extends RecyclerView.Adapter {

    public interface KnowledgeAvailableListener {
        void onKnowledgeAvailable();
        void onKnowledgeEmpty();
    }

    public interface OnItemClickListener {
        void onItemClick(Knowledge knowledge);
    }

    private Activity mActivity;
    private Handler mHandler;
    private KnowledgePlacer mKnowledgePlacer;
    private KnowledgeRender mKnowledgeRender;
    private KnowledgeAvailableListener mKnowledgeAvailableListener;
    private OnItemClickListener mOnItemClickListener;

    public KnowledgeRecyclerAdapter(final Activity activity) {
        mActivity = activity;
        mHandler = new Handler();
        mKnowledgePlacer = new KnowledgePlacer(activity);
        mKnowledgeRender = new KnowledgeRender();
        mKnowledgePlacer.setKnowledgeListListener(new KnowledgePlacer.KnowledgeListListener() {
            @Override
            public void onKnowledgeListLoaded() {
                if(mKnowledgeAvailableListener != null) {
                    if(mKnowledgePlacer.isEmpty()) {
                        mKnowledgeAvailableListener.onKnowledgeEmpty();
                    } else {
                        mKnowledgeAvailableListener.onKnowledgeAvailable();
                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public void setKnowledgeAvailableListener(KnowledgeAvailableListener listener) {
        mKnowledgeAvailableListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void loadKnowledgeList(String networkUrl, String cacheUrl) {
        mKnowledgePlacer.loadKnowledgeList(networkUrl, cacheUrl);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mKnowledgeRender.createView(mActivity, parent));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Knowledge knowledge = mKnowledgePlacer.getKnowledge(position);
        if(knowledge != null) {
            mKnowledgeRender.renderView(holder.itemView, knowledge);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mKnowledgePlacer.getKnowledge(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mKnowledgePlacer.getItemCount();
    }

    public void destroy() {
        mKnowledgePlacer.clear();
    }
}
