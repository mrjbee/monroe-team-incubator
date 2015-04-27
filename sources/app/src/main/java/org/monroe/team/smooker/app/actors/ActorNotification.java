package org.monroe.team.smooker.app.actors;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.monroe.team.android.box.actor.Actor;
import org.monroe.team.android.box.actor.ActorAction;
import org.monroe.team.smooker.app.android.FrontPageActivity;
import org.monroe.team.smooker.app.android.PreferencesActivity;
import org.monroe.team.smooker.app.android.SmookerApplication;
import org.monroe.team.android.box.actor.ActorActionBuilder;

import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;

public class ActorNotification extends Actor {

    public static SmokerAction OPEN_DASHBOARD = new SmokerAction("OPEN_DASHBOARD",506);

    public static SmokerAction CLOSE_STICKY_NOTIFICATION = new SmokerAction("CLOSE_STICKY_NOTIFICATION",505);
    public static SmokerAction CLOSE_QUIT_SUGGESTION = new SmokerAction("CLOSE_QUIT_SUGGESTION",504);

    public static final SmokerAction ADD_SMOKE = new SmokerAction("ADD_SMOKE",501);
    public static final SmokerAction SKIP_SMOKE = new SmokerAction("SKIP_SMOKE",502);
    public static final SmokerAction POSTPONE_SMOKE = new SmokerAction("POSTPONE_SMOKE",503);

    public ActorNotification() {}

    @Override
    public void onReceive(final Context context, Intent intent) {

        String toastText = null;

        reactOn(CLOSE_STICKY_NOTIFICATION,intent,new SilentReaction() {
            @Override
            protected void reactSilent(Intent intent) {
                SmookerApplication.instance.startActivity(
                        new Intent(SmookerApplication.instance, PreferencesActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                SmookerApplication.instance.closeSystemDialogsOld();
            }
        });

        reactOn(OPEN_DASHBOARD,intent,new SilentReaction() {
            @Override
            protected void reactSilent(Intent intent) {
                SmookerApplication.instance.startActivity(
                        new Intent(SmookerApplication.instance, FrontPageActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                SmookerApplication.instance.closeSystemDialogsOld();
            }
        });

        reactOn(ADD_SMOKE, intent, new SilentReaction() {
            @Override
            protected void reactSilent(Intent intent) {
                SmookerApplication.instance.addSmoke();
            }
        });


        reactOn(SKIP_SMOKE, intent, new SilentReaction() {
            @Override
            protected void reactSilent(Intent intent) {
                SmookerApplication.instance.skipSmoke(SmokeCancelReason.SKIP);
            }
        });

        reactOn(POSTPONE_SMOKE, intent, new SilentReaction() {
            @Override
            protected void reactSilent(Intent intent) {
                SmookerApplication.instance.skipSmoke(SmokeCancelReason.POSTPONE);
            }
        });

    }


    public static ActionBuilder create(Context context, SmokerAction action){
        return new ActionBuilder(context,action);
    }

    public static class ActionBuilder extends ActorActionBuilder<SmokerAction> {

        public ActionBuilder(Context context, SmokerAction action) {
            super(context,action);
        }

        public ActionBuilder toast(){
            return with("toast",true);
        }

        public ActionBuilder closeTray(){
            return with("tray",true);
        }

        public PendingIntent buildDefault() {
            return toast().closeTray().build();
        }
    }


    public static class SmokerAction extends ActorAction {
        public SmokerAction(String name, int pendingId) {
            super(name, pendingId, ActorNotification.class);
        }
    }


}
