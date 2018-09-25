package com.idroi.marketsense;

import android.app.Application;
import android.arch.lifecycle.LifecycleObserver;
import android.os.Handler;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.AppLifecycleHandler;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.data.UserProfile;
import com.idroi.marketsense.util.ActionBarHelper;

import static com.idroi.marketsense.data.UserProfile.NOTIFY_ID_NEED_TO_REOPEN;

/**
 * Created by daniel.hsieh on 2018/8/24.
 */

public class App extends Application implements AppLifecycleHandler.LifecycleDelegate, LifecycleObserver {

    private static final int REOPEN_TIME_CONST = 10 * 60 * 1000;

    private boolean mIsNeedToReopen = false;
    private Handler mHandler;
    private Runnable mReopenRunnable;

    @Override
    public void onCreate() {
        super.onCreate();

        AppLifecycleHandler appLifecycleHandler = new AppLifecycleHandler(this);
        registerActivityLifecycleCallbacks(appLifecycleHandler);
        registerComponentCallbacks(appLifecycleHandler);

        mHandler = new Handler();
        mReopenRunnable = new Runnable() {
            @Override
            public void run() {
                MSLog.e("set mIsNeedToReopen = true");
                mIsNeedToReopen = true;
            }
        };
    }

    @Override
    public void onAppBackgrounded() {
        MSLog.d("onAppBackgrounded");
        ClientData clientData = ClientData.getInstance(this);
        if(clientData != null && mHandler != null) {
            mHandler.postDelayed(mReopenRunnable, REOPEN_TIME_CONST);
        }

        ActionBarHelper actionBarHelper = ActionBarHelper.getInstance();
        if(actionBarHelper != null) {
            actionBarHelper.clearCurrentActionBarType();
        }
    }

    @Override
    public void onAppForegrounded() {
        MSLog.d("onAppForegrounded");
        if(mHandler != null) {
            mHandler.removeCallbacks(mReopenRunnable);
        }

        if(mIsNeedToReopen) {
            mIsNeedToReopen = false;
            ClientData clientData = ClientData.getInstance(this);
            if(clientData != null) {
                UserProfile userProfile = clientData.getUserProfile();
                if(userProfile != null) {
                    MSLog.d("NOTIFY_ID_NEED_TO_REOPEN");
                    userProfile.globalBroadcast(NOTIFY_ID_NEED_TO_REOPEN);
                }
            }
        }
    }
}
