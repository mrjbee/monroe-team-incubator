package org.monroe.team.smooker.app.android.controller;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import org.monroe.team.android.box.data.Data;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.utils.Closure;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.actors.ActorNotification;
import org.monroe.team.smooker.app.actors.ActorSystemAlarm;
import org.monroe.team.smooker.app.android.SmookerApplication;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.common.SmookerModel;
import org.monroe.team.android.box.event.Event;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeSchedule;

import java.util.Date;
import java.util.List;

/**
 * Excepted behavior
 * 1. should calculate next time smoke and daily schedule based on today smokes and limit
 * 2. should schedule alarm on time of next suggestion smoke
 * 3. should schedule new time of next suggestion smoke in case of condition changes (see 1)
 * 4. should close notification in case of new suggestion time (see 3) and notification opened
 *
 * Implementation notes
 * 1. On application startup check if suggested time not in a past
 * 1.1 if yes: show notification
 * 1.2 if no: schedule alarm
 *
 * TODO: subscribe on settings to understand if notification required
 */

public class SmokeScheduleController {

    @Deprecated
    private final Context context;
    @Deprecated
    private final SmookerModel smookerModel;

private final SmookerApplication application;

    private final static int NEXT_SCHEDULE_SMOKE_NOTIFICATION_ID = 337;


    public SmokeScheduleController(SmookerModel smookerModel, SmookerApplication application) {
        this.context = application;
        this.application = application;
        this.smookerModel = smookerModel;
    }

    public void onSmokeAlarm() {
        if (!isEnabled()) return;
        showNotification();
    }

    public boolean isEnabled() {
        return application.getSetting(Settings.ENABLED_ASSISTANCE_NOTIFICATION);
    }

    public void scheduleFallback() {
        AlarmManager alarmManager = smookerModel.usingService(AlarmManager.class);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                DateUtils.mathMinutes(DateUtils.now(), 5).getTime(),
                createAlarmIntent());
    }
    private void cancelSmokeNotification() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NEXT_SCHEDULE_SMOKE_NOTIFICATION_ID);
    }

    private void showNotification() {
        NotificationManager manager = smookerModel.usingService(NotificationManager.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        PendingIntent addSmoke = ActorNotification.create(context, ActorNotification.ADD_SMOKE).buildDefault();
        PendingIntent skipSmoke = ActorNotification.create(context, ActorNotification.SKIP_SMOKE).buildDefault();
        PendingIntent laterSmoke = ActorNotification.create(context, ActorNotification.POSTPONE_SMOKE).buildDefault();

        builder.setAutoCancel(true)
                .setContentTitle(getString(R.string.quit_smoke_assistance_title))
                .setContentText(getString(R.string.time_to_smoke))
                .setSmallIcon(R.drawable.notif_quit_assistance)
             //   .setContentIntent(DashboardActivity.openDashboardWithExtraAction(context, DashboardActivity.ExtraActionName.SMOKE_DECISION))
                .setDeleteIntent(skipSmoke)
                .addAction(R.drawable.notif_cancel_small, getString(R.string.skip_this_time), skipSmoke)
                .addAction(R.drawable.notif_clock_small, getString(R.string.later_this_time), laterSmoke)
                .addAction(R.drawable.notif_orange_small, getString(R.string.add_one_smoke), addSmoke);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);
        builder.setVibrate(new long[]{500, 500, 500, 500, 1000, 1000});
        manager.notify(NEXT_SCHEDULE_SMOKE_NOTIFICATION_ID, builder.build());
    }

    private void scheduleNextSmokeAlarm() {
        if (!isEnabled()) return;
        application.data_smokeSchedule().fetch(true, new Data.FetchObserver<PrepareTodaySmokeSchedule.TodaySmokeSchedule>() {
            @Override
            public void onFetch(PrepareTodaySmokeSchedule.TodaySmokeSchedule todaySmokeSchedule) {
                List<Date> smokeSuggestionList = todaySmokeSchedule.scheduledSmokes;
                scheduleNextSmokeAlarmImpl(smokeSuggestionList);
            }

            @Override
            public void onError(Data.FetchError fetchError) {
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {}
                        scheduleNextSmokeAlarm();
                    }
                }.start();
            }
        });
    }

    private void scheduleNextSmokeAlarmImpl(List<Date> smokeSuggestionList) {
        if (smokeSuggestionList.isEmpty()) return;
        Date notificationDate = smokeSuggestionList.get(0);
        if (notificationDate.compareTo(DateUtils.now()) <= 0){
            //PAST or NOW
            onSmokeAlarm();
        } else {
            AlarmManager alarmManager = smookerModel.usingService(AlarmManager.class);
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationDate.getTime(), createAlarmIntent());
        }
    }

    private PendingIntent createAlarmIntent() {
        return ActorSystemAlarm.createIntent(context, ActorSystemAlarm.Alarms.TIME_TO_NEXT_SMOKE);
    }

    public void initialize() {
        application.data_smokeSchedule().addDataChangeObserver(new Data.DataChangeObserver<PrepareTodaySmokeSchedule.TodaySmokeSchedule>() {
            @Override
            public void onDataInvalid() {
                cancelAlarmAndNotification();
                scheduleNextSmokeAlarm();
            }

            @Override
            public void onData(PrepareTodaySmokeSchedule.TodaySmokeSchedule todaySmokeSchedule) {
            }
        });

        cancelAlarmAndNotification();
        scheduleNextSmokeAlarm();
    }

    private void cancelAlarmIfAny() {
        smookerModel.usingService(AlarmManager.class).cancel(createAlarmIntent());
    }

    private CharSequence getString(int id) {
        return smookerModel.getString(id);
    }


    public void cancelAlarmAndNotification() {
        cancelAlarmIfAny();
        cancelSmokeNotification();
    }

    public void scheduleAlarm() {
        scheduleNextSmokeAlarm();
    }
}
