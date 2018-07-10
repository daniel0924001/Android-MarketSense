package com.idroi.marketsense.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.facebook.login.widget.LoginButton;
import com.idroi.marketsense.CommentActivity;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.NewsWebViewActivity;
import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.CommentAndVoteRequest;

import static com.idroi.marketsense.RichEditorActivity.sReplyEditorRequestCode;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_STOCK_COMMENT_CLICK;

/**
 * Created by daniel.hsieh on 2018/7/5.
 */

public class CommentFragment extends Fragment {

    public static final int LAST_CLICK_IS_COMMENT = 1;
    public static final int LAST_CLICK_IS_LIKE = 2;

    private RecyclerView mCommentRecyclerView;
    private CommentsRecyclerViewAdapter mCommentRecyclerViewAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mLoadingProgressBar;
    private ConstraintLayout mNoDataRefreshLayout;

    private int mLastClickAction;

    private AlertDialog mLoginAlertDialog;
    private LoginButton mFBLoginBtn;
    private UserProfile.UserProfileChangeListener mUserProfileChangeListener;

    private Comment mTempComment;
    private int mTempPosition;

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
        mCommentRecyclerViewAdapter = new CommentsRecyclerViewAdapter(getActivity(), true,
                new CommentsRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onSayLikeItemClick(Comment comment, int position) {
                        if (FBHelper.checkFBLogin()) {
                            MSLog.d("say like at position: " + position);
                            comment.increaseLike();
                            comment.setLike(true);
                            PostEvent.sendLike(getActivity(), comment.getCommentId());
                            mCommentRecyclerViewAdapter.notifyItemChanged(position);
                        } else {
                            mTempComment = comment;
                            mTempPosition = position;
                            showLoginAlertDialog(LAST_CLICK_IS_LIKE);
                        }
                    }

                    @Override
                    public void onReplyItemClick(Comment comment, int position) {
                        startActivityForResult(CommentActivity.generateCommentActivityIntent(
                                getActivity(), comment, position), sReplyEditorRequestCode);
                        getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
                    }
                }, new CommentsRecyclerViewAdapter.OnNewsItemClickListener() {
                @Override
                public void onNewsItemClick(News news) {
                    startActivity(NewsWebViewActivity.generateNewsWebViewActivityIntent(
                            getActivity(), news));
                    getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
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

        UserProfile userProfile = ClientData.getInstance(getActivity()).getUserProfile();
        mUserProfileChangeListener = new UserProfile.UserProfileChangeListener() {
            @Override
            public void onUserProfileChange(int notifyId) {
                if(notifyId == NOTIFY_ID_STOCK_COMMENT_CLICK &&
                        FBHelper.checkFBLogin() &&
                        getActivity() != null) {
                    if(mLastClickAction == LAST_CLICK_IS_LIKE) {
                        mCommentRecyclerViewAdapter.updateCommentsLike();
                        MSLog.d("is like: " + mTempComment.isLiked());
                        if(mTempComment.isLiked()) {
                            MSLog.d("say like at position: " + mTempPosition);
                            mTempComment.increaseLike();
                            mTempComment.setLike(true);
                            PostEvent.sendLike(getActivity(), mTempComment.getCommentId());
                            mCommentRecyclerViewAdapter.notifyItemChanged(mTempPosition);
                        }
                    }
                } else if(notifyId == NOTIFY_ID_FAVORITE_LIST) {
                    mCommentRecyclerViewAdapter.updateCommentsLike();
                }
            }
        };
        userProfile.addUserProfileChangeListener(mUserProfileChangeListener);

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

    private void showLoginAlertDialog(int lastAction) {
        mLastClickAction = lastAction;
        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }

        if(getActivity() != null) {
            final View alertView = LayoutInflater.from(getActivity())
                    .inflate(R.layout.alertdialog_login, null);
            mLoginAlertDialog = new AlertDialog.Builder(getActivity())
                    .setView(alertView).create();
            mLoginAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    mFBLoginBtn = alertView.findViewById(R.id.login_button);
                    mFBLoginBtn.setReadPermissions("email");
                    mFBLoginBtn.setReadPermissions("public_profile");
                }
            });
            mLoginAlertDialog.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }
    }

    @Override
    public void onDestroyView() {
        UserProfile userProfile = ClientData.getInstance(getActivity()).getUserProfile();
        userProfile.deleteUserProfileChangeListener(mUserProfileChangeListener);
        super.onDestroyView();
    }
}
