package com.idroi.marketsense.datasource;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.common.SharedPreferencesCompat;
import com.idroi.marketsense.data.Knowledge;
import com.idroi.marketsense.request.KnowledgeListRequest;
import com.idroi.marketsense.util.MarketSenseUtils;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_REQUEST_NAME;

/**
 * Created by daniel.hsieh on 2018/9/19.
 */

public class KnowledgeFetcher {

    public interface KnowledgeNetworkListener {
        void onKnowledgeNetworkLoad(final HashMap<String, ArrayList<Knowledge>> knowledgeHashMap);
        void onKnowledgeNetworkFail(final MarketSenseError marketSenseError);
    }

    private Handler mTimeoutHandler;
    private Runnable mTimeoutRunnable;

    private static final KnowledgeNetworkListener EMPTY_NETWORK_LISTENER = new KnowledgeNetworkListener() {
        @Override
        public void onKnowledgeNetworkLoad(HashMap<String, ArrayList<Knowledge>> knowledgeHashMap) {

        }

        @Override
        public void onKnowledgeNetworkFail(MarketSenseError marketSenseError) {

        }
    };

    private WeakReference<Context> mContext;
    private KnowledgeNetworkListener mKnowledgeNetworkListener;
    private KnowledgeListRequest mRequest;

    KnowledgeFetcher(Context context,
                     KnowledgeNetworkListener knowledgeNetworkListener) {
        mContext = new WeakReference<Context>(context);
        mKnowledgeNetworkListener = knowledgeNetworkListener;
        mTimeoutHandler = new Handler();
        mTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if(mRequest != null) {
                    mRequest.cancel();
                    mRequest = null;
                }
                MSLog.w("Knowledge list request is timeout.");
                mKnowledgeNetworkListener.onKnowledgeNetworkFail(MarketSenseError.NETWORK_CONNECTION_TIMEOUT);
            }
        };
    }

    void makeRequest(String networkUrl, String cacheUrl) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(!MarketSenseUtils.isNetworkAvailable(context)) {
            mKnowledgeNetworkListener.onKnowledgeNetworkFail(MarketSenseError.NETWORK_CONNECTION_FAILED);
            return;
        }

        requestKnowledgeList(networkUrl, cacheUrl);
    }

    private void requestKnowledgeList(final String networkUrl, String cacheUrl) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(networkUrl == null) {
            MSLog.e("[ERROR]: networkUrl should not be null." );
            return;
        }

        if(cacheUrl == null) {
            MSLog.d("cacheUrl is null, so we set networkUrl to cacheUrl");
            cacheUrl = networkUrl;
        }

        MSLog.i("Loading knowledge list...(cache): " + cacheUrl);
        final Cache cache = Networking.getRequestQueue(context).getCache();
        Cache.Entry entry = cache.get(cacheUrl);
        if(entry != null && !entry.isExpired()) {
            try {
                HashMap<String, ArrayList<Knowledge>> knowledgeArrayList
                        = KnowledgeListRequest.parseKnowledgeList(entry.data);
                MSLog.i("Loading knowledge list...(cache hit): " + new String(entry.data));
                mKnowledgeNetworkListener.onKnowledgeNetworkLoad(knowledgeArrayList);
            } catch (JSONException e) {
                MSLog.e("Loading knowledge list...(cache failed JSONException)");
            }
        } else {
            MSLog.i("Loading knowledge list...(cache miss or expired)");
        }

        mRequest = new KnowledgeListRequest(networkUrl, new Response.Listener<HashMap<String, ArrayList<Knowledge>>>() {
            @Override
            public void onResponse(HashMap<String, ArrayList<Knowledge>> response) {
                final Context context = getContextOrDestroy();
                if(context == null) {
                    return;
                }

                SharedPreferences.Editor editor =
                        context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE).edit();
                editor.putString(KnowledgeListRequest.API_URL_KNOWLEDGE_LIST, networkUrl);
                SharedPreferencesCompat.apply(editor);
                MSLog.d("Knowledge list network query success, so we save this network url to cache: " + KnowledgeListRequest.API_URL_KNOWLEDGE_LIST + " " + networkUrl);

                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                mKnowledgeNetworkListener.onKnowledgeNetworkLoad(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final Context context = getContextOrDestroy();
                if(context == null) {
                    return;
                }

                MSLog.e("Knowledge Request error: " + error.getMessage(), error);
                if(error.networkResponse != null) {
                    MSLog.e("Stock Request error: " + new String(error.networkResponse.data), error);
                }
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                if(error instanceof MarketSenseNetworkError) {
                    MarketSenseNetworkError networkError = (MarketSenseNetworkError) error;
                    mKnowledgeNetworkListener.onKnowledgeNetworkFail(networkError.getReason());
                } else {
                    mKnowledgeNetworkListener.onKnowledgeNetworkFail(MarketSenseError.NETWORK_VOLLEY_ERROR);
                }
            }
        });

        mTimeoutHandler.postDelayed(mTimeoutRunnable, 5000);
        Networking.getRequestQueue(context).add(mRequest);
    }

    private Context getContextOrDestroy() {
        final Context context = mContext.get();
        if(context == null) {
            destroy();
            MSLog.d("Weak reference to Context in KnowledgeFetcher became null. " +
                    "This instance of KnowledgeFetcher is destroyed and " +
                    "no more requests will be processed.");
        }
        return context;
    }

    public void destroy() {
        mContext.clear();
        if(mRequest != null) {
            mRequest.cancel();
            mRequest = null;
        }
        mKnowledgeNetworkListener = EMPTY_NETWORK_LISTENER;
        if(mTimeoutHandler != null) {
            mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
        }
    }

    public static void prefetchKnowledgeList(final Context context) {
        final Context applicationContext = context.getApplicationContext();
        final String networkUrl = KnowledgeListRequest.queryKnowledgeList(context, true);
        KnowledgeListRequest knowledgeListRequest = new KnowledgeListRequest(networkUrl, new Response.Listener<HashMap<String, ArrayList<Knowledge>>>() {
            @Override
            public void onResponse(HashMap<String, ArrayList<Knowledge>> response) {
                if(applicationContext != null) {
                    SharedPreferences.Editor editor =
                            context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE).edit();
                    editor.putString(KnowledgeListRequest.API_URL_KNOWLEDGE_LIST, networkUrl);
                    SharedPreferencesCompat.apply(editor);
                    MSLog.d("Knowledge list network query success, so we save this network url to cache: " + KnowledgeListRequest.API_URL_KNOWLEDGE_LIST + " " + networkUrl);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MSLog.e("Knowledge Request error: " + error.getMessage(), error);
            }
        });

        knowledgeListRequest.setShouldCache(true);
        Networking.getRequestQueue(context).add(knowledgeListRequest);
    }
}
