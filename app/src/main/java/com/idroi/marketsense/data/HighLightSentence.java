package com.idroi.marketsense.data;

import com.idroi.marketsense.Logging.MSLog;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by daniel.hsieh on 2018/6/29.
 */

public class HighLightSentence {

    private static final String SENTENCE = "sen";
    private static final String SCORE = "score";

    private String mSentence;
    private int mScore;

    public HighLightSentence() {
    }

    public void setSentence(String sentence) {
        mSentence = sentence;
    }

    public void setScore(int score) {
        mScore = score;
    }

    public String getSentence() {
        return mSentence;
    }

    public int getScore() {
        return mScore;
    }

    public static HighLightSentence jsonObjectToHighLightSentence(JSONObject jsonObject) {
        HighLightSentence sentence = new HighLightSentence();
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                switch (key) {
                    case SENTENCE:
                        sentence.setSentence(jsonObject.optString(key));
                        break;
                    case SCORE:
                        sentence.setScore(jsonObject.optInt(key));
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                MSLog.e(e.toString());
            }
        }
        return sentence;
    }
}
