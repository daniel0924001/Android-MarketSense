package com.idroi.marketsense;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.adapter.KnowledgeRecyclerAdapter;
import com.idroi.marketsense.data.Knowledge;
import com.idroi.marketsense.request.KnowledgeListRequest;
import com.idroi.marketsense.util.ActionBarHelper;

/**
 * Created by daniel.hsieh on 2018/9/18.
 */

public class StockKnowledgeListActivity extends AppCompatActivity {

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
                KnowledgeListRequest.queryKnowledgeList(),
                KnowledgeListRequest.queryKnowledgeList());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stop, R.anim.right_to_left);
    }
}
