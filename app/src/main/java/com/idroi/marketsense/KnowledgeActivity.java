package com.idroi.marketsense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.idroi.marketsense.data.Knowledge;
import com.idroi.marketsense.util.ActionBarHelper;
import com.idroi.marketsense.viewholders.KnowledgeContentItemViewHolder;

/**
 * Created by daniel.hsieh on 2018/9/20.
 */

public class KnowledgeActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    public static final String EXTRA_EXAMPLE = "EXTRA_EXAMPLE";
    public static final String EXTRA_STRATEGY = "EXTRA_STRATEGY";

    private String mTitle;
    private String mDescription;
    private String mExample;
    private String mStrategy;

    private KnowledgeContentItemViewHolder mDescriptionViewHolder;
    private KnowledgeContentItemViewHolder mStrategyViewHolder;
    private KnowledgeContentItemViewHolder mExampleViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_knowledge);

        setInformation();
        setActionBar();
        setViewHolder();
    }

    private void setActionBar() {
        ActionBarHelper.setActionBarForSimpleTitleAndBack(this, mTitle);
    }

    private void setViewHolder() {
        mDescriptionViewHolder = KnowledgeContentItemViewHolder
                .convertToViewHolder(findViewById(R.id.description_block));
        mStrategyViewHolder = KnowledgeContentItemViewHolder
                .convertToViewHolder(findViewById(R.id.strategy_block));
        mExampleViewHolder = KnowledgeContentItemViewHolder
                .convertToViewHolder(findViewById(R.id.example_block));

        KnowledgeContentItemViewHolder.update(mDescriptionViewHolder,
                getString(R.string.knowledge_description_const), mDescription);
        KnowledgeContentItemViewHolder.update(mStrategyViewHolder,
                getString(R.string.knowledge_strategy_const), mStrategy);
        KnowledgeContentItemViewHolder.update(mExampleViewHolder,
                getString(R.string.knowledge_example_const), mExample);
    }

    private void setInformation() {
        Intent intent = getIntent();
        mTitle = intent.getStringExtra(EXTRA_TITLE);
        mDescription = intent.getStringExtra(EXTRA_DESCRIPTION);
        mExample = intent.getStringExtra(EXTRA_EXAMPLE);
        mStrategy = intent.getStringExtra(EXTRA_STRATEGY);
    }

    public static Intent generateKnowledgeActivityIntent(Context context, Knowledge knowledge) {
        Intent intent = new Intent(context, KnowledgeActivity.class);
        intent.putExtra(EXTRA_TITLE, knowledge.getKeyword());
        intent.putExtra(EXTRA_DESCRIPTION, knowledge.getDefinition());
        intent.putExtra(EXTRA_EXAMPLE, knowledge.getExample());
        intent.putExtra(EXTRA_STRATEGY, knowledge.getStrategy());
        return intent;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }
}
