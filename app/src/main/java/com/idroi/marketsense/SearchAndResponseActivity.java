package com.idroi.marketsense;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.adapter.StockListRecyclerViewAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.data.Stock;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.request.NewsRequest;

import java.util.ArrayList;

import static com.idroi.marketsense.adapter.NewsRecyclerAdapter.NEWS_SINGLE_LAYOUT;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_NEWS_READ_RECORD_LIST;
import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_TASK_ID;

/**
 * Created by daniel.hsieh on 2018/4/27.
 */

public class SearchAndResponseActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_COMPANY_NAME_KEY = "extra_selected_company_name";
    public static final String EXTRA_SELECTED_COMPANY_CODE_KEY = "extra_selected_company_code";
    public static final String EXTRA_SEARCH_TYPE = "extra_search_type";

    public static final int SEARCH_BOTH = 1;
    public static final int SEARCH_CODE_ONLY = 2;

    private EditText mSearchView;
    private RecyclerView mStockResultRecyclerView, mNewsResultRecyclerView;
    private StockListRecyclerViewAdapter mStockRecyclerAdapter;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;
    private TextView mStockResultsTextView, mNewsResultsTextView, mSearchStatusTextView;

    private String mQueryString;
    private int mSearchType;

    private UserProfile.GlobalBroadcastListener mGlobalBroadcastListener;

    private  ArrayList<Stock> mAllStocks = ClientData.getInstance(this).getAllStocksListInfo();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrescoHelper.initialize(getApplicationContext());
        setContentView(R.layout.activity_search);

        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        setInformation();
        setResultsLayout();
        setActionBar();

        mGlobalBroadcastListener = new UserProfile.GlobalBroadcastListener() {
            @Override
            public void onGlobalBroadcast(int notifyId, Object payload) {
                if(notifyId == NOTIFY_ID_NEWS_READ_RECORD_LIST) {
                    MSLog.d("update user's read news records");
                    mNewsRecyclerAdapter.notifyItemRangeChanged(0, mNewsRecyclerAdapter.getItemCount());
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
            actionBar.setBackgroundDrawable(getDrawable(R.drawable.action_bar_background_with_border));
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_search, null);

            mSearchView = view.findViewById(R.id.search_text);
            if(mSearchView != null) {
                mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                        if(actionId == EditorInfo.IME_ACTION_DONE) {
                            filter(mSearchView.getText().toString());
                        }
                        return true;
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

    private void setResultsLayout() {
        mSearchStatusTextView = findViewById(R.id.tv_search_status);
        mStockResultsTextView = findViewById(R.id.tv_search_result_stock);
        mNewsResultsTextView = findViewById(R.id.tv_search_result_news);

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
                        news.getVoteRaiseNum(), news.getVoteFallNum(), news.getStockKeywords(), news.getLevel()));
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
        mNewsRecyclerAdapter.setNewsAvailableListener(new NewsRecyclerAdapter.NewsAvailableListener() {
            @Override
            public void onNewsAvailable() {
                adjustUIForEndSearching(true, mQueryString);
            }

            @Override
            public void onNewsEmpty() {
                adjustUIForEndSearching(false, mQueryString);
            }
        });

        mNewsResultRecyclerView = findViewById(R.id.search_news_result_list);
        mNewsResultRecyclerView.setAdapter(mNewsRecyclerAdapter);
        mNewsResultRecyclerView.setNestedScrollingEnabled(false);
        mNewsResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mStockRecyclerAdapter = new StockListRecyclerViewAdapter(this,
                ClientData.getInstance(this).getAllStocksListInfo());
        mStockRecyclerAdapter.setOnClickListener(new StockListRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Stock stock) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_SELECTED_COMPANY_NAME_KEY, stock.getName());
                intent.putExtra(EXTRA_SELECTED_COMPANY_CODE_KEY, stock.getCode());
                setResult(RESULT_OK, intent);

                mSearchView.clearFocus();
                hideSoftKeyboard();

                finish();
                overridePendingTransition(0, 0);
            }
        });

        mStockResultRecyclerView = findViewById(R.id.search_stock_result_list);
        mStockResultRecyclerView.setAdapter(mStockRecyclerAdapter);
        mStockResultRecyclerView.setNestedScrollingEnabled(false);
        mStockResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        resetSearchState();
    }

    private void adjustUIForStartSearching(String query) {
        mStockResultsTextView.setVisibility(View.GONE);
        mStockResultRecyclerView.setVisibility(View.GONE);
        mNewsResultRecyclerView.setVisibility(View.GONE);
        mNewsResultsTextView.setVisibility(View.GONE);

        mSearchStatusTextView.setVisibility(View.VISIBLE);
        String text = String.format(getResources().getString(R.string.title_search_processing), query);
        mSearchStatusTextView.setText(text);
    }

    private void adjustUIForEndSearching(boolean hasNewsContent, String query) {
        boolean isAllEmpty = true;
        if(mStockRecyclerAdapter.getItemCount() > 0) {
            mStockResultsTextView.setVisibility(View.VISIBLE);
            mStockResultRecyclerView.setVisibility(View.VISIBLE);
            isAllEmpty = false;
        }
        if(hasNewsContent) {
            mNewsResultRecyclerView.setVisibility(View.VISIBLE);
            mNewsResultsTextView.setVisibility(View.VISIBLE);
            isAllEmpty = false;
        }
        if(isAllEmpty) {
            mSearchStatusTextView.setVisibility(View.VISIBLE);
            String text = String.format(getResources().getString(R.string.title_search_no_results), query);
            mSearchStatusTextView.setText(text);
        } else {
            mSearchStatusTextView.setVisibility(View.GONE);
        }
    }

    private void filter(String text) {

        MSLog.d("search stock: " + text);
        mQueryString = text;
        adjustUIForStartSearching(mQueryString);
        ArrayList<String> newsQueryStrings = new ArrayList<>();
        // stock search part
        ArrayList<Stock> filterStocks = new ArrayList<>();

        if(mAllStocks != null) {
            for (Stock stock : mAllStocks) {
                if(stock.getCode().equals(text) || stock.getName().equals(text)) {
                    filterStocks.add(stock);
                    newsQueryStrings.add(stock.getName());
                }
            }
        }
        mStockRecyclerAdapter.filterList(filterStocks);

        if(mSearchType == SEARCH_CODE_ONLY) {
            adjustUIForEndSearching(false, mQueryString);
            hideSoftKeyboard();
            return;
        }

        // news search part
        if(newsQueryStrings.size() > 0) {
            ArrayList<String> networkUrls = new ArrayList<>();
            ArrayList<String> cacheUrls = new ArrayList<>();
            for (String name : newsQueryStrings) {
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
        } else {
            adjustUIForEndSearching(false, mQueryString);
        }

        hideSoftKeyboard();
    }

    private void resetSearchState() {
        if(mStockResultRecyclerView != null) {
            mStockResultRecyclerView.scrollToPosition(0);
        }
        if (mSearchView != null) {
            mSearchView.clearFocus();
            mSearchView.setText("");
        }
        hideSoftKeyboard();
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            View v = getCurrentFocus();
            if (v != null) {
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        if(mNewsRecyclerAdapter != null) {
            mNewsRecyclerAdapter.destroy();
        }
        UserProfile userProfile = ClientData.getInstance(this).getUserProfile();
        if(userProfile != null) {
            userProfile.deleteGlobalBroadcastListener(mGlobalBroadcastListener);
        }
        super.onDestroy();
    }
}
