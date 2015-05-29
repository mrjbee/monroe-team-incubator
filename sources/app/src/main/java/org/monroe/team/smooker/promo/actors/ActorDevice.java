package org.monroe.team.smooker.promo.actors;

import android.content.Context;
import android.content.Intent;

import org.monroe.team.android.box.actor.Actor;
import org.monroe.team.smooker.promo.android.SmookerApplication;
import org.monroe.team.smooker.promo.common.constant.Settings;

public class ActorDevice extends Actor {

    public ActorDevice() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        SmookerApplication.instance.updateStickyNotification(SmookerApplication.instance.settings().get(Settings.ENABLED_STICKY_NOTIFICATION));
        SmookerApplication.instance.scheduleAlarms();
    }
}
