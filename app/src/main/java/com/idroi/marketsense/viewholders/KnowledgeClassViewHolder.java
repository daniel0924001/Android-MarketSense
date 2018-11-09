package com.idroi.marketsense.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;

/**
 * Created by daniel.hsieh on 2018/11/8.
 */

public class KnowledgeClassViewHolder {

    public TextView titleTextView;
    public ImageView iconImageView;

    static final KnowledgeClassViewHolder EMPTY_VIEW_HOLDER = new KnowledgeClassViewHolder();

    private KnowledgeClassViewHolder() {
    }

    public static KnowledgeClassViewHolder convertToViewHolder(final View view) {
        final KnowledgeClassViewHolder viewHolder = new KnowledgeClassViewHolder();
        try {
            viewHolder.titleTextView = view.findViewById(R.id.knowledge_class_title);
            viewHolder.iconImageView = view.findViewById(R.id.knowledge_class_icon);

            return viewHolder;
        } catch (ClassCastException exception) {
            MSLog.e(exception.toString());
            return EMPTY_VIEW_HOLDER;
        }
    }
}
