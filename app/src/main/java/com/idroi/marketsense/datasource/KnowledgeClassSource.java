package com.idroi.marketsense.datasource;

import android.content.Context;
import android.content.res.Resources;

import com.idroi.marketsense.R;
import com.idroi.marketsense.data.KnowledgeClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel.hsieh on 2018/11/8.
 */

public class KnowledgeClassSource {

    private Context mContext;
    private List<KnowledgeClass> mKnowledgeClassList;

    public KnowledgeClassSource(final Context context) {
        mContext = context.getApplicationContext();

        Resources resources = mContext.getResources();
        mKnowledgeClassList = new ArrayList<>();

        mKnowledgeClassList.add(
                new KnowledgeClass.Builder()
                        .title(resources.getString(R.string.knowledge_class_newbie))
                        .icon(resources.getDrawable(R.mipmap.ic_know_newbie))
                        .present(true)
                        .build()
        );
        mKnowledgeClassList.add(
                new KnowledgeClass.Builder()
                        .title(resources.getString(R.string.knowledge_class_fina))
                        .icon(resources.getDrawable(R.mipmap.ic_know_chart))
                        .build()
        );
        mKnowledgeClassList.add(
                new KnowledgeClass.Builder()
                        .title(resources.getString(R.string.knowledge_class_trade))
                        .icon(resources.getDrawable(R.mipmap.ic_know_trade))
                        .build()
        );
        mKnowledgeClassList.add(
                new KnowledgeClass.Builder()
                        .title(resources.getString(R.string.knowledge_class_apple))
                        .icon(resources.getDrawable(R.mipmap.ic_know_apple))
                        .build()
        );
        mKnowledgeClassList.add(
                new KnowledgeClass.Builder()
                        .title(resources.getString(R.string.knowledge_class_buffet))
                        .icon(resources.getDrawable(R.mipmap.ic_know_buffet))
                        .build()
        );
        mKnowledgeClassList.add(
                new KnowledgeClass.Builder()
                        .title(resources.getString(R.string.knowledge_class_save))
                        .icon(resources.getDrawable(R.mipmap.ic_know_save))
                        .build()
        );
        mKnowledgeClassList.add(
                new KnowledgeClass.Builder()
                        .title(resources.getString(R.string.knowledge_class_global))
                        .icon(resources.getDrawable(R.mipmap.ic_know_global))
                        .build()
        );
        mKnowledgeClassList.add(
                new KnowledgeClass.Builder()
                        .title(resources.getString(R.string.knowledge_class_material))
                        .icon(resources.getDrawable(R.mipmap.ic_know_material))
                        .build()
        );
    }

    public KnowledgeClass getItem(int position) {
        return mKnowledgeClassList.get(position);
    }

    public int getSize() {
        return mKnowledgeClassList.size();
    }

    public void destroy() {
        mKnowledgeClassList.clear();
    }
}
