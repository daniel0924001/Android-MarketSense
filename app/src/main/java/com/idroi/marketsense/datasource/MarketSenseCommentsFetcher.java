package com.idroi.marketsense.datasource;

import android.content.Context;
import android.os.Handler;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.MarketSenseError;
import com.idroi.marketsense.common.MarketSenseNetworkError;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.CommentAndVote;
import com.idroi.marketsense.request.SingleNewsRequest;
import com.idroi.marketsense.util.DeviceUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
    private SingleNewsRequest mSingleNewsRequest;

    MarketSenseCommentsFetcher(Context context,
           MarketSenseCommentsNetworkListener marketSenseCommentsNetworkListener) {
        mContext = new WeakReference<Context>(context);
        mMarketSenseCommentsNetworkListener = marketSenseCommentsNetworkListener;
        mTimeoutHandler = new Handler();
        mTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if(mSingleNewsRequest != null) {
                    mSingleNewsRequest.cancel();
                    mSingleNewsRequest = null;
                }
                mMarketSenseCommentsNetworkListener.onCommentsFail(MarketSenseError.NETWROK_CONNECTION_TIMEOUT);
            }
        };
    }

    void makeRequest(String url) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        if(!DeviceUtils.isNetworkAvailable(context)) {
            mMarketSenseCommentsNetworkListener.onCommentsFail(MarketSenseError.NETWORK_CONNECTION_FAILED);
            return;
        }

        requestComments(url);
    }

    private void requestComments(String url) {
        final Context context = getContextOrDestroy();
        if(context == null) {
            return;
        }

        MSLog.i("Loading comments ...: " + url);

        mSingleNewsRequest = new SingleNewsRequest(Request.Method.GET, url, new Response.Listener<CommentAndVote>() {
            @Override
            public void onResponse(CommentAndVote response) {
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                mMarketSenseCommentsNetworkListener.onCommentsLoad(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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

        mTimeoutHandler.postDelayed(mTimeoutRunnable, 3000);
        mSingleNewsRequest.setShouldCache(false);
        Networking.getRequestQueue(context).add(mSingleNewsRequest);

    }

    public void destroy() {
        mContext.clear();
        if(mSingleNewsRequest != null) {
            mSingleNewsRequest.cancel();
            mSingleNewsRequest = null;
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
}
