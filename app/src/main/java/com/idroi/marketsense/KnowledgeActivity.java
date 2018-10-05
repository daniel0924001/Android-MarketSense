package com.idroi.marketsense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.idroi.marketsense.adapter.KnowledgeRecyclerAdapter;
import com.idroi.marketsense.adapter.NewsRecyclerAdapter;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.Knowledge;
import com.idroi.marketsense.data.News;
import com.idroi.marketsense.request.NewsRequest;
import com.idroi.marketsense.util.ActionBarHelper;
import com.idroi.marketsense.viewholders.KnowledgeContentItemViewHolder;
import com.idroi.marketsense.viewholders.KnowledgeYouMayWantToKnowViewHolder;
import com.idroi.marketsense.viewholders.NewsRecyclerViewViewHolder;

import java.util.ArrayList;

import static com.idroi.marketsense.adapter.NewsRecyclerAdapter.NEWS_SINGLE_LAYOUT;
import static com.idroi.marketsense.fragments.NewsFragment.KEYWORD_NAME;
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
    private KnowledgeYouMayWantToKnowViewHolder mYouMayWantToKnowViewHolder;
    private NewsRecyclerViewViewHolder mNewsRecyclerViewViewHolder;

    private NestedScrollView mNestedScrollView;

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
        mYouMayWantToKnowViewHolder = KnowledgeYouMayWantToKnowViewHolder
                .convertToViewHolder(findViewById(R.id.you_may_want_to_know_block));
        mNewsRecyclerViewViewHolder = NewsRecyclerViewViewHolder
                .convertToViewHolder(findViewById(R.id.news_block));

        KnowledgeContentItemViewHolder.update(mDescriptionViewHolder,
                getString(R.string.knowledge_description_const), mDescription);
        KnowledgeContentItemViewHolder.update(mStrategyViewHolder,
                getString(R.string.knowledge_strategy_const), mStrategy);
        KnowledgeContentItemViewHolder.update(mExampleViewHolder,
                getString(R.string.knowledge_example_const), mExample);
    }

    private void setRelatedKnowledge() {
        mYouMayWantToKnowViewHolder.setRelatedKnowledge(this, mRelatedKeywords, mTitle, new KnowledgeRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Knowledge knowledge) {
                startActivity(KnowledgeActivity.generateKnowledgeActivityIntent(
                        KnowledgeActivity.this, knowledge));
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
    }

    private void setNewsBlock() {
        mNestedScrollView = findViewById(R.id.body_scroll_view);
        mNewsRecyclerViewViewHolder.update(this, mNestedScrollView, mTitle);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNewsRecyclerViewViewHolder.destroy();
        mYouMayWantToKnowViewHolder.destroy();
        if(mRelatedKeywords != null) {
            mRelatedKeywords.clear();
        }
    }
}
