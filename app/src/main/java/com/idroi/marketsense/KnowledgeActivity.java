package com.idroi.marketsense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.KnowledgeRecyclerAdapter;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.Knowledge;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.util.ActionBarHelper;
import com.idroi.marketsense.viewholders.KnowledgeContentItemViewHolder;
import com.idroi.marketsense.viewholders.KnowledgeYouMayWantToKnowViewHolder;

import java.util.ArrayList;

import static com.idroi.marketsense.adapter.NewsRecyclerAdapter.NEWS_SINGLE_LAYOUT;
import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_TASK_ID;

/**
 * Created by daniel.hsieh on 2018/9/20.
 */

public class KnowledgeActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    public static final String EXTRA_EXAMPLE = "EXTRA_EXAMPLE";
    public static final String EXTRA_STRATEGY = "EXTRA_STRATEGY";
    public static final String EXTRA_RELATED_KEYWORDS = "EXTRA_RELATED_KEYWORDS";

    private String mTitle;
    private String mDescription;
    private String mExample;
    private String mStrategy;
    private ArrayList<String> mRelatedKeywords;

    private KnowledgeContentItemViewHolder mDescriptionViewHolder;
    private KnowledgeContentItemViewHolder mStrategyViewHolder;
    private KnowledgeContentItemViewHolder mExampleViewHolder;
    private KnowledgeYouMayWantToKnowViewHolder mYouMayWantToKnow;

    private RecyclerView mKnowledgeRecyclerView;
    @Nullable private KnowledgeRecyclerAdapter mKnowledgeRecyclerAdapter;

    private NestedScrollView mNestedScrollView;
    private RecyclerView mNewsRecyclerView;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;
    private ProgressBar mLoadingProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_knowledge);

        setInformation();
        setTitle();
        setActionBar();
        setViewHolder();
        setRelatedKnowledge();
        setNewsBlock();
    }

    private void setActionBar() {
        ActionBarHelper.setActionBarForSimpleTitleAndBack(this, getString(R.string.knowledge_tutorial));
    }

    private void setTitle() {
        TextView titleTextView = findViewById(R.id.knowledge_title);
        MarketSenseRendererHelper.addTextView(titleTextView, mTitle);
    }

    private void setViewHolder() {
        mDescriptionViewHolder = KnowledgeContentItemViewHolder
                .convertToViewHolder(findViewById(R.id.description_block));
        mStrategyViewHolder = KnowledgeContentItemViewHolder
                .convertToViewHolder(findViewById(R.id.strategy_block));
        mExampleViewHolder = KnowledgeContentItemViewHolder
                .convertToViewHolder(findViewById(R.id.example_block));
        mYouMayWantToKnow = KnowledgeYouMayWantToKnowViewHolder
                .convertToViewHolder(findViewById(R.id.you_may_want_to_know_block));

        KnowledgeContentItemViewHolder.update(mDescriptionViewHolder,
                getString(R.string.knowledge_description_const), mDescription);
        KnowledgeContentItemViewHolder.update(mStrategyViewHolder,
                getString(R.string.knowledge_strategy_const), mStrategy);
        KnowledgeContentItemViewHolder.update(mExampleViewHolder,
                getString(R.string.knowledge_example_const), mExample);
    }

    private void setRelatedKnowledge() {
        mYouMayWantToKnow.setRelatedKnowledge(this, mRelatedKeywords, mTitle, new KnowledgeRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Knowledge knowledge) {
                startActivity(KnowledgeActivity.generateKnowledgeActivityIntent(
                        KnowledgeActivity.this, knowledge));
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
    }

    private void setNewsBlock() {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TITLE, mTitle);

        mLoadingProgressBar = findViewById(R.id.news_list_progress_bar);
        mNestedScrollView = findViewById(R.id.body_scroll_view);
        mNewsRecyclerView = findViewById(R.id.news_recycler_view);

        mNewsRecyclerAdapter = new NewsRecyclerAdapter(this, KEYWORD_TASK_ID, bundle);
        mNewsRecyclerAdapter.setNewsLayoutType(NEWS_SINGLE_LAYOUT);
        mNewsRecyclerView.setNestedScrollingEnabled(false);
        mNewsRecyclerView.setAdapter(mNewsRecyclerAdapter);
        mNewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mNewsRecyclerAdapter.setNewsAvailableListener(new NewsRecyclerAdapter.NewsAvailableListener() {
            @Override
            public void onNewsAvailable() {
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                }
                Group newsGroup = findViewById(R.id.news_group);
                newsGroup.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNewsEmpty() {
                if(mLoadingProgressBar != null) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                }
                Group newsGroup = findViewById(R.id.news_group);
                newsGroup.setVisibility(View.GONE);
            }
        });

        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(v.getChildAt(v.getChildCount() - 1) != null) {
                    if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                            scrollY > oldScrollY) {
                        mNewsRecyclerAdapter.expand(7);
                    }
                }
            }
        });

        mNewsRecyclerAdapter.setOnItemClickListener(new NewsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(News news) {
                mNewsRecyclerAdapter.notifyNewsIsClicked(news);
                startActivity(NewsWebViewActivity.generateNewsWebViewActivityIntent(
                        KnowledgeActivity.this, news.getId(), news.getTitle(),
                        news.getUrlImage(), news.getDate(),
                        news.getPageLink(), news.getOriginLink(),
                        news.getVoteRaiseNum(), news.getVoteFallNum(), news.getStockKeywords(), news.getLevel()));
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });

        ArrayList<String> networkUrls = new ArrayList<>();
        ArrayList<String> cacheUrls = new ArrayList<>();
        networkUrls.add(NewsRequest.queryKeywordNewsUrl(this, mTitle, true));
        cacheUrls.add(NewsRequest.queryKeywordNewsUrl(this, mTitle, false));

        mNewsRecyclerAdapter.loadNews(networkUrls, cacheUrls);
    }

    private void setInformation() {
        Intent intent = getIntent();
        mTitle = intent.getStringExtra(EXTRA_TITLE);
        mDescription = intent.getStringExtra(EXTRA_DESCRIPTION);
        mExample = intent.getStringExtra(EXTRA_EXAMPLE);
        mStrategy = intent.getStringExtra(EXTRA_STRATEGY);
        mRelatedKeywords = intent.getStringArrayListExtra(EXTRA_RELATED_KEYWORDS);
    }

    public static Intent generateKnowledgeActivityIntent(Context context, Knowledge knowledge) {
        Intent intent = new Intent(context, KnowledgeActivity.class);
        intent.putExtra(EXTRA_TITLE, knowledge.getKeyword());
        intent.putExtra(EXTRA_DESCRIPTION, getContent(context, knowledge.getDefinition()));
        intent.putExtra(EXTRA_EXAMPLE, getContent(context, knowledge.getExample()));
        intent.putExtra(EXTRA_STRATEGY, getContent(context, knowledge.getStrategy()));
        intent.putExtra(EXTRA_RELATED_KEYWORDS, knowledge.getRelatedKeywords());
        return intent;
    }

    public static String getContent(Context context, String text) {
        return text.isEmpty() ? context.getString(R.string.knowledge_content_unavailable) : text;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }
}
