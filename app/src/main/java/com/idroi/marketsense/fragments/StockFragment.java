package com.idroi.marketsense.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.RichEditorActivity;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.CommentAndVoteRequest;

import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_HTML;
import static com.idroi.marketsense.RichEditorActivity.sEditorRequestCode;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_STOCK_COMMENT_CLICK;

/**
 * Created by daniel.hsieh on 2018/5/6.
 */

@Deprecated
public class StockFragment extends Fragment {

    public final static String STOCK_CODE = "STOCK_CODE";
    public final static String STOCK_NAME = "STOCK_NAME";
    public final static String RAISE_BUNDLE = "RAISE_BUNDLE";
    public final static String FALL_BUNDLE = "FALL_BUNDLE";
    private final static String STOCK_REAL_TIME_URL_PREFIX = "https://so.cnyes.com/JavascriptGraphic/chartstudy.aspx?country=tw&market=twreal&divwidth=%d&divheight=%d&code=%s";

    private String mStockId;
    private String mStockName;

    private RecyclerView mCommentRecyclerView;
    private CommentsRecyclerViewAdapter mCommentsRecyclerViewAdapter;

    private Button mButtonRaise, mButtonFall, mButtonComment, mButtonSendFirst;
    private int mVoteRaiseNum, mVoteFallNum;
    private String mVoteRaisePercentageString, mVoteFallPercentageString;
    private boolean mLastClickedButtonIsComment;

    private static final float CONST_ENABLE_ALPHA = 1.0f;
    private static final float CONST_DISABLE_ALPHA = 0.7f;

    private AlertDialog mLoginAlertDialog;
    private LoginButton mFBLoginBtn;
    private UserProfile.UserProfileChangeListener mUserProfileChangeListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.stock_fragment, container, false);

        setInformation();
//        initRealTimeWebView(view);
        initButton(view);
        initComments(view);

        return view;
    }

    private void initComments(final View view) {
        TextView actionTitle = view.findViewById(R.id.action_title).findViewById(R.id.marketsense_block_title_tv);
        TextView commentTitle = view.findViewById(R.id.comment_title).findViewById(R.id.marketsense_block_title_tv);
        actionTitle.setText(getResources().getString(R.string.title_action_seven));
        commentTitle.setText(getResources().getString(R.string.title_comment));
        mCommentRecyclerView = view.findViewById(R.id.marketsense_stock_comment_rv);

        mCommentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter(getActivity());
        mCommentRecyclerView.setAdapter(mCommentsRecyclerViewAdapter);

        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCommentsRecyclerViewAdapter.setCommentsAvailableListener(new CommentsRecyclerViewAdapter.CommentsAvailableListener() {
            @Override
            public void onCommentsAvailable(CommentAndVote commentAndVote) {
                if(commentAndVote != null) {
                    if (commentAndVote.getCommentSize() > 0) {
                        showCommentBlock(view);
                    }
                    mVoteRaiseNum = commentAndVote.getRaiseNumber();
                    mVoteFallNum = commentAndVote.getFallNumber();
                    setButtonStatus();
                    MSLog.d("raise number: " + commentAndVote.getRaiseNumber());
                    MSLog.d("fall number: " + commentAndVote.getFallNumber());
                }
            }
        });
        mCommentsRecyclerViewAdapter.loadCommentsList(CommentAndVoteRequest.querySingleNewsUrl(mStockId, CommentAndVoteRequest.TASK.STOCK_COMMENT));
    }

    private void showCommentBlock(View view) {
        if(view != null) {
            view.findViewById(R.id.marketsense_stock_no_comment_iv).setVisibility(View.GONE);
            view.findViewById(R.id.marketsense_stock_no_comment_tv).setVisibility(View.GONE);
            view.findViewById(R.id.btn_send_first).setVisibility(View.GONE);
            mCommentRecyclerView.setVisibility(View.VISIBLE);
        } else {
            MSLog.e("StockFragment view is null?");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == sEditorRequestCode) {
            if(resultCode == RESULT_OK) {
                String html = data.getStringExtra(EXTRA_RES_HTML);
                Comment comment = new Comment();
                comment.setCommentHtml(html);
                mCommentsRecyclerViewAdapter.addOneComment(comment);
                showCommentBlock(getView());
                MSLog.d("user send a comment on code: " + mStockId);
                MSLog.d("user send a comment of html: " + html);
            }
        }
    }

    private void setInformation() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            mStockId = bundle.getString(STOCK_CODE);
            mStockName = bundle.getString(STOCK_NAME);
            mVoteRaiseNum = bundle.getInt(RAISE_BUNDLE, 0);
            mVoteFallNum = bundle.getInt(FALL_BUNDLE, 0);
        }
    }

    private String getStockPriceURL(String code) {
        int width = ClientData.getInstance().getScreenWidth();
        int height = (int)((float)(width * 2)/3);
        return String.format(Locale.US, STOCK_REAL_TIME_URL_PREFIX, width, height, code);
    }

    private void initButton(View view) {
        mButtonRaise = view.findViewById(R.id.btn_say_good);
        mButtonFall = view.findViewById(R.id.btn_say_bad);
        mButtonComment = view.findViewById(R.id.btn_say_comment);
        mButtonSendFirst = view.findViewById(R.id.btn_send_first);

        mLastClickedButtonIsComment = false;
        setButtonStatus();

        mButtonRaise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLastClickedButtonIsComment = false;
                if(FBHelper.checkFBLogin()) {
                    MSLog.e("click good in company: " + mStockId);
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.title_welcome_tomorrow, Toast.LENGTH_SHORT).show();
                    }
                    PostEvent.sendStockVote(getContext(), mStockId, PostEvent.EventVars.VOTE_RAISE, 1);
                    mVoteRaiseNum += 1;
                    setButtonStatus();
                } else {
                    showLoginAlertDialog();
                }
            }
        });

        mButtonFall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLastClickedButtonIsComment = false;
                if(FBHelper.checkFBLogin()) {
                    MSLog.e("click bad in company: " + mStockId);
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.title_welcome_tomorrow, Toast.LENGTH_SHORT).show();
                    }
                    PostEvent.sendStockVote(getContext(), mStockId, PostEvent.EventVars.VOTE_FALL, 1);
                    mVoteFallNum += 1;
                    setButtonStatus();
                } else {
                    showLoginAlertDialog();
                }
            }
        });

        mButtonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLastClickedButtonIsComment = true;
                if(FBHelper.checkFBLogin()) {
                    startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                            getActivity(), RichEditorActivity.TYPE.STOCK, mStockId),
                            sEditorRequestCode);
                    getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
                } else {
                    showLoginAlertDialog();
                }
            }
        });

        mButtonSendFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLastClickedButtonIsComment = true;
                if(FBHelper.checkFBLogin()) {
                    startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                            getActivity(), RichEditorActivity.TYPE.STOCK, mStockId),
                            sEditorRequestCode);
                    getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
                } else {
                    showLoginAlertDialog();
                }
            }
        });

        UserProfile userProfile = ClientData.getInstance(getActivity()).getUserProfile();
        mUserProfileChangeListener = new UserProfile.UserProfileChangeListener() {
            @Override
            public void onUserProfileChange(int notifyId) {
                if(notifyId == NOTIFY_ID_STOCK_COMMENT_CLICK && mLastClickedButtonIsComment) {
                    if(FBHelper.checkFBLogin() && getActivity() != null) {
                        startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                                getActivity(), RichEditorActivity.TYPE.STOCK, mStockId),
                                sEditorRequestCode);
                        getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
                    }
                }
            }
        };
        userProfile.addUserProfileChangeListener(mUserProfileChangeListener);
    }

    private void setButtonStatus() {
        if(!ClientData.getInstance(getActivity()).getUserProfile().canVoteAgain(mStockId)) {
            mButtonRaise.setEnabled(false);
            mButtonFall.setEnabled(false);

            updateVotePercentageString();
            mButtonRaise.setText(mVoteRaisePercentageString);
            mButtonFall.setText(mVoteFallPercentageString);

            mButtonRaise.setAlpha(CONST_DISABLE_ALPHA);
            mButtonFall.setAlpha(CONST_DISABLE_ALPHA);
        } else {
            mButtonRaise.setEnabled(true);
            mButtonFall.setEnabled(true);

            mButtonRaise.setText(R.string.title_vote);
            mButtonFall.setText(R.string.title_vote);

            mButtonRaise.setAlpha(CONST_ENABLE_ALPHA);
            mButtonFall.setAlpha(CONST_ENABLE_ALPHA);
        }
    }

    private void updateVotePercentageString() {
        int total = mVoteFallNum + mVoteRaiseNum;
        mVoteRaisePercentageString =
                String.format(Locale.US, "%d%%", (int)(((float) mVoteRaiseNum/total)*100));
        mVoteFallPercentageString =
                String.format(Locale.US, "%d%%", (int)(((float) mVoteFallNum/total)*100));
    }


    @Override
    public void onDestroy() {
        UserProfile userProfile = ClientData.getInstance(getActivity()).getUserProfile();
        userProfile.deleteUserProfileChangeListener(mUserProfileChangeListener);
        super.onDestroy();
    }

    private void showLoginAlertDialog() {
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
}
