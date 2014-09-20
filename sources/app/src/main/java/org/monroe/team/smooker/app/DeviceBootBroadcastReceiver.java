package org.monroe.team.smooker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.monroe.team.smooker.app.common.Settings;

public class DeviceBootBroadcastReceiver extends BroadcastReceiver {
    public DeviceBootBroadcastReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        SmookerApplication.instance
                .updateStickyNotification(SmookerApplication.instance.settings().get(Settings.ENABLED_STICKY_NOTIFICATION));

        SmookerApplication.instance
                .scheduleAlarms();
    }
}
