package org.monroe.team.smooker.promo.android.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;

import org.monroe.team.android.box.data.Data;
import org.monroe.team.smooker.promo.R;
import org.monroe.team.smooker.promo.actors.ActorNotification;
import org.monroe.team.smooker.promo.android.FrontPageFragment;
import org.monroe.team.android.box.event.Event;
import org.monroe.team.smooker.promo.uc.PrepareTodaySmokeDetails;
import org.monroe.team.smooker.promo.uc.underreview.GetStatisticState;
import org.monroe.team.smooker.promo.android.SmookerApplication;

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

            SmookerApplication.instance.data_smokeDetails().addDataChangeObserver(new Data.DataChangeObserver<PrepareTodaySmokeDetails.TodaySmokeDetails>() {
                @Override
                public void onDataInvalid() {
                    fetchData();
                }

                @Override
                public void onData(PrepareTodaySmokeDetails.TodaySmokeDetails todaySmokeDetails) {

                }
            });
            fetchData();

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void fetchData() {
        SmookerApplication.instance.data_smokeDetails().fetch(true, new Data.FetchObserver<PrepareTodaySmokeDetails.TodaySmokeDetails>() {
            @Override
            public void onFetch(PrepareTodaySmokeDetails.TodaySmokeDetails todaySmokeDetails) {
                Pair<String,String> stat = FrontPageFragment.toSimpleString(SmookerApplication.instance, todaySmokeDetails);
                setText(stat.second+" "+stat.first);
            }

            @Override
            public void onError(Data.FetchError fetchError) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                        fetchData();
                    }
                }.start();
            }
        });
    }

    private String generateNotificationStringFor(Integer smokeCount) {
        return getString(R.string.dashboard_today_smokes) +  smokeCount;
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

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.notification_cigareta)
                    .setContentIntent(ActorNotification.ADD_SMOKE.createPendingIntent(getApplicationContext()))
                    .setContentTitle(text)
                    .setContentText(getString(R.string.notification_sticky_text))
                    .setShowWhen(false)
                    .setWhen(0)
                    .setOngoing(true)
                    .addAction(0,getString(R.string.notification_sticky_action_dashboard), ActorNotification.OPEN_DASHBOARD.createPendingIntent(getApplicationContext()))
                    .addAction(0,getString(R.string.notification_sticky_action_option), ActorNotification.CLOSE_STICKY_NOTIFICATION.createPendingIntent(getApplicationContext()));

            return builder.build();
        }

    }
}