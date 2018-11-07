package com.idroi.marketsense;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.login.widget.LoginButton;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.adapter.StockListRecyclerAdapter;
import com.idroi.marketsense.adapter.StockListRecyclerViewAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.CommentAndVoteRequest;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.util.MarketSenseUtils;
import com.idroi.marketsense.viewholders.SearchSelectorViewHolder;

import java.io.Serializable;
import java.util.ArrayList;

import static com.idroi.marketsense.CommentActivity.EXTRA_COMMENT;
import static com.idroi.marketsense.CommentActivity.EXTRA_NEED_TO_CHANGE;
import static com.idroi.marketsense.CommentActivity.EXTRA_POSITION;
import static com.idroi.marketsense.RichEditorActivity.sReplyEditorRequestCode;
import static com.idroi.marketsense.adapter.CommentsRecyclerViewAdapter.ADAPTER_CHANGE_LIKE_ONLY;
import static com.idroi.marketsense.adapter.NewsRecyclerAdapter.NEWS_SINGLE_LAYOUT;
import static com.idroi.marketsense.adapter.StockListRecyclerAdapter.ADAPTER_UPDATE_PRICE_ONLY;
import static com.idroi.marketsense.adapter.StockListRecyclerAdapter.ADAPTER_UPDATE_RIGHT_BLOCK_ONLY;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_DISCUSSION_COMMENT_CLICK;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_FAVORITE_LIST;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_NEWS_READ_RECORD_LIST;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_PRICE_CHANGED;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_RIGHT_PART_CHANGE;
import static com.idroi.marketsense.datasource.StockListPlacer.SORT_BY_PREDICTION;
import static com.idroi.marketsense.datasource.StockListPlacer.SORT_DOWNWARD;
import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_TASK_ID;
import static com.idroi.marketsense.fragments.StockListFragment.NORMAL_ID;

/**
 * Created by daniel.hsieh on 2018/11/5.
 */

public class SearchAndResponseActivity extends AppCompatActivity {

    public static final int LAST_CLICK_IS_LIKE = 1;

    public static final String EXTRA_SELECTED_COMPANY_NAME_KEY = "extra_selected_company_name";
    public static final String EXTRA_SELECTED_COMPANY_CODE_KEY = "extra_selected_company_code";
    public static final String EXTRA_SEARCH_TYPE = "extra_search_type";

    public static final int SEARCH_BOTH = 1;
    public static final int SEARCH_CODE_ONLY = 2;

    private EditText mSearchView;
    private ImageButton mSearchCancelButton;

    private RecyclerView mSuggestionRecyclerView;
    private StockListRecyclerViewAdapter mSuggestionAdapter;

    private SearchSelectorViewHolder mSearchSelectorViewHolder;
    private RecyclerView mStockRecyclerView, mNewsRecyclerView, mCommentRecyclerView;
    private StockListRecyclerAdapter mStockAdapter;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;
    private CommentsRecyclerViewAdapter mCommentsRecyclerViewAdapter;

    private boolean mIsRecyclerViewIdle = true;

    private ArrayList<Stock> mAllStocks = ClientData.getInstance(this).getAllStocksListInfo();
    private ArrayList<Stock> mSuggestionStockList = new ArrayList<>();

    private int mSearchType;

    private UserProfile.GlobalBroadcastListener mGlobalBroadcastListener;

    private int mLastClickAction;
    private Comment mTempComment;
    private int mTempPosition;
    private AlertDialog mLoginAlertDialog;
    private LoginButton mFBLoginBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrescoHelper.initialize(getApplicationContext());
        setContentView(R.layout.activity_search_v2);

        final UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        setInformation();
        initSuggestionLayout();
        intiResultsLayout();
        setActionBar();

        mGlobalBroadcastListener = new UserProfile.GlobalBroadcastListener() {
            @Override
            public void onGlobalBroadcast(int notifyId, Object payload) {
                if(notifyId == NOTIFY_ID_NEWS_READ_RECORD_LIST) {
                    MSLog.d("update user's read news records");
                    mNewsRecyclerAdapter.notifyItemRangeChanged(0, mNewsRecyclerAdapter.getItemCount());
                } else if(notifyId == NOTIFY_ID_PRICE_CHANGED && mIsRecyclerViewIdle) {
                    mStockAdapter.updatePriceInVisibleItems(ADAPTER_UPDATE_PRICE_ONLY);
                } else if(notifyId == NOTIFY_ID_RIGHT_PART_CHANGE) {
                    mStockAdapter.updatePriceInVisibleItems(ADAPTER_UPDATE_RIGHT_BLOCK_ONLY);
                } else if(notifyId == NOTIFY_ID_DISCUSSION_COMMENT_CLICK && FBHelper.checkFBLogin()) {
                    if(mLastClickAction == LAST_CLICK_IS_LIKE) {
                        mCommentsRecyclerViewAdapter.updateCommentsLike();
                        if(mTempComment != null) {
                            MSLog.d("is like: " + mTempComment.isLiked());
                            if(!mTempComment.isLiked()) {
                                MSLog.d("say like at position: " + mTempPosition);
                                mTempComment.increaseLike();
                                mTempComment.setLike(true);
                                PostEvent.sendLike(SearchAndResponseActivity.this, mTempComment.getCommentId());
                                mCommentsRecyclerViewAdapter.notifyItemChanged(mTempPosition, ADAPTER_CHANGE_LIKE_ONLY);
                                mCommentsRecyclerViewAdapter.removeCommentGeneralCache(SearchAndResponseActivity.this);
                                mTempComment = null;
                            }
                        }
                    }
                } else if(notifyId == NOTIFY_ID_FAVORITE_LIST) {
                    if(mTempComment != null) {
                        userProfile.globalBroadcast(NOTIFY_ID_DISCUSSION_COMMENT_CLICK);
                    } else {
                        mCommentsRecyclerViewAdapter.updateCommentsLike();
                    }
                }
            }
        };

        if(userProfile != null) {
            userProfile.addGlobalBroadcastListener(mGlobalBroadcastListener);
        }
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setElevation(0);
            actionBar.setBackgroundDrawable(new ColorDrawable(0xfbfbfb));
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_search, null);

            mSearchView = view.findViewById(R.id.search_text);
            if(mSearchView != null) {
                mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                        if(actionId == EditorInfo.IME_ACTION_DONE) {
                            filterSuggestion(mSearchView.getText().toString());
                            MarketSenseUtils.hideSoftKeyboard(SearchAndResponseActivity.this);

                            if(mSearchType == SEARCH_BOTH) {
                                openResultPage();
                            }
                        }
                        return true;
                    }
                });

                mSearchView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        if(charSequence.length() == 0) {
                            hideClearEditableButton();
                        } else {
                            showClearEditableButton();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        filterSuggestion(editable.toString());
                        updateSuggestionAdapter();
                    }
                });

                mSearchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if(hasFocus) {
                            openSuggestionPage();
                        }
                    }
                });
                mSearchView.requestFocus();
            }

            mSearchCancelButton = view.findViewById(R.id.search_cancel);
            if(mSearchCancelButton != null) {
                mSearchCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resetSearchState();
                    }
                });
            }

            ImageButton goBackImageButton = view.findViewById(R.id.go_back);
            if(goBackImageButton != null) {
                goBackImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
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

    private void setInformation() {
        mSearchType = getIntent().getIntExtra(EXTRA_SEARCH_TYPE, SEARCH_BOTH);
    }

    private void initSuggestionLayout() {
        mSuggestionAdapter = new StockListRecyclerViewAdapter(this);
        mSuggestionAdapter.setOnClickListener(new StockListRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Stock stock) {
                if(mSearchType == SEARCH_CODE_ONLY) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_SELECTED_COMPANY_NAME_KEY, stock.getName());
                    intent.putExtra(EXTRA_SELECTED_COMPANY_CODE_KEY, stock.getCode());
                    setResult(RESULT_OK, intent);

                    mSearchView.clearFocus();
                    MarketSenseUtils.hideSoftKeyboard(SearchAndResponseActivity.this);

                    finish();
                    overridePendingTransition(0, 0);
                    return;
                }

                filterSuggestion(stock.getName());
                openResultPage();
                MarketSenseUtils.hideSoftKeyboard(SearchAndResponseActivity.this);
            }
        });
        mSuggestionRecyclerView = findViewById(R.id.suggestion_recycler_view);
        mSuggestionRecyclerView.setAdapter(mSuggestionAdapter);
        mSuggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        resetSearchState();
    }

    private void intiResultsLayout() {

        // stock part
        mStockRecyclerView = findViewById(R.id.stock_recycler_view);
        mStockAdapter = new StockListRecyclerAdapter(this, NORMAL_ID, SORT_BY_PREDICTION, SORT_DOWNWARD);
        mStockRecyclerView.setAdapter(mStockAdapter);
        mStockRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mStockAdapter.setOnItemClickListener(new StockListRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Stock stock) {
                startActivity(StockActivity.generateStockActivityIntent(
                        SearchAndResponseActivity.this, stock.getName(), stock.getCode(),
                        stock.getRaiseNum(), stock.getFallNum(),
                        stock.getPrice(), stock.getDiffNumber(), stock.getDiffPercentage()));
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });

        // news part
        mNewsRecyclerView = findViewById(R.id.news_recycler_view);
        mNewsRecyclerAdapter = new NewsRecyclerAdapter(this, KEYWORD_TASK_ID, null);
        mNewsRecyclerAdapter.setNewsLayoutType(NEWS_SINGLE_LAYOUT);
        mNewsRecyclerAdapter.setOnItemClickListener(new NewsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(News news) {
                mNewsRecyclerAdapter.notifyNewsIsClicked(news);
                startActivity(NewsWebViewActivity.generateNewsWebViewActivityIntent(
                        SearchAndResponseActivity.this, news.getId(), news.getTitle(),
                        news.getUrlImage(), news.getDate(),
                        news.getPageLink(), news.getOriginLink(),
                        news.getVoteRaiseNum(), news.getVoteFallNum(),
                        news.getStockKeywords(), news.getExplicitKeywords(), news.getLevel()));
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
        mNewsRecyclerView.setAdapter(mNewsRecyclerAdapter);
        mNewsRecyclerView.setNestedScrollingEnabled(false);
        mNewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // comment part
        mCommentRecyclerView = findViewById(R.id.comment_recycler_view);
        mCommentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter(this, true,
                new CommentsRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onSayLikeItemClick(Comment comment, int position) {
                        if (FBHelper.checkFBLogin()) {
                            MSLog.d("say like at position: " + position);
                            comment.increaseLike();
                            comment.setLike(true);
                            PostEvent.sendLike(SearchAndResponseActivity.this, comment.getCommentId());
                            mCommentsRecyclerViewAdapter.notifyItemChanged(position, ADAPTER_CHANGE_LIKE_ONLY);
                            mCommentsRecyclerViewAdapter.removeCommentGeneralCache(SearchAndResponseActivity.this);
                        } else {
                            mTempComment = comment;
                            mTempPosition = position;
                            showLoginAlertDialog(LAST_CLICK_IS_LIKE);
                        }
                    }

                    @Override
                    public void onReplyItemClick(Comment comment, int position) {
                        startActivityForResult(CommentActivity.generateCommentActivityIntent(
                                SearchAndResponseActivity.this, comment, position), sReplyEditorRequestCode);
                        overridePendingTransition(R.anim.enter, R.anim.stop);
                    }
                }, new CommentsRecyclerViewAdapter.OnNewsItemClickListener() {
            @Override
            public void onNewsItemClick(News news) {
                startActivity(NewsWebViewActivity.generateNewsWebViewActivityIntent(
                        SearchAndResponseActivity.this, news));
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
        mCommentRecyclerView.setAdapter(mCommentsRecyclerViewAdapter);
        mCommentRecyclerView.setNestedScrollingEnabled(false);
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSearchSelectorViewHolder = SearchSelectorViewHolder
                .convertToViewHolder(findViewById(R.id.search_result_block),
                        mStockRecyclerView, mNewsRecyclerView, mCommentRecyclerView);
    }

    private void filterSuggestion(String text) {
        mSuggestionStockList.clear();

        if(mAllStocks != null) {
            for(Stock stock : mAllStocks) {
                if(stock.getCode().contains(text) || stock.getName().contains(text)) {
                    mSuggestionStockList.add(stock);
                }
            }
        }
    }

    private void updateSuggestionAdapter() {
        mSuggestionAdapter.filterList(mSuggestionStockList);
    }

    private void openResultPage() {
        hideClearEditableButton();
        mSearchView.clearFocus();

        // stock part
        mStockAdapter.setStockArrayList(mSuggestionStockList);

        // news part
        if(mSuggestionStockList.size() > 0) {
            ArrayList<String> networkUrls = new ArrayList<>();
            ArrayList<String> cacheUrls = new ArrayList<>();
            for (Stock stock : mSuggestionStockList) {
                String name = stock.getName();
                String networkUrl = NewsRequest.queryKeywordNewsUrl(this, name, true);
                if (networkUrl == null) {
                    return;
                }
                MSLog.d("search news: " + name);
                String cacheUrl = NewsRequest.queryKeywordNewsUrl(this, name, false);
                networkUrls.add(networkUrl);
                cacheUrls.add(cacheUrl);
            }
            if (networkUrls.size() > 0) {
                MSLog.d("search news size: " + networkUrls.size());
                mNewsRecyclerAdapter.loadNews(networkUrls, cacheUrls);
            }
        }

        // comment part
        mCommentsRecyclerViewAdapter.loadCommentsList(
                CommentAndVoteRequest.queryCommentsEventForStockCode(mSuggestionStockList.get(0).getCode()));

        mSuggestionRecyclerView.setVisibility(View.GONE);
        mSearchSelectorViewHolder.show();
    }

    private void openSuggestionPage() {
        mSuggestionRecyclerView.setVisibility(View.VISIBLE);
        mSearchSelectorViewHolder.hide();
    }

    private void resetSearchState() {
        if (mSearchView != null) {
            mSearchView.clearFocus();
            mSearchView.setText("");
        }
        MarketSenseUtils.hideSoftKeyboard(this);
    }

    private void hideClearEditableButton() {
        if(mSearchCancelButton != null) {
            mSearchCancelButton.setVisibility(View.GONE);
        }
    }

    private void showClearEditableButton() {
        if(mSearchCancelButton != null) {
            mSearchCancelButton.setVisibility(View.VISIBLE);
        }
    }

    private void showLoginAlertDialog(int lastAction) {
        mLastClickAction = lastAction;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == sReplyEditorRequestCode) {
            if(resultCode == RESULT_OK && data.getBooleanExtra(EXTRA_NEED_TO_CHANGE, false)) {
                Serializable serializable = data.getSerializableExtra(EXTRA_COMMENT);
                int position = data.getIntExtra(EXTRA_POSITION, -1);
                if (serializable != null && serializable instanceof Comment && position != -1) {
                    MSLog.d("comment with position " + position + " is needed to change");
                    Comment comment = (Comment) serializable;
                    mCommentsRecyclerViewAdapter.cloneSocialContent(position, comment);
                    mCommentsRecyclerViewAdapter.notifyItemChanged(position, ADAPTER_CHANGE_LIKE_ONLY);
                    mCommentsRecyclerViewAdapter.removeCommentGeneralCache(this);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        if(userProfile != null) {
            userProfile.deleteGlobalBroadcastListener(mGlobalBroadcastListener);
        }
        if(mStockAdapter != null) {
            mStockAdapter.destroy();
            mStockAdapter = null;
        }
        if(mNewsRecyclerAdapter != null) {
            mNewsRecyclerAdapter.destroy();
            mNewsRecyclerAdapter = null;
        }
        if(mSuggestionAdapter != null) {
            mSuggestionAdapter.destroy();
            mSuggestionAdapter = null;
        }
        if(mLoginAlertDialog != null) {
            mLoginAlertDialog.dismiss();
            mLoginAlertDialog = null;
        }
        super.onDestroy();
    }
}
