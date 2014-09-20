package org.monroe.team.smooker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.monroe.team.smooker.app.uc.OverNightUpdate;

public class SystemAlarmBroadcastReceiver extends BroadcastReceiver {

    public SystemAlarmBroadcastReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra("TIME_TO_UPDATE_STATISTICS",false)){
            SmookerApplication.instance.getModel().execute(OverNightUpdate.class,null);
        }
        if (intent.getBooleanExtra("TIME_TO_NOTIFICATION_STATISTICS",false)){
            //TODO: calculate and fire smoke today
            //TODO: re calculate quit smoke schedule
        }
    }
}
