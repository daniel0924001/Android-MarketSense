package com.idroi.marketsense.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public class MarketSenseUtils {

    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void postOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public static boolean isNetworkAvailable(@Nullable final Context context) {
        if (context == null) {
            return false;
        }

        if (!MarketSenseUtils.isPermissionGranted(context, INTERNET)) {
            return false;
        }

        /**
         * This is only checking if we have permission to access the network state
         * It's possible to not have permission to check network state but still be able
         * to access the network itself.
         */
        if (!MarketSenseUtils.isPermissionGranted(context, ACCESS_NETWORK_STATE)) {
            return true;
        }

        // Otherwise, perform the connectivity check.
        try {
            final ConnectivityManager connnectionManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo networkInfo = connnectionManager.getActiveNetworkInfo();
            return networkInfo.isConnected();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isPermissionGranted(@NonNull final Context context,
                                              @NonNull final String permission) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(permission);

        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED;
    }
}
