package org.monroe.team.smooker.app;

import android.app.PendingIntent;
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

    public static PendingIntent createAddSmokeIntent(Context context) {
        Intent addActionIntent = new Intent(context, RemoteControlNotificationReceiver.class);
        return PendingIntent.getBroadcast(context, 2, addActionIntent, 0);
    }
}
