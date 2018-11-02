package com.idroi.marketsense;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.adapter.CriticalStatisticsAdapter;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.YahooStxChartCrawler;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.StockTradeData;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.CriticalStatisticsRequest;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.request.CommentAndVoteRequest;
import com.idroi.marketsense.request.StockChartDataRequest;
import com.idroi.marketsense.util.MarketSenseUtils;
import com.idroi.marketsense.viewholders.ChartPeriodSelectorViewHolder;
import com.idroi.marketsense.viewholders.ChartTaTopItemsViewHolder;
import com.idroi.marketsense.viewholders.ChartTickBottomItemsViewHolder;
import com.idroi.marketsense.viewholders.ChartTickTopItemsViewHolder;
import com.idroi.marketsense.viewholders.FiveBestPriceViewHolder;
import com.idroi.marketsense.viewholders.PredictForDifferentPeriodViewHolder;
import com.idroi.marketsense.viewholders.StockActivityActionBarViewHolder;
import com.idroi.marketsense.viewholders.StockActivityBottomContent;
import com.idroi.marketsense.viewholders.StockActivityRealPriceBlockViewHolder;
import com.idroi.marketsense.viewholders.StockPredictionBlockViewHolder;
import com.idroi.marketsense.viewholders.StockPredictionInfoBlockViewHolder;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import static com.idroi.marketsense.adapter.NewsRecyclerAdapter.NEWS_SINGLE_LAYOUT;
import static com.idroi.marketsense.common.Constants.FACEBOOK_CONSTANTS;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_NEWS_READ_RECORD_LIST;
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
    public final static int CLICK_LIKE_BEFORE_LOGIN = 4;
    public final static int CLICK_VOTE_UP_BEFORE_LOGIN = 5;
    public final static int CLICK_VOTE_DOWN_BEFORE_LOGIN = 6;

    private YahooStxChartCrawler mYahooStxChartCrawler;
    private ProgressBar mLoadingProgressBar;
    private String mStockName;
    private String mCode;
    private String mPrice, mDiffNum, mDiffPercentage;

    private CallbackManager mFBCallbackManager;
    private UserProfile mUserProfile;
    private boolean mIsFavorite;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mSelectedChartTextView;

    private RecyclerView mCommentRecyclerView;
    private CommentsRecyclerViewAdapter mCommentsRecyclerViewAdapter;
    private RecyclerView mNewsRecyclerView;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;

    private UserProfile.GlobalBroadcastListener mGlobalBroadcastListener;
    private int mLastClickedButton;
    private AlertDialog mLoginAlertDialog, mStarAlertDialog;
    private LoginButton mFBLoginBtn;
    private NestedScrollView mNestedScrollView;

    private View mMoreAlertView;
    private RecyclerView mCriticalStatisticsRecyclerView;
    private CriticalStatisticsAdapter mCriticalStatisticsAdapter;

    private Comment mTempComment;
    private int mTempPosition;

    private LineChart mLineChart;
    private BarChart mBarChart;
    private CandleStickChart mCandleStickChart;

    private StockActivityActionBarViewHolder mStockActivityActionBarViewHolder;
    private StockActivityRealPriceBlockViewHolder mStockActivityRealPriceBlockViewHolder;
    private StockPredictionInfoBlockViewHolder mStockPredictionInfoBlockViewHolder;
    private ChartPeriodSelectorViewHolder mChartPeriodSelectorViewHolder;
    private ChartTickTopItemsViewHolder mChartTickTopItemsViewHolder;
    private ChartTaTopItemsViewHolder mChartTaTopItemsViewHolder;
    private ChartTickBottomItemsViewHolder mChartTickBottomItemsViewHolder;
    private FiveBestPriceViewHolder mBestPriceRowViewHolder;
    private StockActivityBottomContent mStockActivityBottomContent;

    private View.OnLayoutChangeListener mOnLayoutChangeListener;
    private int mRealTimeContainerShrinkHeight = Integer.MAX_VALUE;
    private int mSelectorTop = Integer.MAX_VALUE;
    private boolean mIsSelectorFixed = false;

    private AlertDialog mMoreAlertDialog;

    private boolean mIsTopShrink = false;

    private Handler mHandler = new Handler();
    private Runnable mExpandRunnable = new Runnable() {
        @Override
        public void run() {
            if(mNewsRecyclerAdapter != null) {
                mNewsRecyclerAdapter.expand(7);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrescoHelper.initialize(getApplicationContext());
        setContentView(R.layout.activity_stock);

        mUserProfile = ClientData.getInstance(this).getUserProfile();

        initFBLogin();
        setInformation();
        initViewHolders();
        setActionBar();
        initCriticalStatistics();
        initStockPredictionBlock();
        initStockChart();
        setSelector();
        initSocialButtons();
    }

    private void initViewHolders() {
        mStockActivityRealPriceBlockViewHolder =
                StockActivityRealPriceBlockViewHolder
                        .convertToViewHolder(findViewById(R.id.top_real_price_block));
        mChartPeriodSelectorViewHolder =
                ChartPeriodSelectorViewHolder
                        .convertToViewHolder(findViewById(R.id.stock_period_block));
        mChartTickTopItemsViewHolder =
                ChartTickTopItemsViewHolder
                        .convertToViewHolder(findViewById(R.id.stock_chart_tick_top_block));
        mChartTaTopItemsViewHolder =
                ChartTaTopItemsViewHolder
                        .convertToViewHolder(findViewById(R.id.stock_chart_ta_top_block));
        mChartTickBottomItemsViewHolder =
                ChartTickBottomItemsViewHolder
                        .convertToViewHolder(findViewById(R.id.stock_chart_tick_bottom_block));
        mBestPriceRowViewHolder =
                FiveBestPriceViewHolder
                        .convertToViewHolder(findViewById(R.id.five_best_price));
        mStockPredictionInfoBlockViewHolder =
                StockPredictionInfoBlockViewHolder
                        .convertToViewHolder(findViewById(R.id.stock_prediction_block).findViewById(R.id.stock_prediction_block));
        mStockActivityBottomContent =
                StockActivityBottomContent
                        .convertToViewHolder(findViewById(R.id.stock_activity_bottom_selector));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mUserProfile.deleteGlobalBroadcastListener(mGlobalBroadcastListener);
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }

    @Override
    protected void onDestroy() {
        mNewsRecyclerAdapter.destroy();
        mCommentsRecyclerViewAdapter.destroy();
        mUserProfile.deleteGlobalBroadcastListener(mGlobalBroadcastListener);
        mYahooStxChartCrawler.destroy();
        mHandler.removeCallbacks(mExpandRunnable);
        mStockActivityRealPriceBlockViewHolder.mainView.removeOnLayoutChangeListener(mOnLayoutChangeListener);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }

        if(mStarAlertDialog != null) {
            mStarAlertDialog.dismiss();
            mStarAlertDialog = null;
        }

        if(mMoreAlertDialog != null) {
            mMoreAlertDialog.dismiss();
            mMoreAlertDialog = null;
        }
    }

    private void initSocialButtons() {

        ImageView buttonGoUp = findViewById(R.id.btn_go_up);
        buttonGoUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNestedScrollView.scrollTo(0, 0);
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

        final Button buttonWriteFirst = findViewById(R.id.btn_send_first);
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
                // Google the bug: NestedScrollView parameter must be a descendant of this view
                buttonWriteFirst.clearFocus();
            }
        });

        mGlobalBroadcastListener = new UserProfile.GlobalBroadcastListener() {
            @Override
            public void onGlobalBroadcast(int notifyId, Object payload) {
                if(notifyId == NOTIFY_USER_HAS_LOGIN && FBHelper.checkFBLogin()) {
                    switch (mLastClickedButton) {
                        case CLICK_COMMENT_BEFORE_LOGIN:
                            startActivityForResult(RichEditorActivity.generateRichEditorActivityIntent(
                                    StockActivity.this, RichEditorActivity.TYPE.STOCK, mCode),
                                    sEditorRequestCode);
                            overridePendingTransition(R.anim.enter, R.anim.stop);
                            return;
                        case CLICK_STAR_BEFORE_LOGIN:
                            changeFavorite(mStockActivityActionBarViewHolder.favoriteImageView);
                            break;
                        case CLICK_LIKE_BEFORE_LOGIN:
                            mCommentsRecyclerViewAdapter.updateCommentsLike();
                            MSLog.d("is like: " + mTempComment.isLiked());
                            if(!mTempComment.isLiked()) {
                                MSLog.d("say like at position: " + mTempPosition);
                                mTempComment.increaseLike();
                                mTempComment.setLike(true);
                                PostEvent.sendLike(StockActivity.this, mTempComment.getCommentId());
                                mCommentsRecyclerViewAdapter.notifyItemChanged(mTempPosition, ADAPTER_CHANGE_LIKE_ONLY);
                            }
                    }
                } else if(notifyId == NOTIFY_ID_FAVORITE_LIST) {
                    mIsFavorite = mUserProfile.isFavoriteStock(mCode);
                    if(mIsFavorite) {
                        mStockActivityActionBarViewHolder.favoriteImageView.setImageResource(R.mipmap.ic_fav_on);
                    } else {
                        mStockActivityActionBarViewHolder.favoriteImageView.setImageResource(R.mipmap.ic_fav_off);
                    }
                } else if(notifyId == NOTIFY_ID_NEWS_READ_RECORD_LIST) {
                    MSLog.d("update user's read news records");
                    mNewsRecyclerAdapter.notifyItemRangeChanged(0, mNewsRecyclerAdapter.getItemCount());
                }
            }
        };
        mUserProfile.addGlobalBroadcastListener(mGlobalBroadcastListener);
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
        mStockActivityBottomContent.stockActivityBottomSelector.commentSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseBottomBlock(0);
            }
        });
        mStockActivityBottomContent.stockActivityBottomSelector.newsSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseBottomBlock(1);
            }
        });
        mStockActivityRealPriceBlockViewHolder.stockActivityBottomSelector.commentSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseBottomBlock(0);
            }
        });
        mStockActivityRealPriceBlockViewHolder.stockActivityBottomSelector.newsSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseBottomBlock(1);
            }
        });

        mNestedScrollView = findViewById(R.id.body_scroll_view);

        mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                int height = view.getHeight();
                if(height != 0) {
                    if(height < mRealTimeContainerShrinkHeight) {
                        mRealTimeContainerShrinkHeight = view.getHeight();

                        Point childOffset = new Point();
                        MarketSenseUtils.getDeepChildOffset(mNestedScrollView,
                                mStockActivityBottomContent.stockActivityBottomSelector.newsSelector.getParent(),
                                mStockActivityBottomContent.stockActivityBottomSelector.newsSelector,
                                childOffset);
                        mSelectorTop = childOffset.y - mRealTimeContainerShrinkHeight;
                    }
                }
            }
        };

        mStockActivityRealPriceBlockViewHolder.mainView.addOnLayoutChangeListener(mOnLayoutChangeListener);

        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(!mIsTopShrink && scrollY > 50) {
                    mIsTopShrink = true;
                    mStockActivityRealPriceBlockViewHolder.shrink();
                }

                if(mIsTopShrink && scrollY < 100) {
                    mIsTopShrink = false;
                    mStockActivityRealPriceBlockViewHolder.expand();
                }

                if(!mIsSelectorFixed && scrollY >= mSelectorTop) {
                    mStockActivityRealPriceBlockViewHolder.showSelector();
                    mIsSelectorFixed = true;
                }

                if(mIsSelectorFixed && scrollY < mSelectorTop) {
                    mStockActivityRealPriceBlockViewHolder.hideSelector();
                    mIsSelectorFixed = false;
                }
            }
        });
    }

    private void slideToView(final View child) {
        mNestedScrollView.post(new Runnable() {
            @Override
            public void run() {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                Point childOffset = new Point();
                MarketSenseUtils.getDeepChildOffset(mNestedScrollView, child.getParent(), child, childOffset);
                childOffset.y = childOffset.y - (int)(mStockActivityRealPriceBlockViewHolder.mainView.getHeight() * metrics.density);
                mNestedScrollView.scrollTo(0, childOffset.y);
            }
        });
    }

    private void chooseBottomBlock(int position) {
        switch (position) {
            case 0:
                mStockActivityBottomContent.setSelected(this,
                        mStockActivityBottomContent.stockActivityBottomSelector.commentSelector,
                        mStockActivityBottomContent.stockActivityBottomSelector.commentUnderline,
                        mStockActivityBottomContent.commentBlock);
                break;
            case 1:
                mStockActivityBottomContent.setSelected(this,
                        mStockActivityBottomContent.stockActivityBottomSelector.newsSelector,
                        mStockActivityBottomContent.stockActivityBottomSelector.newsUnderline,
                        mStockActivityBottomContent.newsBlock);
                break;
        }
    }

    private void initCriticalStatistics() {
        mMoreAlertView = LayoutInflater.from(this)
                .inflate(R.layout.alertdialog_stock_more, null);
        ImageView cancelImageView = mMoreAlertView.findViewById(R.id.cancel_button);
        cancelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissMoreAlertDialog();
            }
        });
        mCriticalStatisticsRecyclerView = mMoreAlertView.findViewById(R.id.critical_statistics_list_view);

        mCriticalStatisticsAdapter = new CriticalStatisticsAdapter(this);
        mCriticalStatisticsRecyclerView.setAdapter(mCriticalStatisticsAdapter);
        mCriticalStatisticsRecyclerView.setNestedScrollingEnabled(false);
        mCriticalStatisticsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCriticalStatisticsAdapter.loadCriticalStatistics(
                        CriticalStatisticsRequest.getUrlStockCriticalStatistics(mCode));
    }

    private void showMoreAlertDialog() {
        dismissMoreAlertDialog();

        if(mMoreAlertView != null) {
            ViewGroup viewGroup = ((ViewGroup) mMoreAlertView.getParent());
            if (viewGroup != null) {
                viewGroup.removeView(mMoreAlertView);
            }
        }

        mMoreAlertDialog = new AlertDialog.Builder(this)
                .setView(mMoreAlertView)
                .create();
        mMoreAlertDialog.show();
        Window window = mMoreAlertDialog.getWindow();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if(window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, (int) (520 * metrics.density));
        }
    }

    private void dismissMoreAlertDialog() {
        if(mMoreAlertDialog != null) {
            mMoreAlertDialog.dismiss();
            mMoreAlertDialog = null;
        }
    }

    private void initNews() {
        Bundle bundle = new Bundle();
        bundle.putString(KEYWORD_NAME, mStockName);
        mNewsRecyclerView = findViewById(R.id.news_recycler_view);
        mNewsRecyclerAdapter = new NewsRecyclerAdapter(this, KEYWORD_TASK_ID, bundle, false);
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
                    mHandler.post(mExpandRunnable);
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
        ArrayList<String> networkUrls = new ArrayList<>();
        ArrayList<String> cacheUrls = new ArrayList<>();
        networkUrls.add(networkUrl);
        cacheUrls.add(cacheUrl);
        mNewsRecyclerAdapter.loadNews(networkUrls, cacheUrls);

        mNewsRecyclerAdapter.setOnItemClickListener(new NewsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(News news) {
                mNewsRecyclerAdapter.notifyNewsIsClicked(news);
                startActivity(NewsWebViewActivity.generateNewsWebViewActivityIntent(
                        StockActivity.this, news.getId(), news.getTitle(),
                        news.getUrlImage(), news.getDate(),
                        news.getPageLink(), news.getOriginLink(),
                        news.getVoteRaiseNum(), news.getVoteFallNum(),
                        news.getStockKeywords(), news.getExplicitKeywords(), news.getLevel()));
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
    }

    private void setNewsForEmptyData(boolean isEmpty) {
        ImageView noDataImageView = findViewById(R.id.no_news_iv);
        TextView noDataTextView = findViewById(R.id.no_news_tv);
        if(isEmpty) {
            mNewsRecyclerView.setVisibility(View.GONE);
            noDataTextView.setVisibility(View.VISIBLE);
            noDataImageView.setVisibility(View.VISIBLE);
            noDataTextView.setText(R.string.ops_something_wrong_with_no_refresh);
            noDataImageView.setImageResource(R.drawable.baseline_sentiment_dissatisfied_24px);
        } else {
            mNewsRecyclerView.setVisibility(View.VISIBLE);
            noDataTextView.setVisibility(View.GONE);
            noDataImageView.setVisibility(View.GONE);
        }
    }

    private void initComments() {
        mCommentRecyclerView = findViewById(R.id.marketsense_stock_comment_rv);

        mCommentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter(this, new CommentsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onSayLikeItemClick(Comment comment, int position) {
                mTempComment = comment;
                mTempPosition = position;
                if(FBHelper.checkFBLogin()) {
                    MSLog.d("say like at position: " + position);
                    comment.increaseLike();
                    comment.setLike(true);
                    PostEvent.sendLike(StockActivity.this, comment.getCommentId());
                    mCommentsRecyclerViewAdapter.notifyItemChanged(position, ADAPTER_CHANGE_LIKE_ONLY);
                } else {
                    showLoginAlertDialog(CLICK_LIKE_BEFORE_LOGIN);
                }
            }

            @Override
            public void onReplyItemClick(Comment comment, int position) {
                MSLog.d("reply at position: " + position);
                startActivityForResult(CommentActivity.generateCommentActivityIntent(
                        StockActivity.this, comment, position), sReplyEditorRequestCode);
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
        mCommentRecyclerView.setAdapter(mCommentsRecyclerViewAdapter);
        mCommentRecyclerView.setNestedScrollingEnabled(false);

        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentsRecyclerViewAdapter.setCommentsAvailableListener(new CommentsRecyclerViewAdapter.CommentsAvailableListener() {
            @Override
            public void onCommentsAvailable(CommentAndVote commentAndVote) {
                if(commentAndVote != null) {
                    if (commentAndVote.getCommentSize() > 0) {
                        showCommentBlock();
                    }
                    MSLog.d("update stock raise vote number: " + commentAndVote.getRaiseNumber());
                    MSLog.d("update stock fall vote number: " + commentAndVote.getFallNumber());
                }
            }
        });
        mCommentsRecyclerViewAdapter.loadCommentsList(CommentAndVoteRequest.querySingleNewsUrl(mCode, CommentAndVoteRequest.TASK.STOCK_COMMENT));
    }

    private void showCommentBlock() {
        findViewById(R.id.marketsense_stock_no_comment_iv).setVisibility(View.GONE);
        findViewById(R.id.marketsense_stock_no_comment_tv).setVisibility(View.GONE);
        findViewById(R.id.btn_send_first).setVisibility(View.GONE);
        mCommentRecyclerView.setVisibility(View.VISIBLE);
    }

    private void initStockPredictionBlock() {
        ClientData clientData = ClientData.getInstance(this);
        Stock stock = clientData.getPriceFromCode(mCode);

        if(stock != null) {
            mStockPredictionInfoBlockViewHolder.render(this, stock);
        }
    }

    private void initStockChart() {

        mLoadingProgressBar = findViewById(R.id.loading_progress_bar_1);
        mSwipeRefreshLayout = findViewById(R.id.swipe_to_refresh);

        updatePrice(mPrice, mDiffNum, mDiffPercentage, null);

        mLineChart = findViewById(R.id.stock_chart_price);
        mBarChart = findViewById(R.id.stock_chart_volume);
        mCandleStickChart = findViewById(R.id.stock_candle_chart_price);
        mYahooStxChartCrawler =
                new YahooStxChartCrawler(this, mStockName, mCode, mLineChart, mBarChart, mCandleStickChart);

        mYahooStxChartCrawler.setInformationTextView(
                mChartTaTopItemsViewHolder,
                mChartTickBottomItemsViewHolder);
        mYahooStxChartCrawler.setYahooStxChartListener(new YahooStxChartCrawler.YahooStxChartListener() {
            @Override
            public void onStxChartDataLoad() {
                mYahooStxChartCrawler.renderStockChartData();
                StockTradeData stockTradeData = mYahooStxChartCrawler.getStockTradeData();
                if(stockTradeData != null) {
                    updatePrice(stockTradeData.getRealPrice(),
                            stockTradeData.getDiffPrice(),
                            stockTradeData.getDiffPercentage(),
                            stockTradeData.getTickTotalVolume());
                    if(stockTradeData.getFiveBestPrice() != null) {
                        updateFiveBestPrice(stockTradeData.getFiveBestPrice(), stockTradeData.getYesterdayPrice());
                    }
                }

                if(mSelectedChartTextView == mChartPeriodSelectorViewHolder.chartType1M) {
                    showTickBlock();
                } else {
                    showTaBlock();
                }

                mSwipeRefreshLayout.setRefreshing(false);
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onStxChartDataFail(MarketSenseError marketSenseError) {
                mYahooStxChartCrawler.renderStockChartData();
                mSwipeRefreshLayout.setRefreshing(false);
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                }
            }
        });
        mYahooStxChartCrawler.loadStockChartData();

        mChartPeriodSelectorViewHolder.chartType1M.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeChartSelectorUI(mChartPeriodSelectorViewHolder.chartType1M,
                        mChartPeriodSelectorViewHolder.underline1M);
                mYahooStxChartCrawler.loadStockChartData();
            }
        });

        mChartPeriodSelectorViewHolder.chartTypeD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeChartSelectorUI(mChartPeriodSelectorViewHolder.chartTypeD,
                        mChartPeriodSelectorViewHolder.underlineD);
                mYahooStxChartCrawler.loadTaStockChartData(StockChartDataRequest.TA_TYPE_DAY);
            }
        });

        mChartPeriodSelectorViewHolder.chartTypeW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeChartSelectorUI(mChartPeriodSelectorViewHolder.chartTypeW,
                        mChartPeriodSelectorViewHolder.underlineW);
                mYahooStxChartCrawler.loadTaStockChartData(StockChartDataRequest.TA_TYPE_WEEK);
            }
        });

        mChartPeriodSelectorViewHolder.chartTypeM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeChartSelectorUI(mChartPeriodSelectorViewHolder.chartTypeM,
                        mChartPeriodSelectorViewHolder.underlineM);
                mYahooStxChartCrawler.loadTaStockChartData(StockChartDataRequest.TA_TYPE_MONTH);
            }
        });

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        int start = (int) (100 * metrics.density);
        int end = (int) (130 * metrics.density);
        mSelectedChartTextView = mChartPeriodSelectorViewHolder.chartType1M;
        mSwipeRefreshLayout.setProgressViewOffset(false, start, end);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mSelectedChartTextView != null) {
                    mSelectedChartTextView.performClick();
                }
            }
        });
    }

    private void updatePrice(String price, String diffNum, String diffPercentage, String volume) {
        StockActivityRealPriceBlockViewHolder.update(this, mStockActivityRealPriceBlockViewHolder, price, diffNum, diffPercentage);
        StockActivityActionBarViewHolder.update(this, mStockActivityActionBarViewHolder, diffNum);
        ChartTickTopItemsViewHolder.update(this, mChartTickTopItemsViewHolder, volume);
    }

    private void updatePrice(float price, float diffNum , float diffPercentage, String volume) {
        StockActivityRealPriceBlockViewHolder.update(this, mStockActivityRealPriceBlockViewHolder, price, diffNum, diffPercentage);
        StockActivityActionBarViewHolder.update(this, mStockActivityActionBarViewHolder, diffNum);
        ChartTickTopItemsViewHolder.update(this, mChartTickTopItemsViewHolder, volume);
    }

    private void updateFiveBestPrice(StockTradeData.BestPriceRow[] bestPriceRows, float yesterdayPrice) {
        FiveBestPriceViewHolder.update(mBestPriceRowViewHolder, bestPriceRows, yesterdayPrice);
    }

    private void changeChartSelectorUI(TextView selected, View underlineView) {
        mChartPeriodSelectorViewHolder.setSelected(this, selected, underlineView);
        mSelectedChartTextView = selected;
        if(mLoadingProgressBar != null) {
            mLoadingProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void showTickBlock() {
        mSwipeRefreshLayout.setEnabled(true);
        mChartTickTopItemsViewHolder.mainView.setVisibility(View.VISIBLE);
        mChartTaTopItemsViewHolder.mainView.setVisibility(View.GONE);

        mChartTickBottomItemsViewHolder.showSecondRow();
    }

    private void showTaBlock() {
        mSwipeRefreshLayout.setEnabled(false);
        mChartTickTopItemsViewHolder.mainView.setVisibility(View.GONE);
        mChartTaTopItemsViewHolder.mainView.setVisibility(View.VISIBLE);

        mChartTickBottomItemsViewHolder.hideSecondRow();
    }

    private void setInformation() {
        if(getIntent().getStringExtra(EXTRA_CODE) != null) {
            MSLog.d("set information in normal stock list click");
            mStockName = getIntent().getStringExtra(Intent.EXTRA_TITLE);
            mCode = getIntent().getStringExtra(EXTRA_CODE);

            mIsFavorite = mUserProfile.isFavoriteStock(mCode);

            mPrice = getIntent().getStringExtra(EXTRA_PRICE);
            mDiffNum = getIntent().getStringExtra(EXTRA_DIFF_NUM);
            mDiffPercentage = getIntent().getStringExtra(EXTRA_DIFF_PERCENTAGE);
        } else {
            Intent intent = getIntent();
            String action = intent.getAction();
            Uri data = intent.getData();
            Stock stock = null;
            if(data != null) {
                MSLog.d("set information in comment's deep link: " + action + ", uri: " + data);

                List<String> pathSegments = data.getPathSegments();
                int index = pathSegments.indexOf("code");
                try {
                    stock = ClientData.getInstance(this).getPriceFromCode(pathSegments.get(index + 1));
                } catch (Exception e) {
                    MSLog.e("deep link parse error: " + data);
                    stock = ClientData.getInstance(this).getPriceFromCode("2330");
                }

                mStockName = stock.getName();
                mCode = stock.getCode();

                mIsFavorite = mUserProfile.isFavoriteStock(mCode);

                mPrice = stock.getPrice();
                mDiffNum = stock.getDiffNumber();
                mDiffPercentage = stock.getDiffPercentage();
            }
        }
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setElevation(0);
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_stock_activity, null);

            mStockActivityActionBarViewHolder = StockActivityActionBarViewHolder.convertToViewHolder(view, actionBar);

            mStockActivityActionBarViewHolder.backImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

            String format = getResources().getString(R.string.title_company_name_code_format_no_decoration);
            mStockActivityActionBarViewHolder.titleTextView.setText(String.format(format, mStockName, mCode));

            mStockActivityActionBarViewHolder.favoriteImageView.setVisibility(View.VISIBLE);
            if(mIsFavorite) {
                mStockActivityActionBarViewHolder.favoriteImageView.setImageResource(R.mipmap.ic_fav_on);
            } else {
                mStockActivityActionBarViewHolder.favoriteImageView.setImageResource(R.mipmap.ic_fav_off);
            }
            mStockActivityActionBarViewHolder.favoriteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(FBHelper.checkFBLogin()) {
                        changeFavorite(mStockActivityActionBarViewHolder.favoriteImageView);
                    } else {
                        showLoginAlertDialog(CLICK_STAR_BEFORE_LOGIN);
                    }
                }
            });
            mStockActivityActionBarViewHolder.moreImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showMoreAlertDialog();
                }
            });

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
            imageView.setImageResource(R.mipmap.ic_fav_off);
        } else {
            mUserProfile.addFavoriteStock(mCode);
            PostEvent.sendFavoriteStocksAdd(this, mCode);
            String format = getResources().getString(R.string.title_add_complete);
            Toast.makeText(this, String.format(format, mStockName, mCode), Toast.LENGTH_SHORT).show();
            imageView.setImageResource(R.mipmap.ic_fav_on);

            if(mUserProfile.canShowStarDialog(this)) {
                showStarAlertDialog();
            }
        }
        mIsFavorite = mUserProfile.isFavoriteStock(mCode);
        mUserProfile.globalBroadcast(NOTIFY_ID_FAVORITE_LIST);
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
                        UserProfile.generatePassword(userId, FACEBOOK_CONSTANTS), userEmail, avatarLink, new PostEvent.PostEventListener() {
                            @Override
                            public void onResponse(boolean isSuccessful, Object data) {
                                if(!isSuccessful) {
                                    Toast.makeText(StockActivity.this, R.string.login_failed_description, Toast.LENGTH_SHORT).show();
                                    LoginManager.getInstance().logOut();
                                } else {
                                    mUserProfile.globalBroadcast(NOTIFY_USER_HAS_LOGIN);
                                }
                            }
                        });
            }
        });
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

                chooseBottomBlock(0);
                slideToView(mStockActivityBottomContent.stockActivityBottomSelector.commentSelector);

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
                    mCommentsRecyclerViewAdapter.notifyItemChanged(position, ADAPTER_CHANGE_LIKE_ONLY);
                }
            }
        }
        mFBCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showStarAlertDialog() {
        if(mStarAlertDialog != null) {
            mStarAlertDialog.dismiss();
            mStarAlertDialog = null;
        }

        mStarAlertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.star_title)
                .setMessage(R.string.star_description_simple)
                .setPositiveButton(R.string.star_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            MSLog.d("go to: " + Uri.parse("market://details?id=" + appPackageName));
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException e) {
                            MSLog.d("go to: " + Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        mStarAlertDialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.star_negative_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mStarAlertDialog.dismiss();
                    }
                })
                .show();
    }
}
