package com.idroi.marketsense;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.YahooStxChartCrawler;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.request.SingleNewsRequest;

import org.json.JSONObject;

import static com.idroi.marketsense.RichEditorActivity.EXTRA_RES_HTML;
import static com.idroi.marketsense.RichEditorActivity.sEditorRequestCode;
import static com.idroi.marketsense.adapter.NewsRecyclerAdapter.NEWS_SINGLE_LAYOUT;
import static com.idroi.marketsense.common.Constants.FACEBOOK_CONSTANTS;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_USER_HAS_LOGIN;
import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_NAME;
import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_TASK_ID;

/**
 * Created by daniel.hsieh on 2018/4/24.
 */

public class StockActivity extends AppCompatActivity {

    public final static String EXTRA_CODE = "com.idroi.marketsense.StockActivity.extra_code";
    public final static String EXTRA_RAISE_NUM = "com.idroi.marketsense.StockActivity.extra_raise_number";
    public final static String EXTRA_FALL_NUM = "com.idroi.marketsense.StockActivity.extra_fall_number";
    public final static String EXTRA_PRICE = "com.idroi.marketsense.StockActivity.extra_price";
    public final static String EXTRA_DIFF_NUM = "com.idroi.marketsense.StockActivity.extra_diff_num";
    public final static String EXTRA_DIFF_PERCENTAGE = "com.idroi.marketsense.StockActivity.extra_diff_percentage";

    public final static int CLICK_NOTHING_BEFORE_LOGIN = 0;
    public final static int CLICK_STAR_BEFORE_LOGIN = 1;
    public final static int CLICK_COMMENT_BEFORE_LOGIN = 2;

    private YahooStxChartCrawler mYahooStxChartCrawler;
    private ViewSkeletonScreen mSkeletonScreen;
    private String mStockName;
    private String mCode;
    private String mPrice, mDiffNum, mDiffPercentage;
    private int mRaiseNum, mFallNum;
    private Button mButtonRaise, mButtonFall, mButtonResult;

    private CallbackManager mFBCallbackManager;
    private UserProfile mUserProfile;
    private boolean mIsFavorite;

    private RecyclerView mCommentRecyclerView;
    private CommentsRecyclerViewAdapter mCommentsRecyclerViewAdapter;
    private RecyclerView mNewsRecyclerView;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;

    private int mVoteRaiseNum, mVoteFallNum;

    private UserProfile.UserProfileChangeListener mUserProfileChangeListener;
    private int mLastClickedButton;
    private AlertDialog mLoginAlertDialog;
    private LoginButton mFBLoginBtn;
    private ImageView mAddFavorite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        mUserProfile = ClientData.getInstance(this).getUserProfile();

        initFBLogin();
        setInformation();
        setActionBar();
        initStockChart();
        setSocialButtons();
        setSelector();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }

    @Override
    protected void onDestroy() {
        mNewsRecyclerAdapter.destroy();
        mUserProfile.deleteUserProfileChangeListener(mUserProfileChangeListener);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }
    }

    private void setSocialButtons() {
        mButtonRaise = findViewById(R.id.vote_up_btn);
        mButtonFall = findViewById(R.id.vote_down_btn);
        mButtonResult = findViewById(R.id.vote_result_btn);

        final NestedScrollView nestedScrollView = findViewById(R.id.body_scroll_view);
        Button buttonGoUp = findViewById(R.id.btn_go_up);
        buttonGoUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nestedScrollView.scrollTo(0, 0);
            }
        });

        Button buttonWriteComment = findViewById(R.id.btn_write_comment);
        buttonWriteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FBHelper.checkFBLogin()) {
                    startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                            StockActivity.this, RichEditorActivity.TYPE.STOCK, mCode),
                            sEditorRequestCode);
                    overridePendingTransition(R.anim.enter, R.anim.stop);
                } else {
                    showLoginAlertDialog(CLICK_COMMENT_BEFORE_LOGIN);
                }
            }
        });

        Button buttonWriteFirst = findViewById(R.id.btn_send_first);
        buttonWriteFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FBHelper.checkFBLogin()) {
                    startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                            StockActivity.this, RichEditorActivity.TYPE.STOCK, mCode),
                            sEditorRequestCode);
                    overridePendingTransition(R.anim.enter, R.anim.stop);
                } else {
                    showLoginAlertDialog(CLICK_COMMENT_BEFORE_LOGIN);
                }
            }
        });

        mUserProfileChangeListener = new UserProfile.UserProfileChangeListener() {
            @Override
            public void onUserProfileChange(int notifyId) {
                if(notifyId == NOTIFY_USER_HAS_LOGIN && FBHelper.checkFBLogin()) {
                    switch (mLastClickedButton) {
                        case CLICK_COMMENT_BEFORE_LOGIN:
                            startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                                    StockActivity.this, RichEditorActivity.TYPE.STOCK, mCode),
                                    sEditorRequestCode);
                            overridePendingTransition(R.anim.enter, R.anim.stop);
                            return;
                        case CLICK_STAR_BEFORE_LOGIN:
                            changeFavorite(mAddFavorite);
                    }
                }
            }
        };
        mUserProfile.addUserProfileChangeListener(mUserProfileChangeListener);
    }

    private void showLoginAlertDialog(int lastButton) {
        mLastClickedButton = lastButton;

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

    private void setSelector() {
        initSelector();
        initComments();
        initNews();
    }

    private void initSelector() {
        final ConstraintLayout commentBlock = findViewById(R.id.marketsense_stock_comment);
        final ConstraintLayout newsBlock = findViewById(R.id.marketsense_stock_news);
        final ConstraintLayout selectorComment = findViewById(R.id.selector_comment_block);
        final ConstraintLayout selectorNews = findViewById(R.id.selector_news_block);
        selectorComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentBlock.setVisibility(View.VISIBLE);
                newsBlock.setVisibility(View.GONE);
                selectorComment.setBackground(getDrawable(R.drawable.border_selector_selected));
                selectorNews.setBackground(getDrawable(R.drawable.border_selector));
            }
        });
        selectorNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentBlock.setVisibility(View.GONE);
                newsBlock.setVisibility(View.VISIBLE);
                selectorComment.setBackground(getDrawable(R.drawable.border_selector));
                selectorNews.setBackground(getDrawable(R.drawable.border_selector_selected));
            }
        });
    }

    private void initNews() {
        Bundle bundle = new Bundle();
        bundle.putString(KEYWORD_NAME, mStockName);
        mNewsRecyclerView = findViewById(R.id.news_recycler_view);
        mNewsRecyclerAdapter = new NewsRecyclerAdapter(this, KEYWORD_TASK_ID, bundle);
        mNewsRecyclerAdapter.setNewsLayoutType(NEWS_SINGLE_LAYOUT);
        mNewsRecyclerView.setAdapter(mNewsRecyclerAdapter);
        mNewsRecyclerView.setNestedScrollingEnabled(false);
        mNewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mNewsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    MSLog.e("expand 7");
                    mNewsRecyclerAdapter.expand(7);
                }
            }
        });

        mNewsRecyclerAdapter.setNewsAvailableListener(new NewsRecyclerAdapter.NewsAvailableListener() {
            @Override
            public void onNewsAvailable() {
                setNewsForEmptyData(false);
            }

            @Override
            public void onNewsEmpty() {
                setNewsForEmptyData(true);
            }
        });

        String networkUrl = NewsRequest.queryKeywordNewsUrl(this, mStockName, true);
        if(networkUrl == null) {
            setNewsForEmptyData(true);
            return;
        }
        String cacheUrl = NewsRequest.queryKeywordNewsUrl(this, mStockName, false);
        mNewsRecyclerAdapter.loadNews(networkUrl, cacheUrl);

        mNewsRecyclerAdapter.setOnItemClickListener(new NewsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(News news) {
                startActivity(NewsWebViewActivity.generateNewsWebViewActivityIntent(
                        StockActivity.this, news.getId(), news.getTitle(),
                        news.getUrlImage(), news.getDate(),
                        news.getPageLink(), news.getOriginLink(),
                        news.getVoteRaiseNum(), news.getVoteFallNum()));
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
    }

    private void setNewsForEmptyData(boolean isEmpty) {

        TextView newsJoin = findViewById(R.id.news_join);
        String format = getResources().getString(R.string.title_news_join);
        newsJoin.setText(String.format(format, mNewsRecyclerAdapter.getNewsTotalCount()));

        ImageView noDataImageView = findViewById(R.id.no_news_iv);
        TextView noDataTextView = findViewById(R.id.no_news_tv);
        if(isEmpty) {
            mNewsRecyclerView.setVisibility(View.GONE);
            noDataTextView.setVisibility(View.VISIBLE);
            noDataImageView.setVisibility(View.VISIBLE);
            noDataTextView.setText(R.string.ops_something_wrong);
            noDataImageView.setImageResource(R.drawable.baseline_sentiment_dissatisfied_24px);
        } else {
            mNewsRecyclerView.setVisibility(View.VISIBLE);
            noDataTextView.setVisibility(View.GONE);
            noDataImageView.setVisibility(View.GONE);
        }
    }

    private void initComments() {
        mCommentRecyclerView = findViewById(R.id.marketsense_stock_comment_rv);

        mCommentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter(this);
        mCommentRecyclerView.setAdapter(mCommentsRecyclerViewAdapter);
        mCommentRecyclerView.setNestedScrollingEnabled(false);

        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentsRecyclerViewAdapter.setCommentsAvailableListener(new CommentsRecyclerViewAdapter.CommentsAvailableListener() {
            @Override
            public void onCommentsAvailable(CommentAndVote commentAndVote) {
                if(commentAndVote.getCommentSize() > 0) {
                    showCommentBlock();
                }
                mVoteRaiseNum = commentAndVote.getRaiseNumber();
                mVoteFallNum = commentAndVote.getFallNumber();
//                setButtonStatus();
                MSLog.d("raise number: " + commentAndVote.getRaiseNumber());
                MSLog.d("fall number: " + commentAndVote.getFallNumber());
                setSocialInformation(commentAndVote);
            }
        });
        mCommentsRecyclerViewAdapter.loadCommentsList(SingleNewsRequest.querySingleNewsUrl(mCode, SingleNewsRequest.TASK.STOCK_COMMENT));
    }

    private void showCommentBlock() {
        findViewById(R.id.marketsense_stock_no_comment_iv).setVisibility(View.GONE);
        findViewById(R.id.marketsense_stock_no_comment_tv).setVisibility(View.GONE);
        findViewById(R.id.btn_send_first).setVisibility(View.GONE);
        mCommentRecyclerView.setVisibility(View.VISIBLE);
    }

    private void setSocialInformation(CommentAndVote commentAndVote) {
        TextView peopleJoin = findViewById(R.id.people_join);
        String format = getResources().getString(R.string.title_people_join);
        peopleJoin.setText(String.format(format, commentAndVote.getCommentSize()));

        TextView peopleScore = findViewById(R.id.people_score);
        TextView newsScore = findViewById(R.id.news_score);
        TextView peopleAttitude = findViewById(R.id.people_attitude);
        TextView newsAttitude = findViewById(R.id.news_attitude);

        peopleScore.setText(commentAndVote.getVotingScore());
        newsScore.setText(commentAndVote.getPredictionScore());
        peopleAttitude.setText(commentAndVote.getVotingAttitude(this));
        newsAttitude.setText(commentAndVote.getPredictionAttitude(this));

        ImageView peopleImageView = findViewById(R.id.people_score_iv);
        ImageView newsImageView = findViewById(R.id.news_score_iv);
        commentAndVote.setVotingIcon(this, peopleImageView);
        commentAndVote.setPredictionIcon(this, newsImageView);
    }

    private void initStockChart() {

        TextView priceTextView = findViewById(R.id.stock_price_tv);
        TextView diffTextView = findViewById(R.id.stock_diff_tv);
        priceTextView.setText(mPrice);
        String format = getResources().getString(R.string.title_diff_format);
        diffTextView.setText(String.format(format, mDiffNum, mDiffPercentage));

        LineChart lineChart = findViewById(R.id.stock_chart_price);
        BarChart barChart = findViewById(R.id.stock_chart_volume);
        mYahooStxChartCrawler =
                new YahooStxChartCrawler(this, mStockName, mCode, lineChart, barChart);
        mYahooStxChartCrawler.setYahooStxChartListener(new YahooStxChartCrawler.YahooStxChartListener() {
            @Override
            public void onStxChartDataLoad() {
                mYahooStxChartCrawler.renderStockChartData();
                mSkeletonScreen.hide();
            }

            @Override
            public void onStxChartDataFail(MarketSenseError marketSenseError) {
                mYahooStxChartCrawler.renderStockChartData();
                mSkeletonScreen.hide();
                MSLog.e("onStxChartDataFail: " + marketSenseError.toString());
            }
        });
        mYahooStxChartCrawler.loadStockChartData();

        mSkeletonScreen = Skeleton.bind(lineChart)
                .shimmer(false)
                .load(R.layout.skeleton_webview)
                .show();
    }

    private void setInformation() {
        mStockName = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        mCode = getIntent().getStringExtra(EXTRA_CODE);
        mRaiseNum = getIntent().getIntExtra(EXTRA_RAISE_NUM, 0);
        mFallNum = getIntent().getIntExtra(EXTRA_FALL_NUM, 0);

        mIsFavorite = mUserProfile.isFavoriteStock(mCode);

        mPrice = getIntent().getStringExtra(EXTRA_PRICE);
        mDiffNum = getIntent().getStringExtra(EXTRA_DIFF_NUM);
        mDiffPercentage = getIntent().getStringExtra(EXTRA_DIFF_PERCENTAGE);
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.main_action_bar, null);

            SimpleDraweeView imageView = view.findViewById(R.id.action_bar_avatar);
            if(imageView != null) {
                imageView.setImageResource(R.drawable.ic_keyboard_backspace_white_24px);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
            }

            TextView textView = view.findViewById(R.id.action_bar_name);
            if(textView != null) {
                String title = mStockName + " " + mCode;
                textView.setText(title);
            }

            mAddFavorite = view.findViewById(R.id.action_bar_notification);
            if(mAddFavorite != null) {
                mAddFavorite.setVisibility(View.VISIBLE);
                if(mIsFavorite) {
                    mAddFavorite.setImageResource(R.drawable.ic_star_yellow_24px);
                } else {
                    mAddFavorite.setImageResource(R.drawable.ic_star_border_white_24px);
                }
                mAddFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(FBHelper.checkFBLogin()) {
                            changeFavorite(mAddFavorite);
                        } else {
                            showLoginAlertDialog(CLICK_STAR_BEFORE_LOGIN);
                        }
                    }
                });
            }

            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    private void changeFavorite(final ImageView imageView) {
        if(mIsFavorite) {
            mUserProfile.deleteFavoriteStock(mCode);
            PostEvent.sendFavoriteStocksDelete(this, mCode);
            String format = getResources().getString(R.string.title_delete_complete);
            Toast.makeText(this, String.format(format, mStockName, mCode), Toast.LENGTH_SHORT).show();
            imageView.setImageResource(R.drawable.ic_star_border_white_24px);
        } else {
            mUserProfile.addFavoriteStock(mCode);
            PostEvent.sendFavoriteStocksAdd(this, mCode);
            String format = getResources().getString(R.string.title_add_complete);
            Toast.makeText(this, String.format(format, mStockName, mCode), Toast.LENGTH_SHORT).show();
            imageView.setImageResource(R.drawable.ic_star_yellow_24px);
        }
        mIsFavorite = mUserProfile.isFavoriteStock(mCode);
        mUserProfile.notifyUserProfile(NOTIFY_ID_FAVORITE_LIST);
    }

    public static Intent generateStockActivityIntent(Context context,
                                                     String title, String code,
                                                     int raiseNum, int fallNum,
                                                     String price, String diffNum, String diffPercentage) {
        Intent intent = new Intent(context, StockActivity.class);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(EXTRA_CODE, code);
        intent.putExtra(EXTRA_RAISE_NUM, raiseNum);
        intent.putExtra(EXTRA_FALL_NUM, fallNum);
        intent.putExtra(EXTRA_PRICE, price);
        intent.putExtra(EXTRA_DIFF_NUM, diffNum);
        intent.putExtra(EXTRA_DIFF_PERCENTAGE, diffPercentage);
        return intent;
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
                PostEvent.sendRegister(StockActivity.this, userId, userName, FACEBOOK_CONSTANTS,
                        UserProfile.generatePassword(userId, FACEBOOK_CONSTANTS), userEmail, avatarLink);
                mUserProfile.notifyUserProfile(NOTIFY_USER_HAS_LOGIN);
            }
        }, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == sEditorRequestCode) {
            if(resultCode == RESULT_OK) {
                String html = data.getStringExtra(EXTRA_RES_HTML);
                Comment comment = new Comment();
                comment.setCommentHtml(html);
                mCommentsRecyclerViewAdapter.addOneComment(comment);
                showCommentBlock();
                MSLog.d("user send a comment on code: " + mCode);
                MSLog.d("user send a comment of html: " + html);
            }
        }
        mFBCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
