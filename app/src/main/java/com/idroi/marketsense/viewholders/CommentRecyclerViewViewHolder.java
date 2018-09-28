package com.idroi.marketsense.viewholders;

import android.app.Activity;
import android.support.constraint.Group;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.request.CommentAndVoteRequest;

/**
 * Created by daniel.hsieh on 2018/9/28.
 */

public class CommentRecyclerViewViewHolder {

    public TextView titleTextView;
    public CommentsRecyclerViewAdapter mCommentsRecyclerViewAdapter;
    public RecyclerView mCommentRecyclerView;
    public Group noCommentGroup;

    static final CommentRecyclerViewViewHolder EMPTY_VIEW_HOLDER = new CommentRecyclerViewViewHolder();

    private CommentRecyclerViewViewHolder() {}

    public static CommentRecyclerViewViewHolder convertToViewHolder(final View view) {
        final CommentRecyclerViewViewHolder viewHolder = new CommentRecyclerViewViewHolder();
        try {
            viewHolder.titleTextView = view.findViewById(R.id.comment_title);
            viewHolder.mCommentRecyclerView = view.findViewById(R.id.marketsense_webview_comment_rv);
            viewHolder.noCommentGroup = view.findViewById(R.id.no_comment_group);

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void init(final Activity activity,
                     String id,
                     final CommentsRecyclerViewAdapter.OnItemClickListener itemClickListener,
                     final CommentsRecyclerViewAdapter.CommentsAvailableListener availableListener) {
        mCommentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter(activity, itemClickListener);
        mCommentRecyclerView.setAdapter(mCommentsRecyclerViewAdapter);

        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mCommentsRecyclerViewAdapter.setCommentsAvailableListener(availableListener);
        mCommentsRecyclerViewAdapter.loadCommentsList(
                CommentAndVoteRequest.querySingleNewsUrl(id, CommentAndVoteRequest.TASK.NEWS_COMMENT));
    }

    public void showCommentBlock(Activity activity) {
        noCommentGroup.setVisibility(View.GONE);
        mCommentRecyclerView.setVisibility(View.VISIBLE);

        int size = mCommentsRecyclerViewAdapter.getItemCount();
        titleTextView.setText(String.format(activity.getString(R.string.title_comment_format), size));
    }

    public void destroy() {
        if(mCommentsRecyclerViewAdapter != null) {
            mCommentsRecyclerViewAdapter.destroy();
        }
    }
}
