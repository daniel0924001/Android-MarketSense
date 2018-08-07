package com.idroi.marketsense.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.SharedPreferencesCompat;
import com.idroi.marketsense.data.NewsReadRecord;

import java.io.IOException;
import java.util.ArrayList;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_NEWS_BROWSE_HISTORY;

/**
 * Created by daniel.hsieh on 2018/8/7.
 */

public class NewsReadRecordHelper {

    private static final String NEWS_READ_HISTORY = "NewsReadHistory_%s";

    public static void saveToInternalStorage(Context context, String userId, ArrayList<NewsReadRecord> readRecords) {

        if(userId == null || readRecords == null) {
            MSLog.w("userId or NewsReadRecord is null in saveToInternalStorage");
            return;
        }

        try {
            SharedPreferences.Editor editor =
                    context.getSharedPreferences(SHARED_PREFERENCE_NEWS_BROWSE_HISTORY, Context.MODE_PRIVATE).edit();
            editor.putString(String.format(NEWS_READ_HISTORY, userId), ObjectSerializer.serialize(readRecords));
            SharedPreferencesCompat.apply(editor);
            MSLog.d("saveToInternalStorage size: " + readRecords.size());
        } catch (IOException exception) {
            MSLog.e("IOException in saveToInternalStorage: " + exception);
        }
    }

    public static ArrayList<NewsReadRecord> readFromInternalStorage(Context context, String userId) {
        ArrayList<NewsReadRecord> readRecords = new ArrayList<>();

        if(userId == null) {
            MSLog.e("userId is null in readFromInternalStorage");
            return readRecords;
        }

        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCE_NEWS_BROWSE_HISTORY, Context.MODE_PRIVATE);
        try {
            Object object = ObjectSerializer.deserialize(prefs.getString(String.format(NEWS_READ_HISTORY, userId), ObjectSerializer.serialize(new ArrayList<NewsReadRecord>())));
            if(object instanceof ArrayList) {
                ArrayList arrayList = (ArrayList) object;
                for (Object o : arrayList) {
                    if(o instanceof NewsReadRecord) {
                        NewsReadRecord newsReadRecord = (NewsReadRecord) o;
                        if(!newsReadRecord.isExpired()) {
                            readRecords.add(newsReadRecord);
                        }
                    }
                }
            }
            MSLog.d("readFromInternalStorage size: " + readRecords.size());
        } catch (IOException exception) {
            MSLog.e("IOException in readFromInternalStorage: " + exception);
        } catch (ClassCastException exception) {
            MSLog.e("ClassNotFoundException in readFromInternalStorage: " + exception);
        }
        return readRecords;
    }
}
