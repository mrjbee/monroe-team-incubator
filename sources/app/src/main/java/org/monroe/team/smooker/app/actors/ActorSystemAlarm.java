package org.monroe.team.smooker.app.actors;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.monroe.team.android.box.actor.Actor;
import org.monroe.team.android.box.actor.ActorAction;
import org.monroe.team.smooker.app.android.SmookerApplication;
import org.monroe.team.smooker.app.common.constant.Events;

public class ActorSystemAlarm extends Actor {

    public ActorSystemAlarm() {}

    public final static AlarmAction ACTION_OVERNIGHT_UPDATE = new AlarmAction("OVERNIGHT_UPDATE", 600);
    public final static AlarmAction ACTION_NEXT_SMOKE = new AlarmAction("NEXT_SMOKE", 601);

    @Override
    public void onReceive(Context context, Intent intent) {
        reactOn(ACTION_OVERNIGHT_UPDATE,intent,new SilentReaction() {
            @Override
            protected void reactSilent(Intent intent) {
                SmookerApplication.instance.doOverNightUpdate();
            }
        });

        reactOn(ACTION_NEXT_SMOKE,intent, new SilentReaction() {
            @Override
            protected void reactSilent(Intent intent) {
                SmookerApplication.instance.getSuggestionsController().onSmokeAlarm();
            }
        });
    }

    public static class AlarmAction extends ActorAction{
        public AlarmAction(String name, int pendingId) {
            super(name, pendingId, ActorSystemAlarm.class);
        }
    }

}
