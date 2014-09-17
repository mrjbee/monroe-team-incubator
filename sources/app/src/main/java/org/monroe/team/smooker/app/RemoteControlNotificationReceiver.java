package org.monroe.team.smooker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RemoteControlNotificationReceiver extends BroadcastReceiver {

    public RemoteControlNotificationReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra("CLOSE",false)){
            SmookerApplication.instance.onRemoteControlNotificationCloseRequest();
        } else {
            SmookerApplication.instance.onRemoteControlNotificationAddSmokeRequest();
        }
    }
}
