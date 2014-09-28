package org.monroe.team.smooker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmokeSchedulerReceiver extends BroadcastReceiver {

    public SmokeSchedulerReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        SmookerApplication.instance.scheduleNextSmokeAlarm();
    }
}
