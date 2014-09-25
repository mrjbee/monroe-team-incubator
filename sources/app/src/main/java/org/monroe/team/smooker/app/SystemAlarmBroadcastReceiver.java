package org.monroe.team.smooker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.event.Event;
import org.monroe.team.smooker.app.uc.OverNightUpdate;

public class SystemAlarmBroadcastReceiver extends BroadcastReceiver {

    public SystemAlarmBroadcastReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getBooleanExtra("TIME_TO_UPDATE_STATISTICS",false)){
            SmookerApplication.instance.getModel().execute(OverNightUpdate.class,null);
        }

        if (intent.getBooleanExtra("TIME_TO_NOTIFICATION_STATISTICS",false)){
            SmookerApplication.instance.doMorningNotification();
        }

        if (intent.getBooleanExtra("TIME_TO_UPDATE_CALENDAR_WIDGET",false)){
             Event.send(context,Events.QUIT_SCHEDULE_REFRESH,true);
        }
    }
}
