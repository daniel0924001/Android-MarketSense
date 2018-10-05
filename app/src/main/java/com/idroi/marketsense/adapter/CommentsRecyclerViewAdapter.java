package com.idroi.marketsense.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.SharedPreferencesCompat;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.datasource.CommentsPlacer;
import com.idroi.marketsense.datasource.MarketSenseCommentsFetcher;
import com.idroi.marketsense.request.CommentAndVoteRequest;

import java.util.ArrayList;
import java.util.List;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_REQUEST_NAME;
import static com.idroi.marketsense.data.Comment.VIEW_TYPE_COMMENT;
import static com.idroi.marketsense.data.Comment.VIEW_TYPE_REPLY;
import static com.idroi.marketsense.request.CommentAndVoteRequest.COMMENT_CACHE_KEY_GENERAL;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter {

    public static final int ADAPTER_CHANGE_LIKE_ONLY = 1;

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
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public void setCommentsAvailableListener(CommentsAvailableListener listener) {
        mCommentsAvailableListener = listener;
    }

    public void loadCommentsList(String url) {
        loadCommentsList(null, url);
    }

    public void loadCommentsList(String cacheKey, String url) {
        mCommentsPlacer.loadComments(cacheKey, url);
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

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        if(payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            Comment comment = mCommentsPlacer.getCommentData(position);
            int viewType = comment.getViewType();
            if(viewType == VIEW_TYPE_COMMENT) {
                int type = (int) payloads.get(0);
                switch (type) {
                    case ADAPTER_CHANGE_LIKE_ONLY:
                        mCommentsRenderer.updateLikeAndReplyBlock(holder.itemView, comment);
                }
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

    public void removeCommentGeneralCache(Context context) {
        final Context applicationContext = context.getApplicationContext();
        // clear cache url and reload comments since user had inserted a new comment
        if(applicationContext != null) {
            SharedPreferences.Editor editor =
                    context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE).edit();
            editor.remove(COMMENT_CACHE_KEY_GENERAL);
            SharedPreferencesCompat.apply(editor);
            MSLog.d("try to remove COMMENT_CACHE_KEY_GENERAL");
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(applicationContext != null) {
                    MarketSenseCommentsFetcher.prefetchGeneralComments(applicationContext);
                }
            }
        }, 1000);
    }

    public void destroy() {
        mCommentsRenderer.clear();
        mReplyRenderer.clear();
        mCommentsPlacer.clear();
        mCommentsAvailableListener = null;
        mOnItemClickListener = null;
        mOnNewsItemClickListener = null;
    }
}
