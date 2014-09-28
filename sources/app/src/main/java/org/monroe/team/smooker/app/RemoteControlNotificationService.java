package org.monroe.team.smooker.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.monroe.team.smooker.app.common.Closure;
import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeData;
import org.monroe.team.smooker.app.event.Event;
import org.monroe.team.smooker.app.uc.GetStatisticState;

public class RemoteControlNotificationService extends Service {

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
                public Void execute(Integer arg) {
                    GetStatisticState.StatisticState state = SmookerApplication.instance.getModel().execute(GetStatisticState.class,new GetStatisticState.StatisticRequest().with(GetStatisticState.StatisticName.SMOKE_TODAY, GetStatisticState.StatisticName.QUIT_SMOKE));
                    String text = generateNotificationStringFor(arg);
                    if (state.getTodaySmokeLimit() != null && state.getTodaySmokeLimit() > -1){
                        int delta = state.getTodaySmokeLimit() - state.getTodaySmokeDates().size();
                        if (delta >= 0){
                            text = delta +" smokes left for today";
                        } else {
                            text = Math.abs(delta) +" smokes over limit today";
                        }
                    }
                    setText(text);
                    return null;
                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private String generateNotificationStringFor(Integer smokeCount) {
        return " "+smokeCount +" smoke breaks today";
    }

    private String getNotificationInitialText() {
        GetStatisticState.StatisticState statisticState = ((SmookerApplication) getApplication()).getModel().execute(GetStatisticState.class,
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
            PendingIntent pIntent = DashboardActivity.openDashboard(getApplicationContext());

            PendingIntent addBtnIntent = RemoteControlNotificationReceiver.createAddSmokeIntent(getApplicationContext());


            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                    // Set Icon
                    .setSmallIcon(R.drawable.notif_orange_small)
                            // Set PendingIntent into Notification
                    .setContentIntent(addBtnIntent)
                    .setContentText(text)
                    .setContentTitle("Press for +1 smoke")
                            // Set RemoteViews into Notification
                    .setContent(remoteViews);

            remoteViews.setTextViewText(R.id.cn_title_text,text);


            Intent closeActionIntent = new Intent(getApplicationContext(), RemoteControlNotificationReceiver.class);
            closeActionIntent.putExtra("CLOSE",true);
            PendingIntent closeBtnIntent = PendingIntent.getBroadcast(getApplicationContext(), 3, closeActionIntent, 0);

            remoteViews.setOnClickPendingIntent(R.id.cn_root_view,pIntent);
            remoteViews.setOnClickPendingIntent(R.id.cn_add_btn,addBtnIntent);
            remoteViews.setOnClickPendingIntent(R.id.cn_close_btn,closeBtnIntent);

            return builder.build();
        }

    }
}
