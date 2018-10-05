package com.idroi.marketsense.viewholders;

import android.view.View;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.MarketSenseRendererHelper;

/**
 * Created by daniel.hsieh on 2018/9/20.
 */

public class KnowledgeContentItemViewHolder {

    public TextView titleTextView;
    public TextView contentTextView;

    static final KnowledgeContentItemViewHolder EMPTY_VIEW_HOLDER = new KnowledgeContentItemViewHolder();

    private KnowledgeContentItemViewHolder() {}

    public static KnowledgeContentItemViewHolder convertToViewHolder(final View view) {
        final KnowledgeContentItemViewHolder knowledgeViewHolder = new KnowledgeContentItemViewHolder();
        try {
            knowledgeViewHolder.titleTextView =
                    view.findViewById(R.id.knowledge_description_title);
            knowledgeViewHolder.contentTextView =
                    view.findViewById(R.id.knowledge_description_content);

            return knowledgeViewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }

    public static void update(KnowledgeContentItemViewHolder holder, String title, String content) {
        MarketSenseRendererHelper.addTextView(holder.titleTextView, title);
        MarketSenseRendererHelper.addTextView(holder.contentTextView, content);
    }
}
