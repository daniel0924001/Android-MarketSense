package com.idroi.marketsense.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.request.CommentAndVoteRequest;

/**
 * Created by daniel.hsieh on 2018/7/5.
 */

public class CommentFragment extends Fragment {

    private RecyclerView mCommentRecyclerView;
    private CommentsRecyclerViewAdapter mCommentRecyclerViewAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mLoadingProgressBar;
    private ConstraintLayout mNoDataRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.discussion_fragment, container, false);
        mCommentRecyclerView = view.findViewById(R.id.comment_recycler_view);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_to_refresh);
        mNoDataRefreshLayout = view.findViewById(R.id.no_data_block);
        mLoadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCommentRecyclerViewAdapter = new CommentsRecyclerViewAdapter(getActivity(), new CommentsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onSayLikeItemClick(Comment comment, int position) {

            }

            @Override
            public void onReplyItemClick(Comment comment, int position) {

            }
        });
        mCommentRecyclerViewAdapter.setCommentsAvailableListener(new CommentsRecyclerViewAdapter.CommentsAvailableListener() {
            @Override
            public void onCommentsAvailable(CommentAndVote commentAndVote) {
                if(commentAndVote != null) {
                    if (commentAndVote.getCommentSize() > 0) {
                        showCommentBlock(true);
                    } else {
                        showCommentBlock(false);
                    }
                }
            }
        });
        mNoDataRefreshLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCommentRecyclerViewAdapter.loadCommentsList(CommentAndVoteRequest.queryCommentsEvent());
                startToLoadComment();
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCommentRecyclerViewAdapter.loadCommentsList(CommentAndVoteRequest.queryCommentsEvent());
            }
        });
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCommentRecyclerViewAdapter.loadCommentsList(CommentAndVoteRequest.queryCommentsEvent());
        mCommentRecyclerView.setAdapter(mCommentRecyclerViewAdapter);
        startToLoadComment();
    }

    private void startToLoadComment() {
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mNoDataRefreshLayout.setVisibility(View.GONE);
        mCommentRecyclerView.setVisibility(View.GONE);
    }

    private void showCommentBlock(boolean hasData) {
        mLoadingProgressBar.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
        if(hasData) {
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            mNoDataRefreshLayout.setVisibility(View.GONE);
            mCommentRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mSwipeRefreshLayout.setVisibility(View.GONE);
            mNoDataRefreshLayout.setVisibility(View.VISIBLE);
            mCommentRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
