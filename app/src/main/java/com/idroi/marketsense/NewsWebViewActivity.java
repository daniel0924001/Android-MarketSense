package com.idroi.marketsense;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.adapter.KnowledgeRecyclerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.data.Event;
import com.idroi.marketsense.data.Knowledge;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.util.ActionBarHelper;
import com.idroi.marketsense.util.NewsReadRecordHelper;
import com.idroi.marketsense.viewholders.CommentRecyclerViewViewHolder;
import com.idroi.marketsense.viewholders.KnowledgeYouMayWantToKnowViewHolder;
import com.idroi.marketsense.viewholders.NewsWebViewTopViewHolder;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import static com.idroi.marketsense.CommentActivity.EXTRA_COMMENT;
import static com.idroi.marketsense.CommentActivity.EXTRA_NEED_TO_CHANGE;
import static com.idroi.marketsense.CommentActivity.EXTRA_POSITION;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_EVENT_ID;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_HTML;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_ID;
import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_TYPE;
import static com.idroi.marketsense.RichEditorActivity.sEditorRequestCode;
import static com.idroi.marketsense.RichEditorActivity.sReplyEditorRequestCode;
import static com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter.ADAPTER_CHANGE_LIKE_ONLY;
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
    public static final String EXTRA_EXPLICIT_KEYWORDS = "EXTRA_EXPLICIT_KEYWORDS";
    public static final String EXTRA_NEWS_LEVEL = "EXTRA_NEWS_LEVEL";

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
    private int mLevel;
    private String[] mStockKeywords;
    private ArrayList<String> mExplicitKeywords;

    private ScrollView mUpperBlock;
    private NewsWebView mNewsWebViewOriginal;
    private NewsWebView mNewsWebViewMiddle;
    private ProgressBar mLoadingProgressBar, mLoadingProgressBarOriginal;

    private ConstraintLayout mVoteUpBlock, mVoteDownBlock;
    private TextView mVoteUpTextView, mVoteDownTextView;
    private ImageView mVoteUpImageView, mVoteDownImageView;

    private AlertDialog mLoginAlertDialog;
    private LoginButton mFBLoginBtn;
    private UserProfile.GlobalBroadcastListener mGlobalBroadcastListener;
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

    private CommentRecyclerViewViewHolder mCommentRecyclerViewViewHolder;
    private KnowledgeYouMayWantToKnowViewHolder mYouMayWantToKnowViewHolder;

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
        initYouMayWantToKnowBlock();
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
        Intent intent = getIntent();
        mId = intent.getStringExtra(EXTRA_MIDDLE_ID);
        mMiddlePageUrl = intent.getStringExtra(EXTRA_MIDDLE_PAGE_URL);
        mOriginalPageUrl = intent.getStringExtra(EXTRA_ORIGINAL_PAGE_URL);
        mTitle = intent.getStringExtra(EXTRA_MIDDLE_TITLE);
        mImageUrl = intent.getStringExtra(EXTRA_MIDDLE_IMAGE_URL);
        mSourceDate = intent.getStringExtra(EXTRA_MIDDLE_DATE);
        mStockKeywords = intent.getStringArrayExtra(EXTRA_STOCK_KEYWORDS);
        mExplicitKeywords = intent.getStringArrayListExtra(EXTRA_EXPLICIT_KEYWORDS);

        mVoteRaiseNum = intent.getIntExtra(EXTRA_VOTE_RAISE_NUM, 0);
        mVoteFallNum = intent.getIntExtra(EXTRA_VOTE_FALL_NUM, 0);
        mLevel = intent.getIntExtra(EXTRA_NEWS_LEVEL, 0);
    }

    private void initYouMayWantToKnowBlock() {
        mYouMayWantToKnowViewHolder = KnowledgeYouMayWantToKnowViewHolder
                .convertToViewHolder(findViewById(R.id.you_may_want_to_know_block));
        mYouMayWantToKnowViewHolder.setRelatedKnowledge(this, mExplicitKeywords, new KnowledgeRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Knowledge knowledge) {
                startActivity(KnowledgeActivity.generateKnowledgeActivityIntent(
                        NewsWebViewActivity.this, knowledge));
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
    }

    private void initComments() {
        mCommentRecyclerViewViewHolder = CommentRecyclerViewViewHolder
                .convertToViewHolder(findViewById(R.id.target_comment_block));
        mCommentRecyclerViewViewHolder.init(this, mId, new CommentsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onSayLikeItemClick(Comment comment, int position) {
                if(FBHelper.checkFBLogin()) {
                    MSLog.d("say like at position: " + position);
                    comment.increaseLike();
                    comment.setLike(true);
                    PostEvent.sendLike(NewsWebViewActivity.this, comment.getCommentId());
                    mCommentRecyclerViewViewHolder.mCommentsRecyclerViewAdapter.notifyItemChanged(position, ADAPTER_CHANGE_LIKE_ONLY);
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
        }, new CommentsRecyclerViewAdapter.CommentsAvailableListener() {
            @Override
            public void onCommentsAvailable(CommentAndVote commentAndVote) {
                if(commentAndVote != null) {
                    if (commentAndVote.getCommentSize() > 0) {
                        mCommentRecyclerViewViewHolder.showCommentBlock(NewsWebViewActivity.this);
                    }
                    mVoteRaiseNum = commentAndVote.getRaiseNumber();
                    mVoteFallNum = commentAndVote.getFallNumber();
                    setButtonStatus();
                }
            }
        });
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
                            NewsWebViewActivity.this, RichEditorActivity.TYPE.NEWS, mId, mStockKeywords, mTitle, mLevel),
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

        setButtonStatus();

        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        mGlobalBroadcastListener = new UserProfile.GlobalBroadcastListener() {
            @Override
            public void onGlobalBroadcast(int notifyId, Object payload) {
                if(notifyId == NOTIFY_ID_NEWS_COMMENT_CLICK && FBHelper.checkFBLogin()) {
                    if(mLastClickAction == LAST_CLICK_IS_COMMENT) {
                        startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                                NewsWebViewActivity.this, RichEditorActivity.TYPE.NEWS, mId, mStockKeywords, mTitle, mLevel),
                                sEditorRequestCode);
                        overridePendingTransition(R.anim.enter, R.anim.stop);
                    } else if(mLastClickAction == LAST_CLICK_IS_LIKE) {
                        mCommentRecyclerViewViewHolder.mCommentsRecyclerViewAdapter.updateCommentsLike();
                        MSLog.d("is like: " + mTempComment.isLiked());
                        if(!mTempComment.isLiked()) {
                            MSLog.d("say like at position: " + mTempPosition);
                            mTempComment.increaseLike();
                            mTempComment.setLike(true);
                            PostEvent.sendLike(NewsWebViewActivity.this, mTempComment.getCommentId());
                            mCommentRecyclerViewViewHolder.mCommentsRecyclerViewAdapter.notifyItemChanged(mTempPosition, ADAPTER_CHANGE_LIKE_ONLY);
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
        userProfile.addGlobalBroadcastListener(mGlobalBroadcastListener);
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
                mVoteUpTextView.setTextColor(getResources().getColor(R.color.text_first));
                mVoteUpImageView.setAlpha(CONST_DISABLE_ALPHA);
            } else {
                mVoteDownTextView.setTextColor(getResources().getColor(R.color.text_first));
                mVoteDownImageView.setAlpha(CONST_DISABLE_ALPHA);
            }
        } else {
            mVoteUpBlock.setEnabled(true);
            mVoteDownBlock.setEnabled(true);

            mVoteUpTextView.setTextColor(getResources().getColor(R.color.text_third));
            mVoteDownTextView.setTextColor(getResources().getColor(R.color.text_third));
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
                mCommentRecyclerViewViewHolder.mCommentsRecyclerViewAdapter.addOneComment(newComment);
                mCommentRecyclerViewViewHolder.showCommentBlock(this);

                MSLog.d(String.format("user send a comment on (%s, %s, %s): %s", type, id, eventId, html));
            }
        } else if(requestCode == sReplyEditorRequestCode) {
            if(resultCode == RESULT_OK && data.getBooleanExtra(EXTRA_NEED_TO_CHANGE, false)) {
                Serializable serializable = data.getSerializableExtra(EXTRA_COMMENT);
                int position = data.getIntExtra(EXTRA_POSITION, -1);
                if (serializable != null && serializable instanceof Comment && position != -1) {
                    MSLog.d("comment with position " + position + " is needed to change");
                    Comment comment = (Comment) serializable;
                    mCommentRecyclerViewViewHolder.mCommentsRecyclerViewAdapter.cloneSocialContent(position, comment);
                    mCommentRecyclerViewViewHolder.mCommentsRecyclerViewAdapter.notifyItemChanged(position, ADAPTER_CHANGE_LIKE_ONLY);
                }
            }
        }
        mFBCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setActionBar() {
        ActionBarHelper.setActionBarForSimpleTitleAndBack(this, null);
    }

    private void initUpperBlock() {
        NewsWebViewTopViewHolder newsWebViewTopViewHolder = NewsWebViewTopViewHolder
                .convertToViewHolder(findViewById(R.id.marketsense_webview_upper_block));
        newsWebViewTopViewHolder.update(this, mTitle, mSourceDate, mStockKeywords, mLevel);

        MSLog.i("title: " + mTitle + ", source: " + mSourceDate);
        mUpperBlock = findViewById(R.id.marketsense_webview_middle_sv);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {

        mNewsWebViewOriginal = findViewById(R.id.news_webview_original);
        mNewsWebViewOriginal.setVerticalScrollBarEnabled(true);
        mNewsWebViewOriginal.setHorizontalFadingEdgeEnabled(false);
        mNewsWebViewOriginal.getSettings().setBlockNetworkImage(true);

        mNewsWebViewMiddle = findViewById(R.id.news_webview_middle);
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
                if(url.contains("adsbygoogle.js") || url.contains("ebay") || url.contains("amazon") || url.contains("taboola")) {
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
                    if(mLoadingProgressBar != null) {
                        mLoadingProgressBar.setVisibility(View.GONE);
                    }
                    skeletonScreen.hide();
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
                super.onPageFinished(view, url);
            }
        });

        mNewsWebViewOriginal.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress >= 80) {
                    mNewsWebViewOriginal.getSettings().setBlockNetworkImage(false);

                    mIsOriginalCompleted = true;
                    if(mLoadingProgressBarOriginal != null) {
                        mLoadingProgressBarOriginal.setVisibility(View.GONE);
                    }
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
                news.getVoteRaiseNum(), news.getVoteFallNum(),
                news.getStockKeywords(), news.getExplicitKeywords(), news.getLevel());
    }

    public static Intent generateNewsWebViewActivityIntent(
            Context context, String id, String title, String imageUrl, String sourceDate,
            String middleUrl, String originalUrl, int voteRaiseNum, int voteFallNum,
            String[] stockKeywords, ArrayList<String> explicitKeywords, int level) {
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
        intent.putExtra(EXTRA_EXPLICIT_KEYWORDS, explicitKeywords);
        intent.putExtra(EXTRA_NEWS_LEVEL, level);
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
        if(mCommentRecyclerViewViewHolder != null) {
            mCommentRecyclerViewViewHolder.destroy();
            mCommentRecyclerViewViewHolder = null;
        }
        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        userProfile.deleteGlobalBroadcastListener(mGlobalBroadcastListener);
        NewsReadRecordHelper.saveToInternalStorage(this, userProfile.getUserId(), userProfile.getNewsReadRecords());
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
                                    userProfile.globalBroadcast(NOTIFY_ID_NEWS_COMMENT_CLICK);
                                }
                            }
                        });
            }
        });
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
