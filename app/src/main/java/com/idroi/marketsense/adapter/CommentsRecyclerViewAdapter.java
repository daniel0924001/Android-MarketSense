package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.datasource.CommentsPlacer;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter {

    public interface OnItemClickListener {
        void onSayLikeItemClick(Comment comment);
        void onReplyItemClick(Comment comment);
    }

    public interface CommentsAvailableListener {
        void onCommentsAvailable(CommentAndVote commentAndVote);
    }

    private Activity mActivity;
    private CommentsPlacer mCommentsPlacer;
    private CommentsRenderer mCommentsRenderer;
    private OnItemClickListener mOnItemClickListener;
    private CommentsAvailableListener mCommentsAvailableListener;

    private Handler mHandler;

    public CommentsRecyclerViewAdapter(final Activity activity) {
        this(activity, null);
    }

    public CommentsRecyclerViewAdapter(final Activity activity, OnItemClickListener listener) {
        mActivity = activity;
        mCommentsPlacer = new CommentsPlacer(activity);
        mOnItemClickListener = listener;
        mCommentsRenderer = new CommentsRenderer(mOnItemClickListener);
        mHandler = new Handler();
        mCommentsPlacer.setCommentsListener(new CommentsPlacer.CommentsListener() {
            @Override
            public void onCommentsLoaded(CommentAndVote commentAndVote) {
                if(mCommentsAvailableListener != null) {
                    mCommentsAvailableListener.onCommentsAvailable(commentAndVote);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onCommentsFailed() {
                MSLog.d("no comment or failed");
            }
        });
    }

    public void setCommentsAvailableListener(CommentsAvailableListener listener) {
        mCommentsAvailableListener = listener;
    }

    public void loadCommentsList(String url) {
        mCommentsPlacer.loadComments(url);
    }

    public void addOneComment(Comment comment) {
        mCommentsPlacer.addOneComment(comment);
        notifyItemInserted(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketSenseViewHolder(mCommentsRenderer.createView(mActivity, parent));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Comment comment = mCommentsPlacer.getCommentData(position);
        if(comment != null) {
            mCommentsRenderer.renderView(holder.itemView, comment);
        }
    }

    public Comment getComment(int position) {
        return mCommentsPlacer.getCommentData(position);
    }

    public int getItemPositionById(String eventId) {
        return mCommentsPlacer.getItemPositionById(eventId);
    }

    @Override
    public int getItemCount() {
        return mCommentsPlacer.getItemCount();
    }

    public void destroy() {
        mCommentsRenderer.clear();
        mCommentsPlacer.clear();
        mCommentsAvailableListener = null;
    }
}
