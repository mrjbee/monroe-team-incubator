package org.monroe.team.smooker.app.actors;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.monroe.team.android.box.actor.Actor;
import org.monroe.team.android.box.actor.ActorAction;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.android.SmookerApplication;
import org.monroe.team.android.box.actor.ActorActionBuilder;
import static org.monroe.team.android.box.actor.ActorActionBuilder.requested;

import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;
import org.monroe.team.smooker.app.uc.underreview.AddSmoke;
import org.monroe.team.smooker.app.uc.underreview.CancelSmoke;

public class ActorSmoker extends Actor {

    public static SmokerAction CLOSE_STICKY_NOTIFICATION = new SmokerAction("CLOSE_STICKY_NOTIFICATION",505);
    public static SmokerAction CLOSE_QUIT_SUGGESTION = new SmokerAction("CLOSE_QUIT_SUGGESTION",504);

    public static final SmokerAction ADD_SMOKE = new SmokerAction("ADD_SMOKE",501);
    public static final SmokerAction SKIP_SMOKE = new SmokerAction("SKIP_SMOKE",502);
    public static final SmokerAction POSTPONE_SMOKE = new SmokerAction("POSTPONE_SMOKE",503);

    public ActorSmoker() {}

    @Override
    public void onReceive(final Context context, Intent intent) {

        String toastText = null;

        reactOn(CLOSE_STICKY_NOTIFICATION,intent,new SilentReaction() {
            @Override
            protected void reactSilent(Intent intent) {
                SmookerApplication.instance.onRemoteControlNotificationCloseRequest();
            }
        });

        toastText = reactOn(ADD_SMOKE, intent, toastText, new Reaction<String>() {
            @Override
            public String react(Intent intent) {
                SmookerApplication.instance.model().execute(AddSmoke.class, null);
                return context.getString(R.string.pattern_one_smoke_spend_with_value,
                        SmookerApplication.instance.getSmokePriceString());
            }
        });

        toastText = reactOn(SKIP_SMOKE, intent, toastText, new Reaction<String>(){
            @Override
            public String react(Intent intent) {
                SmookerApplication.instance.model().execute(CancelSmoke.class, SmokeCancelReason.SKIP);
                return context.getString(R.string.pattern_one_smoke_saved_with_value,
                        SmookerApplication.instance.getSmokePriceString());
            }
        });

        toastText = reactOn(POSTPONE_SMOKE, intent, toastText, new Reaction<String>(){
            @Override
            public String react(Intent intent) {
                SmookerApplication.instance.model().execute(CancelSmoke.class, SmokeCancelReason.POSTPONE);
                return context.getString(R.string.smoke_rescheduled);
            }
        });

        if (requested("toast",intent) && toastText != null){
            Toast.makeText(context, toastText,Toast.LENGTH_LONG).show();
        }

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
            super(name, pendingId, ActorSmoker.class);
        }
    }

}
