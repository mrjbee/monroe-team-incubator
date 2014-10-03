package org.monroe.team.smooker.app.actors;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.monroe.team.smooker.app.android.SmookerApplication;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.uc.OverNightUpdate;

public class ActorSystemAlarm extends BroadcastReceiver {

    public ActorSystemAlarm() {}

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Alarms.TIME_TO_UPDATE_STATISTICS.isAbout(intent)){
            SmookerApplication.instance.getModel().execute(OverNightUpdate.class,null);
        }

        if (Alarms.TIME_TO_NOTIFICATION_STATISTICS.isAbout(intent)){
            SmookerApplication.instance.doMorningNotification();
        }

        if (Alarms.TIME_TO_NEXT_SMOKE.isAbout(intent)){
            SmookerApplication.instance.getSuggestionsController().onSmokeAlarm();
        }

        if (Alarms.TIME_TO_UPDATE_CALENDAR_WIDGET.isAbout(intent)){
            Events.QUIT_SCHEDULE_REFRESH.send(context, true);
        }

    }


    public static PendingIntent createIntent(Context context, int id, String name) {
        Intent intent = new Intent(context, ActorSystemAlarm.class);
        intent.putExtra(name,true);
        return PendingIntent.getBroadcast(context, id, intent, 0);
    }


    public static PendingIntent createIntent(Context context, Alarms alarmEvent) {
        return createIntent(context, alarmEvent.id, alarmEvent.name());
    }


    public static enum Alarms {

        TIME_TO_NEXT_SMOKE(410),
        TIME_TO_UPDATE_STATISTICS(401),
        TIME_TO_NOTIFICATION_STATISTICS(402),
        TIME_TO_UPDATE_CALENDAR_WIDGET(403);

        private final int id;

        Alarms(int id) {
            this.id = id;
        }

        private boolean isAbout(Intent intent){
           return intent != null && intent.getBooleanExtra(this.name(),false);
        }
    }

}
