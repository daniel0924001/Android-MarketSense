package com.idroi.marketsense;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.ViewSkeletonScreen;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.common.FrescoImageHelper;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.data.Event;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.CommentAndVoteRequest;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import static com.idroi.marketsense.CommentActivity.EXTRA_COMMENT;
import static com.idroi.marketsense.CommentActivity.EXTRA_NEED_TO_CHANGE;
import static com.idroi.marketsense.CommentActivity.EXTRA_POSITION;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_EVENT_ID;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_HTML;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_ID;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_TYPE;
import static com.idroi.marketsense.RichEditorActivity.sEditorRequestCode;
import static com.idroi.marketsense.RichEditorActivity.sReplyEditorRequestCode;
import static com.idroi.marketsense.common.Constants.FACEBOOK_CONSTANTS;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_NEWS_COMMENT_CLICK;

/**
 * Created by daniel.hsieh on 2018/4/25.
 */

public class NewsWebViewActivity extends AppCompatActivity {

    private static final String UTM_PARAM_STRING = "utm_source=infohub&utm_medium=android_app&utm_campaign=news_click";
    private static final String PAGELINK_PARM_STRING = "user_name=infohub|android_app|news_click";
    private static final String PAGELINK_PARM_KEY = "config=";

    public static final String EXTRA_MIDDLE_ID = "EXTRA_MIDDLE_ID";
    public static final String EXTRA_MIDDLE_TITLE = "EXTRA_MIDDLE_TITLE";
    public static final String EXTRA_MIDDLE_DATE = "EXTRA_MIDDLE_SOURCE_DATE";
    public static final String EXTRA_MIDDLE_IMAGE_URL = "EXTRA_MIDDLE_IMAGE_URL";
    public static final String EXTRA_MIDDLE_PAGE_URL = "EXTRA_MIDDLE_PAGE_URL";
    public static final String EXTRA_ORIGINAL_PAGE_URL = "EXTRA_ORIGINAL_PAGE_URL";
    public static final String EXTRA_VOTE_RAISE_NUM = "EXTRA_VOTE_RAISE_NUM";
    public static final String EXTRA_VOTE_FALL_NUM = "EXTRA_VOTE_FALL_NUM";
    public static final String EXTRA_STOCK_KEYWORDS = "EXTRA_STOCK_KEYWORDS";

    private static final float CONST_ENABLE_ALPHA = 0.4f;
    private static final float CONST_DISABLE_ALPHA = 1.0f;

    private static final int LAST_CLICK_IS_COMMENT = 1;
    private static final int LAST_CLICK_IS_LIKE = 2;
    private static final int LAST_CLICK_IS_VOTE_UP = 3;
    private static final int LAST_CLICK_IS_VOTE_DOWN = 4;

    private String mId, mTitle, mImageUrl, mSourceDate;
    private String mMiddlePageUrl;
    private String mOriginalPageUrl;
    private String mPageLink;
    private int mVoteRaiseNum, mVoteFallNum;
    private String[] mStockKeywords;

    private View mImageMask;
    private ScrollView mUpperBlock;
    private NewsWebView mNewsWebViewOriginal;
    private NewsWebView mNewsWebViewMiddle;
    private TextView mNewsWebViewMiddleTitleTextView;
    private TextView mNewsWebViewMiddleDateTextView;
    private SimpleDraweeView mNewsWebViewMiddleImageView;
    private CommentsRecyclerViewAdapter mCommentsRecyclerViewAdapter;
    private RecyclerView mCommentRecyclerView;
    private ProgressBar mLoadingProgressBar, mLoadingProgressBarOriginal;

    private ConstraintLayout mVoteUpBlock, mVoteDownBlock;
    private TextView mVoteUpTextView, mVoteDownTextView;
    private ImageView mVoteUpImageView, mVoteDownImageView;

    private AlertDialog mLoginAlertDialog;
    private LoginButton mFBLoginBtn;
    private UserProfile.UserProfileChangeListener mUserProfileChangeListener;
    private CallbackManager mFBCallbackManager;

    private int mLastClickAction;
    private Comment mTempComment;
    private int mTempPosition;

    public static final int sPostDelayMilliSeconds = 2000;
    private boolean mIsOriginalVisible = false;
    private boolean mIsOriginalCompleted = false;
    private boolean mTryToLoadOtherWebViewFlag = false;
    private Handler mHandler = new Handler();
    private Runnable mLoadOriginalWebViewRunnable = new Runnable() {
        @Override
        public void run() {
            if(!mTryToLoadOtherWebViewFlag) {
                mTryToLoadOtherWebViewFlag = true;
                if (mNewsWebViewOriginal != null) {
                    MSLog.i("Loading web page (original): " + mOriginalPageUrl);
                    mNewsWebViewOriginal.loadUrl(mOriginalPageUrl);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrescoHelper.initialize(getApplicationContext());
        setContentView(R.layout.activity_news_webview);

        initFBLogin();
        setInformation();
        setActionBar();
        initUpperBlock();
        initWebView();

        initComments();
        initBtn();
    }

    @Override
    public void onBackPressed() {
        if(mIsOriginalVisible) {
            changeWebViewVisibility();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.stop, R.anim.right_to_left);
        }
    }

    private void setInformation() {
        mId = getIntent().getStringExtra(EXTRA_MIDDLE_ID);
        mMiddlePageUrl = getIntent().getStringExtra(EXTRA_MIDDLE_PAGE_URL);
        mOriginalPageUrl = getIntent().getStringExtra(EXTRA_ORIGINAL_PAGE_URL);
        mTitle = getIntent().getStringExtra(EXTRA_MIDDLE_TITLE);
        mImageUrl = getIntent().getStringExtra(EXTRA_MIDDLE_IMAGE_URL);
        mSourceDate = getIntent().getStringExtra(EXTRA_MIDDLE_DATE);
        mStockKeywords = getIntent().getStringArrayExtra(EXTRA_STOCK_KEYWORDS);

        mVoteRaiseNum = getIntent().getIntExtra(EXTRA_VOTE_RAISE_NUM, 0);
        mVoteFallNum = getIntent().getIntExtra(EXTRA_VOTE_FALL_NUM, 0);
    }

    private void initComments() {
        mCommentRecyclerView = findViewById(R.id.marketsense_webview_comment_rv);

        mCommentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter(this, new CommentsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onSayLikeItemClick(Comment comment, int position) {
                if(FBHelper.checkFBLogin()) {
                    MSLog.d("say like at position: " + position);
                    comment.increaseLike();
                    comment.setLike(true);
                    PostEvent.sendLike(NewsWebViewActivity.this, comment.getCommentId());
                    mCommentsRecyclerViewAdapter.notifyItemChanged(position);
                } else {
                    mTempComment = comment;
                    mTempPosition = position;
                    showLoginAlertDialog(LAST_CLICK_IS_LIKE);
                }
            }

            @Override
            public void onReplyItemClick(Comment comment, int position) {
                MSLog.d("reply at position: " + position);
                startActivityForResult(CommentActivity.generateCommentActivityIntent(
                        NewsWebViewActivity.this, comment, position), sReplyEditorRequestCode);
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
        mCommentRecyclerView.setAdapter(mCommentsRecyclerViewAdapter);

        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentsRecyclerViewAdapter.setCommentsAvailableListener(new CommentsRecyclerViewAdapter.CommentsAvailableListener() {
            @Override
            public void onCommentsAvailable(CommentAndVote commentAndVote) {
                if(commentAndVote != null) {
                    if (commentAndVote.getCommentSize() > 0) {
                        showCommentBlock();
                    }
                    mVoteRaiseNum = commentAndVote.getRaiseNumber();
                    mVoteFallNum = commentAndVote.getFallNumber();
                    setButtonStatus();
                    MSLog.d("raise number: " + commentAndVote.getRaiseNumber());
                    MSLog.d("fall number: " + commentAndVote.getFallNumber());
                }
            }
        });
        mCommentsRecyclerViewAdapter.loadCommentsList(CommentAndVoteRequest.querySingleNewsUrl(mId, CommentAndVoteRequest.TASK.NEWS_COMMENT));
    }

    private void showCommentBlock() {
        findViewById(R.id.marketsense_webview_no_comment_iv).setVisibility(View.GONE);
        findViewById(R.id.marketsense_webview_no_comment_tv).setVisibility(View.GONE);
        mCommentRecyclerView.setVisibility(View.VISIBLE);

        TextView commentTitle = findViewById(R.id.comment_title);
        int size = mCommentsRecyclerViewAdapter.getItemCount();
        commentTitle.setText(String.format(getString(R.string.title_comment_format), size));
    }

    private void initBtn() {

        TextView readMoreTextView = findViewById(R.id.tv_read_more);
        readMoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeWebViewVisibility();
            }
        });

        TextView writeCommentTextView = findViewById(R.id.social_write_comment);
        writeCommentTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FBHelper.checkFBLogin()) {
                    startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                            NewsWebViewActivity.this, RichEditorActivity.TYPE.NEWS, mId, mStockKeywords),
                            sEditorRequestCode);
                    overridePendingTransition(R.anim.enter, R.anim.stop);
                } else {
                    showLoginAlertDialog(LAST_CLICK_IS_COMMENT);
                }
            }
        });

        mVoteUpBlock = findViewById(R.id.social_vote_up);
        mVoteDownBlock = findViewById(R.id.social_vote_down);
        mVoteUpTextView = findViewById(R.id.social_vote_up_tv);
        mVoteDownTextView = findViewById(R.id.social_vote_down_tv);
        mVoteUpImageView = findViewById(R.id.social_vote_up_iv);
        mVoteDownImageView = findViewById(R.id.social_vote_down_iv);
        mVoteUpBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FBHelper.checkFBLogin()) {
                    MSLog.d("click good in news: " + mId);
                    PostEvent.sendNewsVote(getBaseContext(), mId, PostEvent.EventVars.VOTE_RAISE, 1);
                    mVoteRaiseNum += 1;
                    setButtonStatus();
                } else {
                    showLoginAlertDialog(LAST_CLICK_IS_VOTE_UP);
                }
            }
        });
        mVoteDownBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FBHelper.checkFBLogin()) {
                    MSLog.d("click bad in news: " + mId);
                    PostEvent.sendNewsVote(getBaseContext(), mId, PostEvent.EventVars.VOTE_FALL, 1);
                    mVoteFallNum += 1;
                    setButtonStatus();
                } else {
                    showLoginAlertDialog(LAST_CLICK_IS_VOTE_DOWN);
                }
            }
        });

        ImageView goBackImageView = findViewById(R.id.iv_go_back);
        goBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        setButtonStatus();

        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        mUserProfileChangeListener = new UserProfile.UserProfileChangeListener() {
            @Override
            public void onUserProfileChange(int notifyId) {
                if(notifyId == NOTIFY_ID_NEWS_COMMENT_CLICK && FBHelper.checkFBLogin()) {
                    if(mLastClickAction == LAST_CLICK_IS_COMMENT) {
                        startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                                NewsWebViewActivity.this, RichEditorActivity.TYPE.NEWS, mId, mStockKeywords),
                                sEditorRequestCode);
                        overridePendingTransition(R.anim.enter, R.anim.stop);
                    } else if(mLastClickAction == LAST_CLICK_IS_LIKE) {
                        mCommentsRecyclerViewAdapter.updateCommentsLike();
                        MSLog.d("is like: " + mTempComment.isLiked());
                        if(!mTempComment.isLiked()) {
                            MSLog.d("say like at position: " + mTempPosition);
                            mTempComment.increaseLike();
                            mTempComment.setLike(true);
                            PostEvent.sendLike(NewsWebViewActivity.this, mTempComment.getCommentId());
                            mCommentsRecyclerViewAdapter.notifyItemChanged(mTempPosition);
                        }
                    } else if(mLastClickAction == LAST_CLICK_IS_VOTE_UP) {
                        MSLog.d("click good in news: " + mId);
                        PostEvent.sendNewsVote(getBaseContext(), mId, PostEvent.EventVars.VOTE_RAISE, 1);
                        mVoteRaiseNum += 1;
                        setButtonStatus();
                    } else if(mLastClickAction == LAST_CLICK_IS_VOTE_DOWN) {
                        MSLog.d("click bad in news: " + mId);
                        PostEvent.sendNewsVote(getBaseContext(), mId, PostEvent.EventVars.VOTE_FALL, 1);
                        mVoteFallNum += 1;
                        setButtonStatus();
                    }
                }
            }
        };
        userProfile.addUserProfileChangeListener(mUserProfileChangeListener);
    }

    private void changeWebViewVisibility() {
        if(mIsOriginalVisible) {
            // show our webview
            mIsOriginalVisible = false;
            mNewsWebViewMiddle.setVisibility(View.VISIBLE);
            mNewsWebViewOriginal.setVisibility(View.GONE);
            mUpperBlock.setVisibility(View.VISIBLE);
            if(mLoadingProgressBarOriginal != null) {
                mLoadingProgressBarOriginal.setVisibility(View.GONE);
            }
        } else {
            // show original webview
            mIsOriginalVisible = true;
            mNewsWebViewMiddle.setVisibility(View.GONE);
            mNewsWebViewOriginal.setVisibility(View.VISIBLE);
            mUpperBlock.setVisibility(View.GONE);
            if(!mIsOriginalCompleted && mLoadingProgressBarOriginal != null) {
                mLoadingProgressBarOriginal.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setButtonStatus() {
        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        if(userProfile.hasVoteForNews(mId)) {
            mVoteUpBlock.setEnabled(false);
            mVoteDownBlock.setEnabled(false);

            Event event = userProfile.getRecentVoteForNewsEvent(mId);
            if(event.getEventType().equals(PostEvent.EventVars.VOTE_RAISE.getEventVar())) {
                mVoteUpTextView.setTextColor(getResources().getColor(R.color.text_black));
                mVoteUpImageView.setAlpha(CONST_DISABLE_ALPHA);
            } else {
                mVoteDownTextView.setTextColor(getResources().getColor(R.color.text_black));
                mVoteDownImageView.setAlpha(CONST_DISABLE_ALPHA);
            }
        } else {
            mVoteUpBlock.setEnabled(true);
            mVoteDownBlock.setEnabled(true);

            mVoteUpTextView.setTextColor(getResources().getColor(R.color.text_gray));
            mVoteDownTextView.setTextColor(getResources().getColor(R.color.text_gray));
            mVoteUpImageView.setAlpha(CONST_ENABLE_ALPHA);
            mVoteDownImageView.setAlpha(CONST_ENABLE_ALPHA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == sEditorRequestCode) {
            if(resultCode == RESULT_OK) {
                String html = data.getStringExtra(EXTRA_RES_HTML);
                String type = data.getStringExtra(EXTRA_RES_TYPE);
                String id = data.getStringExtra(EXTRA_RES_ID);
                String eventId = data.getStringExtra(EXTRA_RES_EVENT_ID);

                Comment newComment = new Comment();
                newComment.setCommentId(eventId);
                newComment.setCommentHtml(html);
                mCommentsRecyclerViewAdapter.addOneComment(newComment);
                showCommentBlock();

                MSLog.d(String.format("user send a comment on (%s, %s, %s): %s", type, id, eventId, html));
            }
        } else if(requestCode == sReplyEditorRequestCode) {
            if(resultCode == RESULT_OK && data.getBooleanExtra(EXTRA_NEED_TO_CHANGE, false)) {
                Serializable serializable = data.getSerializableExtra(EXTRA_COMMENT);
                int position = data.getIntExtra(EXTRA_POSITION, -1);
                if (serializable != null && serializable instanceof Comment && position != -1) {
                    MSLog.d("comment with position " + position + " is needed to change");
                    Comment comment = (Comment) serializable;
                    mCommentsRecyclerViewAdapter.cloneSocialContent(position, comment);
                    mCommentsRecyclerViewAdapter.notifyItemChanged(position);
                }
            }
        }
        mFBCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.hide();
        }
    }

    private void initUpperBlock() {

        MSLog.i("title: " + mTitle + ", source: " + mSourceDate + ", image: " + mImageUrl);
        mUpperBlock = findViewById(R.id.marketsense_webview_middle_sv);
        mImageMask = findViewById(R.id.marketsense_webview_activity_image_mask);
        mNewsWebViewMiddleTitleTextView = findViewById(R.id.marketsense_webview_activity_title);
        mNewsWebViewMiddleDateTextView = findViewById(R.id.marketsense_webview_activity_source_date);
        if(mNewsWebViewMiddleTitleTextView != null) {
            mNewsWebViewMiddleTitleTextView.setText(mTitle);
        }
        if(mNewsWebViewMiddleDateTextView != null) {
            mNewsWebViewMiddleDateTextView.setText(mSourceDate);
        }
        mNewsWebViewMiddleImageView = findViewById(R.id.marketsense_webview_activity_image);
        if(mNewsWebViewMiddleImageView != null && (!mImageUrl.isEmpty() || mImageUrl.equals("None"))) {
            FrescoImageHelper.loadImageView(mImageUrl,
                    mNewsWebViewMiddleImageView, FrescoImageHelper.MAIN_IMAGE_RATIO);
            mImageMask.setVisibility(View.VISIBLE);
            mNewsWebViewMiddleTitleTextView.setTextColor(getResources().getColor(R.color.text_white));
            mNewsWebViewMiddleDateTextView.setTextColor(getResources().getColor(R.color.text_white));
        } else {
            mImageMask.setVisibility(View.GONE);
            mNewsWebViewMiddleTitleTextView.setTextColor(getResources().getColor(R.color.text_black));
            mNewsWebViewMiddleDateTextView.setTextColor(getResources().getColor(R.color.text_black));
            ConstraintLayout upperBlock = findViewById(R.id.marketsense_webview_upper_block);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) upperBlock.getLayoutParams();
            params.dimensionRatio = "1.55";
            upperBlock.setLayoutParams(params);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {

        mNewsWebViewOriginal = (NewsWebView) findViewById(R.id.news_webview_original);
        mNewsWebViewOriginal.setVerticalScrollBarEnabled(true);
        mNewsWebViewOriginal.setHorizontalFadingEdgeEnabled(false);
        mNewsWebViewOriginal.getSettings().setBlockNetworkImage(true);

        mNewsWebViewMiddle = (NewsWebView) findViewById(R.id.news_webview_middle);
        mNewsWebViewMiddle.setVerticalScrollBarEnabled(true);
        mNewsWebViewMiddle.setHorizontalFadingEdgeEnabled(false);
        mNewsWebViewMiddle.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        mNewsWebViewMiddle.getSettings().setAllowFileAccess(true);
        mNewsWebViewMiddle.getSettings().setAppCacheEnabled(true);
        mNewsWebViewMiddle.getSettings().setBlockNetworkImage(true);

        mLoadingProgressBar = findViewById(R.id.loading_progress_bar_1);
        mLoadingProgressBarOriginal = findViewById(R.id.loading_progress_bar_2);
        final ViewSkeletonScreen skeletonScreen =
                Skeleton.bind(mNewsWebViewMiddle).shimmer(false)
                        .load(R.layout.skeleton_webview).show();

        mNewsWebViewMiddle.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        mNewsWebViewMiddle.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                }
                skeletonScreen.hide();
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if(url.contains("adsbygoogle.js") || url.contains("ebay") || url.contains("amazon")) {
                    return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
                }
                return super.shouldInterceptRequest(view, url);
            }
        });

        mNewsWebViewMiddle.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress >= 80) {
                    mNewsWebViewMiddle.getSettings().setBlockNetworkImage(false);
                }

                if(newProgress >= 80 && !mTryToLoadOtherWebViewFlag) {
                    MSLog.w("GOOD!! start to load original web view");
                    mHandler.post(mLoadOriginalWebViewRunnable);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        mNewsWebViewOriginal.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mIsOriginalCompleted = true;
                if(mLoadingProgressBarOriginal != null) {
                    mLoadingProgressBarOriginal.setVisibility(View.GONE);
                }
                super.onPageFinished(view, url);
            }
        });

        mNewsWebViewOriginal.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress >= 80) {
                    mNewsWebViewOriginal.getSettings().setBlockNetworkImage(false);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        String text = Base64.encodeToString(PAGELINK_PARM_STRING.getBytes(), Base64.DEFAULT);
        mPageLink = mMiddlePageUrl + '?' + UTM_PARAM_STRING + '&' + PAGELINK_PARM_KEY + text;

        MSLog.i("Loading web page (middle): " + mPageLink);
        mNewsWebViewMiddle.loadUrl(mPageLink);
        mHandler.postDelayed(mLoadOriginalWebViewRunnable, sPostDelayMilliSeconds);
    }

    public static Intent generateNewsWebViewActivityIntent(Context context, News news) {
        return generateNewsWebViewActivityIntent(context, news.getId(),
                news.getTitle(), news.getUrlImage(), news.getDate(),
                news.getPageLink(), news.getOriginLink(),
                news.getVoteRaiseNum(), news.getVoteFallNum(), news.getStockKeywords());
    }

    public static Intent generateNewsWebViewActivityIntent(
            Context context, String id, String title, String imageUrl, String sourceDate,
            String middleUrl, String originalUrl, int voteRaiseNum, int voteFallNum, String[] stockKeywords) {
        Intent intent = new Intent(context, NewsWebViewActivity.class);
        intent.putExtra(EXTRA_MIDDLE_ID, id);
        intent.putExtra(EXTRA_MIDDLE_TITLE, title);
        intent.putExtra(EXTRA_MIDDLE_DATE, sourceDate);
        intent.putExtra(EXTRA_MIDDLE_IMAGE_URL, imageUrl);
        intent.putExtra(EXTRA_MIDDLE_PAGE_URL, middleUrl);
        intent.putExtra(EXTRA_ORIGINAL_PAGE_URL, originalUrl);
        intent.putExtra(EXTRA_VOTE_RAISE_NUM, voteRaiseNum);
        intent.putExtra(EXTRA_VOTE_FALL_NUM, voteFallNum);
        intent.putExtra(EXTRA_STOCK_KEYWORDS, stockKeywords);
        return intent;
    }

    @Override
    protected void onDestroy() {
        if(mHandler != null) {
            mHandler.removeCallbacks(mLoadOriginalWebViewRunnable);
            mHandler = null;
        }
        if(mNewsWebViewMiddle != null) {
            mNewsWebViewMiddle.destroy();
            mNewsWebViewMiddle = null;
        }
        if(mNewsWebViewOriginal != null) {
            mNewsWebViewOriginal.destroy();
            mNewsWebViewOriginal = null;
        }
        if(mCommentsRecyclerViewAdapter != null) {
            mCommentsRecyclerViewAdapter.destroy();
            mCommentsRecyclerViewAdapter = null;
        }
        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        userProfile.deleteUserProfileChangeListener(mUserProfileChangeListener);
        super.onDestroy();
    }

    // fb login part when the user click fab
    private void initFBLogin() {

        MSLog.d("The user has logged in Facebook: " + FBHelper.checkFBLogin());

        mFBCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mFBCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                MSLog.d("facebook registerCallback onSuccess in StockActivity");
                getFBUserProfile();
            }

            @Override
            public void onCancel() {
                MSLog.d("facebook registerCallback onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                MSLog.d("facebook registerCallback onError: " + exception.toString());
            }
        });
    }

    private void getFBUserProfile() {
        FBHelper.getFBUserProfile(this, new FBHelper.FBHelperListener() {
            @Override
            public void onTaskCompleted(JSONObject data, String avatarLink) {
                String userName = FBHelper.fetchFbData(data, UserProfile.FB_USER_NAME_KEY);
                String userId = FBHelper.fetchFbData(data, UserProfile.FB_USER_ID_KEY);
                String userEmail = FBHelper.fetchFbData(data, UserProfile.FB_USER_EMAIL_KEY);
                PostEvent.sendRegister(NewsWebViewActivity.this, userId, userName, FACEBOOK_CONSTANTS,
                        UserProfile.generatePassword(userId, FACEBOOK_CONSTANTS), userEmail, avatarLink, new PostEvent.PostEventListener() {
                            @Override
                            public void onResponse(boolean isSuccessful, Object data) {
                                if(!isSuccessful) {
                                    Toast.makeText(NewsWebViewActivity.this, R.string.login_failed_description, Toast.LENGTH_SHORT).show();
                                    LoginManager.getInstance().logOut();
                                } else {
                                    UserProfile userProfile = ClientData.getInstance(NewsWebViewActivity.this).getUserProfile();
                                    userProfile.notifyUserProfile(NOTIFY_ID_NEWS_COMMENT_CLICK);
                                }
                            }
                        });
            }
        }, true);
    }

    private void showLoginAlertDialog(int lastClick) {
        mLastClickAction = lastClick;
        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }

        final View alertView = LayoutInflater.from(this)
                .inflate(R.layout.alertdialog_login, null);
        mLoginAlertDialog = new AlertDialog.Builder(this)
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

    @Override
    public void onPause() {
        super.onPause();

        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }
    }
}
