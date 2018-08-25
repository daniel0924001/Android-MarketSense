package com.idroi.marketsense.common;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;

/**
 * Created by daniel.hsieh on 2018/8/24.
 */

public class AppLifecycleHandler implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    public interface LifecycleDelegate {
        void onAppBackgrounded();
        void onAppForegrounded();
    }

    private boolean mAppInForeground;
    private LifecycleDelegate mLifecycleDelegate;

    public AppLifecycleHandler(LifecycleDelegate lifecycleDelegate) {
        mLifecycleDelegate = lifecycleDelegate;
        mAppInForeground = true;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if(!mAppInForeground) {
            mAppInForeground = true;
            mLifecycleDelegate.onAppForegrounded();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public void onTrimMemory(int level) {
        if(level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            mAppInForeground = false;
            mLifecycleDelegate.onAppBackgrounded();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {

    }

    @Override
    public void onLowMemory() {

    }
}
