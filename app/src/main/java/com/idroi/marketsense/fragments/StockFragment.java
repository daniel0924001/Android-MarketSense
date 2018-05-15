package com.idroi.marketsense.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.ViewSkeletonScreen;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.NewsWebView;
import com.idroi.marketsense.R;
import com.idroi.marketsense.RichEditorActivity;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.SingleNewsRequest;
import com.idroi.marketsense.util.DateUtils;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_HTML;
import static com.idroi.marketsense.RichEditorActivity.sEditorRequestCode;

/**
 * Created by daniel.hsieh on 2018/5/6.
 */

public class StockFragment extends Fragment {

    public final static String STOCK_CODE = "STOCK_CODE";
    public final static String RAISE_BUNDLE = "RAISE_BUNDLE";
    public final static String FALL_BUNDLE = "FALL_BUNDLE";
    private final static String STOCK_REAL_TIME_URL_PREFIX = "https://so.cnyes.com/JavascriptGraphic/chartstudy.aspx?country=tw&market=twreal&divwidth=%d&divheight=%d&code=%s";

    private NewsWebView mStockPriceRealTimeWebView;
    private ViewSkeletonScreen mSkeletonScreen;
    private String mStockId;

    private RecyclerView mCommentRecyclerView;
    private CommentsRecyclerViewAdapter mCommentsRecyclerViewAdapter;

    private Button mButtonRaise, mButtonFall;
    private int mVoteRaiseNum, mVoteFallNum;
    private String mVoteRaisePercentageString, mVoteFallPercentageString;

    private static final float CONST_ENABLE_ALPHA = 1.0f;
    private static final float CONST_DISABLE_ALPHA = 0.7f;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.stock_fragment, container, false);

        setInformation();
        initRealTimeWebView(view);
        initButton(view);
        initComments(view);

        mSkeletonScreen = Skeleton.bind(mStockPriceRealTimeWebView)
                .shimmer(false)
                .load(R.layout.skeleton_webview)
                .show();

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
                if(commentAndVote.getCommentSize() > 0) {
                    showCommentBlock(view);
                }
                mVoteRaiseNum = commentAndVote.getRaiseNumber();
                mVoteFallNum = commentAndVote.getFallNumber();
                setButtonStatus();
                MSLog.d("raise number: " + commentAndVote.getRaiseNumber());
                MSLog.d("fall number: " + commentAndVote.getFallNumber());
            }
        });
        mCommentsRecyclerViewAdapter.loadCommentsList(SingleNewsRequest.querySingleNewsUrl(mStockId, SingleNewsRequest.TASK.STOCK_COMMENT));
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
            mVoteRaiseNum = bundle.getInt(RAISE_BUNDLE, 0);
            mVoteFallNum = bundle.getInt(FALL_BUNDLE, 0);
        }
    }

    private void initRealTimeWebView(View view) {
        mStockPriceRealTimeWebView = view.findViewById(R.id.stock_real_time_webview);
        mStockPriceRealTimeWebView.setVerticalScrollBarEnabled(true);
        mStockPriceRealTimeWebView.setHorizontalFadingEdgeEnabled(true);

        mStockPriceRealTimeWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mSkeletonScreen.hide();
                super.onPageFinished(view, url);
            }
        });

        String url =
                getStockPriceURL(mStockId);
        MSLog.i("Load the real time price of " + mStockId + ": " + url);
        mStockPriceRealTimeWebView.getSettings().setLoadWithOverviewMode(false);
        mStockPriceRealTimeWebView.getSettings().setUseWideViewPort(false);

        mStockPriceRealTimeWebView.loadUrl(url);
    }

    private String getStockPriceURL(String code) {
        int width = ClientData.getInstance().getScreenWidth();
        int height = (int)((float)(width * 2)/3);
        return String.format(Locale.US, STOCK_REAL_TIME_URL_PREFIX, width, height, code);
    }

    private void initButton(View view) {
        mButtonRaise = view.findViewById(R.id.btn_say_good);
        mButtonFall = view.findViewById(R.id.btn_say_bad);
        final Button buttonComment = view.findViewById(R.id.btn_say_comment);
        final Button buttonSendFirst = view.findViewById(R.id.btn_send_first);

        setButtonStatus();

        mButtonRaise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MSLog.e("click good in company: " + mStockId);
                if(getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.title_welcome_tomorrow, Toast.LENGTH_SHORT).show();
                }
                PostEvent.sendStockVote(getContext(), mStockId, PostEvent.EventVars.VOTE_RAISE, 1);
                mVoteRaiseNum += 1;
                setButtonStatus();
            }
        });

        mButtonFall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MSLog.e("click bad in company: " + mStockId);
                if(getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.title_welcome_tomorrow, Toast.LENGTH_SHORT).show();
                }
                PostEvent.sendStockVote(getContext(), mStockId, PostEvent.EventVars.VOTE_FALL, 1);
                mVoteFallNum += 1;
                setButtonStatus();
            }
        });

        buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                        getActivity(), RichEditorActivity.TYPE.STOCK, mStockId),
                        sEditorRequestCode);
                getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });

        buttonSendFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                        getActivity(), RichEditorActivity.TYPE.STOCK, mStockId),
                        sEditorRequestCode);
                getActivity().overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
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
        if(mStockPriceRealTimeWebView != null) {
            mStockPriceRealTimeWebView.destroy();
            mStockPriceRealTimeWebView = null;
        }
        super.onDestroy();
    }
}
