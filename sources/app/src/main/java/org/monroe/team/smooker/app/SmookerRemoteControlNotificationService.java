package org.monroe.team.smooker.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class SmookerRemoteControlNotificationService extends Service {

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
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public static void setText(String text){
        if (notification == null) return;
        notification.setText(text);
    }

    public String getNotificationInitialText() {
        return "Smooker. Smoke club.";
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

            // Open NotificationView Class on Notification Click
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            // Open NotificationView.java Activity
            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                    // Set Icon
                    .setSmallIcon(R.drawable.smooker_logo)
                            // Set PendingIntent into Notification
                    .setContentIntent(pIntent)
                            // Set RemoteViews into Notification
                    .setContent(remoteViews);

            remoteViews.setTextViewText(R.id.cn_title_text,text);
            Intent addActionIntent = new Intent(getApplicationContext(), AddSmokeReceiver.class);
            PendingIntent addBtnIntent = PendingIntent.getBroadcast(getApplicationContext(), 2, addActionIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.cn_add_btn,addBtnIntent);
            return builder.build();
        }
    }
}
