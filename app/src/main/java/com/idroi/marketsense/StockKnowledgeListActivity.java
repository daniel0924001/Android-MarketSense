package com.idroi.marketsense;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.KnowledgeClassAdapter;
import com.idroi.marketsense.adapter.KnowledgeRecyclerAdapter;
import com.idroi.marketsense.data.Knowledge;
import com.idroi.marketsense.data.KnowledgeClass;
import com.idroi.marketsense.datasource.KnowledgeClassSource;
import com.idroi.marketsense.request.KnowledgeListRequest;
import com.idroi.marketsense.util.ActionBarHelper;

/**
 * Created by daniel.hsieh on 2018/9/18.
 */

public class StockKnowledgeListActivity extends AppCompatActivity {

    private RecyclerView mClassRecyclerView;
    private KnowledgeClassAdapter mKnowledgeClassAdapter;
    private KnowledgeClassSource mKnowledgeClassSource;

    private RecyclerView mKnowledgeRecyclerView;
    private KnowledgeRecyclerAdapter mKnowledgeRecyclerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_knowledge_list);

        setActionBar();
        setAdapter();
    }

    private void setActionBar() {
        ActionBarHelper.setActionBarForRightImage(this);
    }

    private void setAdapter() {

        // class list
        mKnowledgeClassSource = new KnowledgeClassSource(this);
        mClassRecyclerView = findViewById(R.id.knowledge_class);
        mKnowledgeClassAdapter = new KnowledgeClassAdapter(this, mKnowledgeClassSource);
        mClassRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mClassRecyclerView.setAdapter(mKnowledgeClassAdapter);
        mKnowledgeClassAdapter.setOnItemClickListener(new KnowledgeClassAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(KnowledgeClass knowledgeClass) {
                MSLog.d("title: " + knowledgeClass.getTitle());
            }
        });

        // knowledge list
        mKnowledgeRecyclerView = findViewById(R.id.knowledge_recycler_view);
        mKnowledgeRecyclerAdapter = new KnowledgeRecyclerAdapter(this);
        mKnowledgeRecyclerView.setAdapter(mKnowledgeRecyclerAdapter);
        mKnowledgeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mKnowledgeRecyclerAdapter.setOnItemClickListener(new KnowledgeRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Knowledge knowledge) {
                startActivity(KnowledgeActivity.generateKnowledgeActivityIntent(
                        StockKnowledgeListActivity.this, knowledge));
                overridePendingTransition(R.anim.enter, R.anim.stop);
            }
        });
        mKnowledgeRecyclerAdapter.loadKnowledgeList(
                KnowledgeListRequest.queryKnowledgeList(this, true),
                KnowledgeListRequest.queryKnowledgeList(this, false));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }

    @Override
    protected void onDestroy() {
        if(mKnowledgeRecyclerAdapter != null) {
            mKnowledgeRecyclerAdapter.destroy();
            mKnowledgeRecyclerAdapter = null;
        }
        if(mKnowledgeClassAdapter != null) {
            mKnowledgeClassAdapter.destroy();
            mKnowledgeClassAdapter = null;
        }
        if(mKnowledgeClassSource != null) {
            mKnowledgeClassSource.destroy();
            mKnowledgeClassSource = null;
        }
        super.onDestroy();
    }
}
