package com.idroi.marketsense.data;

import com.idroi.marketsense.Logging.MSLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by daniel.hsieh on 2018/9/19.
 */

public class Knowledge {

    private static final String KEYWORD = "keyword";
    private static final String DEFINITION = "definition";
    private static final String EXAMPLE = "example";
    private static final String STRATEGY = "strategy";
    private static final String CATEGORY = "category";
    private static final String RELATED_KEYWORDS = "related_keywords";

    private String mKeyword;
    private String mDefinition;
    private String mExample;
    private String mStrategy;
    private String mCategory;
    private ArrayList<String> mRelatedKeywords;

    public Knowledge() {
        mRelatedKeywords = new ArrayList<>();
    }

    public void setKeyword(String keyword) {
        mKeyword = keyword;
    }

    public void setDefinition(String definition) {
        mDefinition = definition;
    }

    public void setExample(String example) {
        mExample = example;
    }

    public void setStrategy(String strategy) {
        mStrategy = strategy;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public void setRelatedKeywords(JSONArray jsonArray) {
        if(jsonArray == null) {
            return;
        }

        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                mRelatedKeywords.add(jsonArray.getString(i));
            } catch (JSONException exception) {
                // pass
            }
        }
    }

    public String getKeyword() {
        return mKeyword;
    }

    public String getDefinition() {
        return mDefinition;
    }

    public String getExample() {
        return mExample;
    }

    public String getStrategy() {
        return mStrategy;
    }

    public String getCategory() {
        return mCategory;
    }

    public ArrayList<String> getRelatedKeywords() {
        return mRelatedKeywords;
    }

    public static Knowledge jsonObjectToKnowledge(JSONObject jsonObject) {
        Knowledge knowledge = new Knowledge();
        Iterator<String> iterator = jsonObject.keys();
        while(iterator.hasNext()) {
            String key = iterator.next();
            try {
                switch (key) {
                    case KEYWORD:
                        knowledge.setKeyword(jsonObject.optString(KEYWORD));
                        break;
                    case DEFINITION:
                        knowledge.setDefinition(jsonObject.optString(DEFINITION));
                        break;
                    case EXAMPLE:
                        knowledge.setExample(jsonObject.optString(EXAMPLE));
                        break;
                    case STRATEGY:
                        knowledge.setStrategy(jsonObject.optString(STRATEGY));
                        break;
                    case RELATED_KEYWORDS:
                        knowledge.setRelatedKeywords(jsonObject.optJSONArray(RELATED_KEYWORDS));
                        break;
                    case CATEGORY:
                        knowledge.setCategory(jsonObject.optString(CATEGORY));
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                MSLog.e("key: " + key + ", " + e.toString());
            }
        }
        return knowledge;
    }
}
