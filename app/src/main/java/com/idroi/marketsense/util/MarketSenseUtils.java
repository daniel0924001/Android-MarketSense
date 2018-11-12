package com.idroi.marketsense.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.idroi.marketsense.BuildConfig;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.datasource.Networking;

import org.jsoup.Jsoup;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;
import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_NEED_TO_APK_UPDATED;

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

    public static void setHtmlColorText(TextView tv, String string) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv.setText(Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        } else {
            tv.setText(Html.fromHtml(string), TextView.BufferType.SPANNABLE);
        }
    }

    public static void isNeedToUpdated(final Context context) {
        String site = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "&hl=en";
        StringRequest documentRequest = new StringRequest(Request.Method.GET, site, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (context != null) {
                        String googlePlayVersionName = Jsoup.parse(response)
                                .select("div:containsOwn(Current Version)")
                                .next()
                                .text();
                        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                        String ourVersionName = info.versionName;
                        if (!TextUtils.isEmpty(googlePlayVersionName) && !ourVersionName.equals(googlePlayVersionName)) {
                            ClientData.getInstance(context).getUserProfile().globalBroadcast(NOTIFY_ID_NEED_TO_APK_UPDATED);
                        }
                    }
                } catch (Exception exception) {
                    MSLog.e("isNeedToUpdated has exception: " + exception.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MSLog.e("isNeedToUpdated has error response: " + error.toString());
            }
        });

        RequestQueue requestQueue = Networking.getRequestQueue(context);
        requestQueue.add(documentRequest);
    }

    /**
     * Used to get deep child offset.
     * <p/>
     * 1. We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
     * 2. So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
     *
     * @param mainParent        Main Top parent.
     * @param parent            Parent.
     * @param child             Child.
     * @param accumulatedOffset Accumulated Offset.
     */
    public static void getDeepChildOffset(final ViewGroup mainParent, final ViewParent parent, final View child, final Point accumulatedOffset) {
        ViewGroup parentGroup = (ViewGroup) parent;
        accumulatedOffset.x += child.getLeft();
        accumulatedOffset.y += child.getTop();
        if (parentGroup.equals(mainParent)) {
            return;
        }
        getDeepChildOffset(mainParent, parentGroup.getParent(), parentGroup, accumulatedOffset);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            View v = activity.getCurrentFocus();
            if (v != null) {
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }
}
