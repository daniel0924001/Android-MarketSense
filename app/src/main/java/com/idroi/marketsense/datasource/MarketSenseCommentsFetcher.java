package com.idroi.marketsense.datasource;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.common.SharedPreferencesCompat;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.request.CommentAndVoteRequest;
import com.idroi.marketsense.util.MarketSenseUtils;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.idroi.marketsense.common.Constants.SHARED_PREFERENCE_REQUEST_NAME;

/**
 * Created by daniel.hsieh on 2018/5/8.
 */

public class MarketSenseCommentsFetcher {

    public interface MarketSenseCommentsNetworkListener {
        void onCommentsLoad(final CommentAndVote commentAndVote);
        void onCommentsFail(final MarketSenseError marketSenseError);
    }

    private Handler mTimeoutHandler;
    private Runnable mTimeoutRunnable;

    private static final MarketSenseCommentsNetworkListener EMPTY_NETWORK_LISTENER = new MarketSenseCommentsNetworkListener () {
        @Override
        public void onCommentsLoad(CommentAndVote commentAndVote) {

        }

        @Override
        public void onCommentsFail(MarketSenseError marketSenseError) {

        }
    };

    private WeakReference<Context> mContext;
    private MarketSenseCommentsNetworkListener mMarketSenseCommentsNetworkListener;
    private CommentAndVoteRequest mCommentAndVoteRequest;

    MarketSenseCommentsFetcher(Context context,
           MarketSenseCommentsNetworkListener marketSenseCommentsNetworkListener) {
        mContext = new WeakReference<Context>(context);
        mMarketSenseCommentsNetworkListener = marketSenseCommentsNetworkListener;
        mTimeoutHandler = new Handler();
        mTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if(mCommentAndVoteRequest != null) {
                    mCommentAndVoteRequest.cancel();
                    mCommentAndVoteRequest = null;
                }
                mMarketSenseCommentsNetworkListener.onCommentsFail(MarketSenseError.NETWORK_CONNECTION_TIMEOUT);
            }
        };
    }

    void makeRequest(String cacheKey, String url) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(!MarketSenseUtils.isNetworkAvailable(context)) {
            mMarketSenseCommentsNetworkListener.onCommentsFail(MarketSenseError.NETWORK_CONNECTION_FAILED);
            return;
        }

        requestComments(cacheKey, url);
    }

    private void requestComments(final String cacheKey, final String url) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        final AtomicBoolean isCacheSuccessful = new AtomicBoolean(false);
        MSLog.i("Loading comments ...: " + url);

        if(cacheKey != null) {
            Cache cache = Networking.getRequestQueue(context).getCache();

            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE);
            String cacheUrl = sharedPreferences.getString(cacheKey, null);

            if(cacheUrl != null) {
                Cache.Entry entry = cache.get(cacheUrl);
                MSLog.i("Loading comments...(cache): " + cacheKey + ", " + cacheUrl);
                if (entry != null && !entry.isExpired()) {
                    try {
                        CommentAndVote commentAndVote = CommentAndVoteRequest.commentsParseResponse(entry.data);
                        MSLog.i("Loading comments...(cache hit): " + new String(entry.data));
                        isCacheSuccessful.set(true);
                        mMarketSenseCommentsNetworkListener.onCommentsLoad(commentAndVote);
                    } catch (JSONException e) {
                        MSLog.e("Loading comments...(cache failed JSONException)");
                    }
                } else {
                    MSLog.i("Loading comments...(cache miss or expired)");
                }
            }
        }

        mCommentAndVoteRequest = new CommentAndVoteRequest(Request.Method.GET, url, new Response.Listener<CommentAndVote>() {
            @Override
            public void onResponse(CommentAndVote response) {
                final Context context = getContextOrDestroy();
                if(context == null) {
                    return;
                }

                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);

                if(cacheKey != null) {
                    writeToSharedPreference(cacheKey, url);
                    if(isCacheSuccessful.get()) {
                        MSLog.i("cache part is successful, so we do not return our network response");
                        return;
                    }
                }

                mMarketSenseCommentsNetworkListener.onCommentsLoad(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final Context context = getContextOrDestroy();
                if(context == null) {
                    return;
                }

                MSLog.e("Comments Request error: " + error.getMessage(), error);
                if(error.networkResponse != null) {
                    MSLog.e("Comments Request error: " + new String(error.networkResponse.data), error);
                }
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                if(error instanceof MarketSenseNetworkError) {
                    MarketSenseNetworkError networkError = (MarketSenseNetworkError) error;
                    mMarketSenseCommentsNetworkListener.onCommentsFail(networkError.getReason());
                } else {
                    mMarketSenseCommentsNetworkListener.onCommentsFail(MarketSenseError.NETWORK_VOLLEY_ERROR);
                }
            }
        });

        mTimeoutHandler.postDelayed(mTimeoutRunnable, 10000);
        if(cacheKey != null) {
            mCommentAndVoteRequest.setShouldCache(true);
        } else {
            mCommentAndVoteRequest.setShouldCache(false);
        }
        Networking.getRequestQueue(context).add(mCommentAndVoteRequest);

    }

    public void destroy() {
        mContext.clear();
        if(mCommentAndVoteRequest != null) {
            mCommentAndVoteRequest.cancel();
            mCommentAndVoteRequest = null;
        }
        mMarketSenseCommentsNetworkListener = EMPTY_NETWORK_LISTENER;
        if(mTimeoutHandler != null) {
            mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
        }
    }

    private Context getContextOrDestroy() {
        final Context context = mContext.get();
        if (context == null) {
            destroy();
            MSLog.d("Weak reference to Context in MarketSenseCommentsFetcher became null. " +
                    "This instance of MarketSenseCommentsFetcher is destroyed and " +
                    "no more requests will be processed.");
        }
        return context;
    }

    private void writeToSharedPreference(String cacheKey, String url) {
        Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        SharedPreferences.Editor editor =
                context.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(cacheKey, url);
        SharedPreferencesCompat.apply(editor);

        MSLog.d("Comment network query success, so we save this network url to cache: " + cacheKey + ", " + url);
    }

    public static void prefetchGeneralComments(final Context context) {
        final Context applicationContext = context.getApplicationContext();
        final String url = CommentAndVoteRequest.queryCommentsEvent();
        CommentAndVoteRequest commentAndVoteRequest = new CommentAndVoteRequest(Request.Method.GET, url, new Response.Listener<CommentAndVote>() {
            @Override
            public void onResponse(CommentAndVote response) {
                if(applicationContext != null) {
                    SharedPreferences.Editor editor =
                            applicationContext.getSharedPreferences(SHARED_PREFERENCE_REQUEST_NAME, Context.MODE_PRIVATE).edit();
                    editor.putString(CommentAndVoteRequest.COMMENT_CACHE_KEY_GENERAL, url);
                    SharedPreferencesCompat.apply(editor);
                    MSLog.d("Prefetch comment network query success, so we save this network url to cache: " + CommentAndVoteRequest.COMMENT_CACHE_KEY_GENERAL + ", " + url);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MSLog.e("Prefetch comment error: " + error.getMessage(), error);
            }
        });

        commentAndVoteRequest.setShouldCache(true);
        Networking.getRequestQueue(applicationContext).add(commentAndVoteRequest);
    }
}
