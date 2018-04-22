package com.idroi.marketsense.common;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

/**
 * Created by daniel.hsieh on 2018/4/22.
 */

public class MarketSenseNetworkError extends VolleyError {

    private final MarketSenseError mReason;

    public MarketSenseNetworkError(MarketSenseError error) {
        super(error.getMessage());
        mReason = error;
    }

    public MarketSenseNetworkError(NetworkResponse response, MarketSenseError error) {
        super(response);
        mReason = error;
    }

    public MarketSenseNetworkError(MarketSenseError error, Throwable throwable) {
        super(error.getMessage(), throwable);
        mReason = error;
    }

    public MarketSenseError getReason() {
        return mReason;
    }
}
