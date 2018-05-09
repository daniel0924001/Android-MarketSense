package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.datasource.CommentsPlacer;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter {

    public interface OnItemClickListener {
        void onItemClick(Comment comment);
    }

    public interface CommentsAvailableListener {
        void onCommentsAvailable();
    }

    public final static int NEWS_COMMENT_ID = 1;
    public final static int STOCK_COMMENT_ID = 2;

    public enum TASK {
        NEWS_COMMENT(NEWS_COMMENT_ID),
        STOCK_COMMENT(STOCK_COMMENT_ID);

        int taskId;
        TASK(int id) {
            taskId = id;
        }

        public int getTaskId() {
            return taskId;
        }
    }

    private Activity mActivity;
    private CommentsPlacer mCommentsPlacer;
    private CommentsRenderer mCommentsRenderer;
    private OnItemClickListener mOnItemClickListener;
    private CommentsAvailableListener mCommentsAvailableListener;

    private Handler mHandler;

    public CommentsRecyclerViewAdapter(final Activity activity) {
        mActivity = activity;
        mCommentsPlacer = new CommentsPlacer(activity);
        mCommentsRenderer = new CommentsRenderer();
        mHandler = new Handler();
        mCommentsPlacer.setCommentsListener(new CommentsPlacer.CommentsListener() {
            @Override
            public void onCommentsLoaded() {
                if(mCommentsAvailableListener != null) {
                    mCommentsAvailableListener.onCommentsAvailable();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onCommentsNoneOrFailed() {
                MSLog.d("no comment or failed");
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mCommentsPlacer.getCommentData(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCommentsPlacer.getItemCount();
    }

    public void destroy() {
        mCommentsRenderer.clear();
        mCommentsPlacer.clear();
    }
}
