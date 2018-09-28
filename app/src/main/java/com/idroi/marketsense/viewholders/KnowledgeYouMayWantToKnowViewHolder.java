package com.idroi.marketsense.viewholders;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.Group;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.idroi.marketsense.KnowledgeActivity;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.adapter.KnowledgeRecyclerAdapter;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.Knowledge;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/9/27.
 */

public class KnowledgeYouMayWantToKnowViewHolder {

    public RecyclerView knowledgeRecyclerView;
    public KnowledgeRecyclerAdapter knowledgeRecyclerAdapter;
    public Group relatedGroup;

    static final KnowledgeYouMayWantToKnowViewHolder EMPTY_VIEW_HOLDER = new KnowledgeYouMayWantToKnowViewHolder();

    private KnowledgeYouMayWantToKnowViewHolder() {

    }

    public static KnowledgeYouMayWantToKnowViewHolder convertToViewHolder(final View view) {
        final KnowledgeYouMayWantToKnowViewHolder viewHolder = new KnowledgeYouMayWantToKnowViewHolder();
        try {
            viewHolder.relatedGroup = view.findViewById(R.id.related_knowledge_group);

            viewHolder.knowledgeRecyclerView = view.findViewById(R.id.related_knowledge_recycler_view);


            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public void setRelatedKnowledge(Activity activity,
                                    ArrayList<String> relatedKeywords,
                                    KnowledgeRecyclerAdapter.OnItemClickListener listener) {
        setRelatedKnowledge(activity, relatedKeywords, null, listener);
    }

    public void setRelatedKnowledge(Activity activity,
                                     ArrayList<String> relatedKeywords,
                                     String title,
                                     KnowledgeRecyclerAdapter.OnItemClickListener listener) {

        if(relatedKeywords != null && relatedKeywords.size() > 0) {

            ClientData clientData = ClientData.getInstance();
            ArrayList<Knowledge> knowledgeList = new ArrayList<>();
            for(String keyword : relatedKeywords) {
                Knowledge knowledge = clientData.getKnowledgeFromKeyword(keyword);

                if(knowledge != null && (title == null || !knowledge.getKeyword().equals(title))) {
                    knowledgeList.add(knowledge);
                }
            }

            if(knowledgeList.size() > 0) {

                relatedGroup.setVisibility(View.VISIBLE);

                knowledgeRecyclerAdapter = new KnowledgeRecyclerAdapter(activity);
                knowledgeRecyclerView.setAdapter(knowledgeRecyclerAdapter);
                knowledgeRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

                knowledgeRecyclerAdapter.setOnItemClickListener(listener);
                knowledgeRecyclerAdapter.setKnowledgeList(knowledgeList);
            } else {
                relatedGroup.setVisibility(View.GONE);
            }
        } else {
            relatedGroup.setVisibility(View.GONE);
        }
    }

    public void destroy() {
        if(knowledgeRecyclerAdapter != null) {
            knowledgeRecyclerAdapter.destroy();
        }
    }
}
