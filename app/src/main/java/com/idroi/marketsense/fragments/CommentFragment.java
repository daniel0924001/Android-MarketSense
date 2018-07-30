package com.idroi.marketsense.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;

import com.facebook.login.widget.LoginButton;
import com.idroi.marketsense.CommentActivity;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.NewsWebViewActivity;
import com.idroi.marketsense.R;
import com.idroi.marketsense.RichEditorActivity;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.CommentAndVoteRequest;

import java.io.Serializable;

import static android.app.Activity.RESULT_OK;
import static com.idroi.marketsense.CommentActivity.EXTRA_COMMENT;
import static com.idroi.marketsense.CommentActivity.EXTRA_NEED_TO_CHANGE;
import static com.idroi.marketsense.CommentActivity.EXTRA_POSITION;
import static com.idroi.marketsense.RichEditorActivity.sReplyEditorRequestCode;
import static com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter.ADAPTER_CHANGE_LIKE_ONLY;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_DISCUSSION_COMMENT_CLICK;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FUNCTION_INSERT_COMMENT;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FUNCTION_SEARCH_COMMENT;
import static com.idroi.marketsense.request.CommentAndVoteRequest.COMMENT_CACHE_KEY_GENERAL;

/**
 * Created by daniel.hsieh on 2018/7/5.
 */

public class CommentFragment extends Fragment {

    public static final int LAST_CLICK_IS_LIKE = 1;

    private RecyclerView mCommentRecyclerView;
    private CommentsRecyclerViewAdapter mCommentRecyclerViewAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mLoadingProgressBar;
    private ConstraintLayout mNoDataRefreshLayout;

    private int mLastClickAction;

    private AlertDialog mLoginAlertDialog;
    private LoginButton mFBLoginBtn;
    private UserProfile.GlobalBroadcastListener mGlobalBroadcastListener;

    private Comment mTempComment;
    private int mTempPosition;

    private TextView mNoDataTextView;
    private String mTempSearchString;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.discussion_fragment, container, false);
        mCommentRecyclerView = view.findViewById(R.id.comment_recycler_view);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_to_refresh);
        mNoDataRefreshLayout = view.findViewById(R.id.no_data_block);
        mLoadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        mNoDataTextView = view.findViewById(R.id.no_comment_tv);
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
                            mCommentRecyclerViewAdapter.notifyItemChanged(position, ADAPTER_CHANGE_LIKE_ONLY);
                            mCommentRecyclerViewAdapter.removeCommentGeneralCache(getActivity());
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
                } else {
                    showCommentBlock(false);
                }
            }
        });
        mNoDataRefreshLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryCommentsEvent();
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryCommentsEvent(false);
            }
        });
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final UserProfile userProfile = ClientData.getInstance(getActivity()).getUserProfile();
        mGlobalBroadcastListener = new UserProfile.GlobalBroadcastListener() {
            @Override
            public void onGlobalBroadcast(int notifyId, Object payload) {
                if(notifyId == NOTIFY_ID_DISCUSSION_COMMENT_CLICK &&
                        FBHelper.checkFBLogin() &&
                        getActivity() != null) {
                    if(mLastClickAction == LAST_CLICK_IS_LIKE) {
                        mCommentRecyclerViewAdapter.updateCommentsLike();
                        if(mTempComment != null) {
                            MSLog.d("is like: " + mTempComment.isLiked());
                            if(!mTempComment.isLiked()) {
                                MSLog.d("say like at position: " + mTempPosition);
                                mTempComment.increaseLike();
                                mTempComment.setLike(true);
                                PostEvent.sendLike(getActivity(), mTempComment.getCommentId());
                                mCommentRecyclerViewAdapter.notifyItemChanged(mTempPosition, ADAPTER_CHANGE_LIKE_ONLY);
                                mCommentRecyclerViewAdapter.removeCommentGeneralCache(getActivity());
                                mTempComment = null;
                            }
                        }
                    }
                } else if(notifyId == NOTIFY_ID_FAVORITE_LIST) {
                    if(mTempComment != null) {
                        userProfile.globalBroadcast(NOTIFY_ID_DISCUSSION_COMMENT_CLICK);
                    } else {
                        mCommentRecyclerViewAdapter.updateCommentsLike();
                    }
                } else if(notifyId == NOTIFY_ID_FUNCTION_SEARCH_COMMENT) {
                    if(payload != null && payload instanceof String) {
                        queryCommentsEventForStockCode((String) payload);
                    }
                } else if(notifyId == NOTIFY_ID_FUNCTION_INSERT_COMMENT) {
                    mCommentRecyclerViewAdapter.removeCommentGeneralCache(getActivity());
                    Comment newComment = (Comment) payload;
                    if(mTempSearchString != null) {
                        // in the code query
                        queryCommentsEvent();
                    } else {
                        // in the general query
                        mCommentRecyclerViewAdapter.addOneComment(newComment);
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                // we have to wait for notifyItemInserted(0)
                                mCommentRecyclerView.scrollToPosition(0);
                            }
                        });
                        showCommentBlock(true);

                        MSLog.d(String.format("user send a comment on (%s, %s): %s",
                                RichEditorActivity.TYPE.NO_CONTENT.getType(),
                                newComment.getCommentId(), newComment.getCommentHtml()));
                    }
                }
            }
        };
        userProfile.addGlobalBroadcastListener(mGlobalBroadcastListener);

        queryCommentsEvent();
        mCommentRecyclerView.setAdapter(mCommentRecyclerViewAdapter);
    }

    private void queryCommentsEvent() {
        queryCommentsEvent(true);
    }

    private void queryCommentsEvent(boolean refreshUi) {
        if(refreshUi) {
            startToLoadComment();
        }
        mTempSearchString = null;
        mCommentRecyclerViewAdapter.loadCommentsList(COMMENT_CACHE_KEY_GENERAL, CommentAndVoteRequest.queryCommentsEvent());
    }

    private void queryCommentsEventForStockCode(String code) {
        startToLoadComment();
        mTempSearchString = code;
        mCommentRecyclerViewAdapter.loadCommentsList(CommentAndVoteRequest.queryCommentsEventForStockCode(mTempSearchString));
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
            if(getActivity() != null) {
                if (mTempSearchString != null) {
                    String name = ClientData.getInstance(getActivity()).getNameFromCode(mTempSearchString);
                    String noDataDescription = null;
                    if(name != null) {
                        noDataDescription = String.format(getActivity().getResources().getString(R.string.ops_name_or_code_not_found_for_comment), name);
                    } else {
                        noDataDescription = String.format(getActivity().getResources().getString(R.string.ops_name_or_code_not_found_for_comment), mTempSearchString);
                    }
                    mNoDataTextView.setText(noDataDescription);
                } else {
                    mNoDataTextView.setText(getActivity().getResources().getString(R.string.ops_something_wrong));
                }
            }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == sReplyEditorRequestCode) {
            if(resultCode == RESULT_OK && data.getBooleanExtra(EXTRA_NEED_TO_CHANGE, false)) {
                Serializable serializable = data.getSerializableExtra(EXTRA_COMMENT);
                int position = data.getIntExtra(EXTRA_POSITION, -1);
                if (serializable != null && serializable instanceof Comment && position != -1) {
                    MSLog.d("comment with position " + position + " is needed to change");
                    Comment comment = (Comment) serializable;
                    mCommentRecyclerViewAdapter.cloneSocialContent(position, comment);
                    mCommentRecyclerViewAdapter.notifyItemChanged(position, ADAPTER_CHANGE_LIKE_ONLY);
                    mCommentRecyclerViewAdapter.removeCommentGeneralCache(getActivity());
                }
            }
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
        userProfile.deleteGlobalBroadcastListener(mGlobalBroadcastListener);
        super.onDestroyView();
    }
}
