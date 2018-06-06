package com.idroi.marketsense;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.idroi.marketsense.adapter.StockListRecyclerViewAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FrescoHelper;
import com.idroi.marketsense.data.Stock;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/4/27.
 */

public class SearchAndResponseActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_COMPANY_NAME_KEY = "extra_selected_company_name";
    public static final String EXTRA_SELECTED_COMPANY_CODE_KEY = "extra_selected_company_code";

    private EditText mSearchView;
    private ImageButton mSearchCancelButton;
    private TextView mResultTextView;
    private RecyclerView mResultRecyclerView;
    StockListRecyclerViewAdapter mAdapter;

    private final ArrayList<Stock> mAllStocks = ClientData.getInstance(this).getAllStocksListInfo();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrescoHelper.initialize(getApplicationContext());
        setContentView(R.layout.activity_search);

        ClientData.getInstance(this);
        setResultsLayout();
        setActionBar();
    }

    private void setActionBar() {
        final ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            View view = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.action_bar_search, null);

            mSearchView = view.findViewById(R.id.search_text);
            if(mSearchView != null) {
                mSearchView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        if(count == 0) {
                            if(mSearchCancelButton != null) {
                                mSearchCancelButton.setVisibility(View.GONE);
                            }
                        } else {
                            if(mSearchCancelButton != null) {
                                mSearchCancelButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        filter(editable.toString());
                    }
                });
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

            TextView cancelSearchTextView = view.findViewById(R.id.cancel);
            cancelSearchTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
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

    private void setResultsLayout() {

        mAdapter = new StockListRecyclerViewAdapter(this,
                ClientData.getInstance(this).getAllStocksListInfo());
        mAdapter.setOnClickListener(new StockListRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Stock stock) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_SELECTED_COMPANY_NAME_KEY, stock.getName());
                intent.putExtra(EXTRA_SELECTED_COMPANY_CODE_KEY, stock.getCode());
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        mResultTextView = findViewById(R.id.tv_search_result);
        mResultRecyclerView = findViewById(R.id.search_result_list);
        mResultRecyclerView.setAdapter(mAdapter);
        mResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        resetSearchState();
    }

    private void filter(String text) {
        ArrayList<Stock> filterStocks = new ArrayList<>();

        if(mAllStocks != null) {
            for (Stock stock : mAllStocks) {
                if(stock.getCode().contains(text) || stock.getName().contains(text)) {
                    filterStocks.add(stock);
                }
            }
        }
        setResultNumber(filterStocks.size());
        mAdapter.filterList(filterStocks);
    }

    private void setResultNumber(int number) {
        if(mResultTextView != null) {
            String format = getResources().getString(R.string.title_search_result);
            mResultTextView.setText(String.format(format, number));
        }
    }

    private void resetSearchState() {
        if(mResultRecyclerView != null) {
            mResultRecyclerView.scrollToPosition(0);
        }
        if (mSearchView != null) {
            mSearchView.clearFocus();
            mSearchView.setText("");
        }
        setResultNumber(mAllStocks.size());
        mAdapter.filterList(mAllStocks);
        hideSoftKeyboard();
    }

    private  void hideSoftKeyboard() {
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
}
