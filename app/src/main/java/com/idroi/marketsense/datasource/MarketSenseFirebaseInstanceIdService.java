package com.idroi.marketsense.datasource;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.idroi.marketsense.Logging.MSLog;

/**
 * Created by daniel.hsieh on 2018/5/25.
 */

public class MarketSenseFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        MSLog.d("Refreshed token: " + refreshedToken);
    }
}
