package com.idroi.marketsense.datasource;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.ResponseDelivery;

/**
 * Created by daniel.hsieh on 2018/4/19.
 */

public class MarketSenseRequestQueue extends RequestQueue {
    public MarketSenseRequestQueue(Cache cache, Network network, int threadPoolSize, ResponseDelivery delivery) {
        super(cache, network, threadPoolSize, delivery);
    }

    public MarketSenseRequestQueue(Cache cache, Network network, int threadPoolSize) {
        super(cache, network, threadPoolSize);
    }

    public MarketSenseRequestQueue(Cache cache, Network network) {
        super(cache, network);
    }
}
