package com.idroi.marketsense.datasource;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

import com.idroi.marketsense.util.Preconditions;

/**
 * Created by daniel.hsieh on 2018/4/19.
 */

public class Networking {

    private static final String DEFAULT_USER_AGENT = System.getProperty("http.agent");

    private volatile static RequestQueue sRequestQueue;
    private volatile static String sUserAgent;

    public static RequestQueue getRequestQueue(@NonNull Context context) {
        RequestQueue requestQueue = sRequestQueue;
        // Double check locking to initialize
        if(requestQueue == null) {
            synchronized (Networking.class) {
                requestQueue = sRequestQueue;
                if(requestQueue == null) {

                    // Instantiate the cache
                    Cache cache = new DiskBasedCache(context.getApplicationContext().getCacheDir(),
                            1024 * 1024); // 1MB cap

                    // Set up the network to use HttpURLConnection as the HTTP client.
                    Network network = new BasicNetwork(new HurlStack());

                    requestQueue = new RequestQueue(cache, network);
                    sRequestQueue = requestQueue;
                    requestQueue.start();
                }
            }
        }

        return requestQueue;
    }

    @NonNull
    public static String getUserAgent(@NonNull Context context) {
        Preconditions.checkNotNull(context);

        String userAgent = sUserAgent;
        if (userAgent == null) {
            synchronized (Networking.class) {
                userAgent = sUserAgent;
                if (userAgent == null) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            userAgent = WebSettings.getDefaultUserAgent(context);
                        } else if (Looper.myLooper() == Looper.getMainLooper()) {
                            // WebViews may only be instantiated on the UI thread. If anything goes
                            // wrong with getting a user agent, use the system-specific user agent.
                            userAgent = new WebView(context).getSettings().getUserAgentString();
                        } else {
                            userAgent = DEFAULT_USER_AGENT;
                        }
                    } catch (Exception e) {
                        // Some custom ROMs may fail to get a user agent. If that happens, return
                        // the Android system user agent.
                        userAgent = DEFAULT_USER_AGENT;
                    }
                    sUserAgent = userAgent;
                }
            }
        }

        return userAgent;
    }
}
