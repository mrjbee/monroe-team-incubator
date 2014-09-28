package org.monroe.team.smooker.app;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.event.Event;
import org.monroe.team.smooker.app.uc.OverNightUpdate;

public class SystemAlarmReceiver extends BroadcastReceiver {

    public SystemAlarmReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getBooleanExtra("TIME_TO_UPDATE_STATISTICS",false)){
            SmookerApplication.instance.getModel().execute(OverNightUpdate.class,null);
        }

        if (intent.getBooleanExtra("TIME_TO_NOTIFICATION_STATISTICS",false)){
            SmookerApplication.instance.doMorningNotification();
        }

        if (intent.getBooleanExtra("TIME_TO_NEXT_SMOKE",false)){
            SmookerApplication.instance.showNextSmokeNotification();
        }

        if (intent.getBooleanExtra("TIME_TO_UPDATE_CALENDAR_WIDGET",false)){
             Event.send(context,Events.QUIT_SCHEDULE_REFRESH,true);
        }

        if (intent.getBooleanExtra("SKIP_SMOKE",false)){
            SmookerApplication.instance.cancelNextSmokeNotification(true);
            SmookerApplication.instance.closeSystemDialogs();
            Events.SUGGESTED_SMOKE_SKIPPED.send(context,true);
        }
    }


    public static PendingIntent createIntent(Context context, int id, String name) {
        Intent intent = new Intent(context, SystemAlarmReceiver.class);
        intent.putExtra(name,true);
        return PendingIntent.getBroadcast(context, id, intent, 0);
    }
}
