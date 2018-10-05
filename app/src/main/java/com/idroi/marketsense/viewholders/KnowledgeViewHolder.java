package com.idroi.marketsense.viewholders;

import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/9/19.
 */

public class KnowledgeViewHolder {

    public TextView titleTextView;
    public TextView descriptionTextView;

    static final KnowledgeViewHolder EMPTY_VIEW_HOLDER = new KnowledgeViewHolder();

    private KnowledgeViewHolder() {}

    public static KnowledgeViewHolder convertToViewHolder(final View view) {
        final KnowledgeViewHolder knowledgeViewHolder = new KnowledgeViewHolder();
        try {
            knowledgeViewHolder.titleTextView = view.findViewById(R.id.knowledge_title);
            knowledgeViewHolder.descriptionTextView = view.findViewById(R.id.knowledge_description);

            return knowledgeViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
