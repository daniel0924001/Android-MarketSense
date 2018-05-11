package com.idroi.marketsense;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;

import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.common.ClientData;
import com.idroi.marketsense.common.FBHelper;
import com.idroi.marketsense.data.PostEvent;
import com.idroi.marketsense.data.UserProfile;

/**
 * Created by daniel.hsieh on 2018/4/26.
 */

public class SplashActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MSLog.i("Enter SplashActivity");

        MSLog.i("Initialize ClientData");
        ClientData clientData = ClientData.getInstance(this);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int)Math.ceil((double)metrics.widthPixels/metrics.density);
        int height = (int)Math.ceil((double)metrics.heightPixels/metrics.density);
        clientData.setScreenSize(width, height);

        if(FBHelper.checkFBLogin()) {
            String id = clientData.getUserProfile().getUserId();
            String password = UserProfile.generatePassword(
                    clientData.getUserProfile().getUserId(),
                    clientData.getUserProfile().getUserType());
            String email = clientData.getUserProfile().getUserEmail();
            PostEvent.sendLogin(this, id, password, email);
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    @Override
    protected void onDestroy() {
        MSLog.i("Exit SplashActivity");
        super.onDestroy();
    }
}
