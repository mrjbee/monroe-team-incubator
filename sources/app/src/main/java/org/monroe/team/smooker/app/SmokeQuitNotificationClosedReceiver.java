package org.monroe.team.smooker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.monroe.team.smooker.app.common.Settings;

public class SmokeQuitNotificationClosedReceiver extends BroadcastReceiver {

    public SmokeQuitNotificationClosedReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        SmookerApplication.instance.settings().set(Settings.FIRST_TIME_QUIT_SMOKE_PAGE,false);
    }
}
