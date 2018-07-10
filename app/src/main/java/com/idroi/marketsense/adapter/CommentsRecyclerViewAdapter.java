package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.NewsWebView;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.datasource.CommentsPlacer;

import java.util.ArrayList;

import static com.idroi.marketsense.data.Comment.VIEW_TYPE_REPLY;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter {

    public interface OnItemClickListener {
        void onSayLikeItemClick(Comment comment, int position);
        void onReplyItemClick(Comment comment, int position);
    }

    public interface CommentsAvailableListener {
        void onCommentsAvailable(CommentAndVote commentAndVote);
    }

    public interface OnNewsItemClickListener {
        void onNewsItemClick(News news);
    }

    private Activity mActivity;
    private CommentsPlacer mCommentsPlacer;
    private CommentsRenderer mCommentsRenderer;
    private ReplyRenderer mReplyRenderer;
    private OnItemClickListener mOnItemClickListener;
    private CommentsAvailableListener mCommentsAvailableListener;
    private OnNewsItemClickListener mOnNewsItemClickListener;

    private Handler mHandler;

    public CommentsRecyclerViewAdapter(final Activity activity) {
        this(activity, false, null, null);
    }

    public CommentsRecyclerViewAdapter(final Activity activity, OnItemClickListener listener) {
        this(activity, false, listener, null);
    }

    public CommentsRecyclerViewAdapter(final Activity activity, boolean largeBorder,
                                       OnItemClickListener listener, OnNewsItemClickListener newsListener) {
        mActivity = activity;
        mCommentsPlacer = new CommentsPlacer(activity);
        mOnItemClickListener = listener;
        mCommentsRenderer = new CommentsRenderer(largeBorder, mOnItemClickListener, newsListener);
        mReplyRenderer = new ReplyRenderer();
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
                if(mCommentsAvailableListener != null) {
                    mCommentsAvailableListener.onCommentsAvailable(null);
                }
            }
        });
    }

    public void setCommentsAvailableListener(CommentsAvailableListener listener) {
        mCommentsAvailableListener = listener;
    }

    public void loadCommentsList(String url) {
        mCommentsPlacer.loadComments(url);
    }

    public void setCommentArrayList(ArrayList<Comment> arrayList) {
        mCommentsPlacer.setCommentArrayList(arrayList);
        notifyDataSetChanged();
    }

    public void addOneComment(Comment comment) {
        mCommentsPlacer.addOneComment(comment);
        notifyItemInserted(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_REPLY) {
            return new MarketSenseViewHolder(mReplyRenderer.createView(mActivity, parent));
        } else {
            return new MarketSenseViewHolder(mCommentsRenderer.createView(mActivity, parent));
        }
    }

    @Override
    public int getItemViewType(int position) {
        Comment comment = mCommentsPlacer.getCommentData(position);
        return comment.getViewType();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Comment comment = mCommentsPlacer.getCommentData(position);
        if(comment != null) {
            int viewType = comment.getViewType();
            if(viewType == VIEW_TYPE_REPLY) {
                mReplyRenderer.renderView(holder.itemView, comment);
            } else {
                mCommentsRenderer.renderView(holder.itemView, comment);
                mCommentsRenderer.setClickListener(holder.itemView, comment, position);
            }
        }
    }

    public void cloneSocialContent(int position, Comment other) {
        Comment comment = getComment(position);
        comment.setLikeNumber(other.getLikeNumber());
        comment.cloneReplies(other.getReplyArrayList());
        comment.setLike(other.isLiked());
    }

    public void updateCommentsLike() {
        mCommentsPlacer.updateCommentsLike();
        notifyItemRangeChanged(0, getItemCount());
    }

    private Comment getComment(int position) {
        return mCommentsPlacer.getCommentData(position);
    }

    @Override
    public int getItemCount() {
        return mCommentsPlacer.getItemCount();
    }

    public void destroy() {
        mCommentsRenderer.clear();
        mReplyRenderer.clear();
        mCommentsPlacer.clear();
        mCommentsAvailableListener = null;
    }
}
