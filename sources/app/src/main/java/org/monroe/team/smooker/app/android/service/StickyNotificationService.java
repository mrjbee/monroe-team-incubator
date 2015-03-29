package org.monroe.team.smooker.app.android.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.actors.ActorSmoker;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.android.box.event.Event;
import org.monroe.team.smooker.app.uc.underreview.GetStatisticState;
import org.monroe.team.smooker.app.android.SmookerApplication;

public class StickyNotificationService extends Service {

    private static NotificationRemoteControl notification;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (notification == null) {
            notification = new NotificationRemoteControl();
            startForeground(
                    NotificationRemoteControl.NOTIFICATION_ID,
                    notification.createNotification(getNotificationInitialText()));
            Event.subscribeOnEvent(getApplicationContext(),this, Events.SMOKE_COUNT_CHANGED,new Closure<Integer, Void>() {
                @Override
                public Void execute(Integer smokeCount) {
                    updateText();
                    return null;
                }
            });

            Event.subscribeOnEvent(getApplicationContext(), this, Events.QUIT_SCHEDULE_UPDATED, new Closure<Boolean, Void>() {
                @Override
                public Void execute(Boolean arg) {
                    updateText();
                    return null;
                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateText() {
        GetStatisticState.StatisticState state = SmookerApplication.instance.model().execute(GetStatisticState.class,new GetStatisticState.StatisticRequest().with(
                GetStatisticState.StatisticName.SMOKE_TODAY,
                GetStatisticState.StatisticName.QUIT_SMOKE));
        String text = generateNotificationStringFor(state.getTodaySmokeDates().size());
        if (state.getTodaySmokeLimit() != null && state.getTodaySmokeLimit() > -1){
            int delta = state.getTodaySmokeLimit() - state.getTodaySmokeDates().size();
            if (delta >= 0){
                text = getString(R.string.pattern_left_for_today_with_value,delta);
            } else {
                text = getString(R.string.pattern_over_limit_for_today_with_value, Math.abs(delta));
            }
        }
        setText(text);
    }

    private String generateNotificationStringFor(Integer smokeCount) {
        return getString(R.string.pattern_smokes_today_with_value, smokeCount);
    }

    private String getNotificationInitialText() {
        GetStatisticState.StatisticState statisticState = ((SmookerApplication) getApplication()).model().execute(GetStatisticState.class,
                GetStatisticState.StatisticRequest.create(GetStatisticState.StatisticName.SMOKE_TODAY));
        return generateNotificationStringFor(statisticState.getTodaySmokeDates().size());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notification = null;
        Event.unSubscribeFromEvents(getApplicationContext(),this);
    }

    public static void setText(String text){
        if (notification == null) return;
        notification.setText(text);
    }

    public class NotificationRemoteControl {

        private static final int NOTIFICATION_ID = 23;

        public void setText(String text){
            Notification notification = createNotification(text);
            NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID,notification);
        }

        private Notification createNotification(String text) {

            // Using RemoteViews to bind custom layouts into Notification
            RemoteViews remoteViews = new RemoteViews(getPackageName(),
                    R.layout.remote_controll_notification);
            //PendingIntent pIntent = DashboardActivity.openDashboard(getApplicationContext());

            PendingIntent addBtnIntent = ActorSmoker.create(getApplicationContext(), ActorSmoker.ADD_SMOKE).buildDefault();


            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                    // Set Icon
                    .setSmallIcon(R.drawable.notif_orange_small)
                            // Set PendingIntent into Notification
                    .setContentIntent(addBtnIntent)
                    .setContentText(text)
                    .setContentTitle(getString(R.string.hit_for_smoke_log))
                            // Set RemoteViews into Notification
                    .setContent(remoteViews);

            remoteViews.setTextViewText(R.id.cn_title_text,text);


            PendingIntent closeBtnIntent = ActorSmoker.CLOSE_STICKY_NOTIFICATION.createPendingIntent(getApplicationContext());

         //   remoteViews.setOnClickPendingIntent(R.id.cn_add_btn,pIntent);
            remoteViews.setOnClickPendingIntent(R.id.cn_close_btn,closeBtnIntent);

            return builder.build();
        }

    }
}
