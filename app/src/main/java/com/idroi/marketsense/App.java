package com.idroi.marketsense;

import android.app.Application;
import android.arch.lifecycle.LifecycleObserver;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.AppLifecycleHandler;

/**
 * Created by daniel.hsieh on 2018/8/24.
 */

public class App extends Application implements AppLifecycleHandler.LifecycleDelegate , LifecycleObserver{

    @Override
    public void onCreate() {
        super.onCreate();

        AppLifecycleHandler appLifecycleHandler = new AppLifecycleHandler(this);
        registerActivityLifecycleCallbacks(appLifecycleHandler);
        registerComponentCallbacks(appLifecycleHandler);
    }

    @Override
    public void onAppBackgrounded() {
        MSLog.d("onAppBackgrounded");
    }

    @Override
    public void onAppForegrounded() {
        MSLog.d("onAppForegrounded");
    }
}
