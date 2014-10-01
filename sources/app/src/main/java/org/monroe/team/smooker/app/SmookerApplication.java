package org.monroe.team.smooker.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;
import android.widget.Toast;

import org.monroe.team.smooker.app.common.Model;
import org.monroe.team.smooker.app.common.Settings;
import org.monroe.team.smooker.app.common.SetupPage;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.uc.AddSmoke;
import org.monroe.team.smooker.app.uc.GetStatisticState;
import org.monroe.team.smooker.app.uc.UpdateQuitSmokeSchedule;
import org.monroe.team.smooker.app.uc.common.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class SmookerApplication extends Application {

    public static SmookerApplication instance;
    private Model model;

    private final static int NEXT_SCHEDULE_SMOKE_NOTIFICATION_ID = 337;
    private final static int QUIT_SMOKE_PROPOSAL_NOTIFICATION_ID = 333;
    private final static int QUIT_SMOKE_UPDATE_NOTIFICATION = 335;
    private final static int STATISTIC_UPDATE_NOTIFICATION = 336;
    private boolean isSmokeSuggestNotificationOpened = false;

    @Override public void onCreate() {
        super.onCreate();
        instance = this;
        if (!settings().has(Settings.APP_FIRST_TIME_DATE)){
            settings().set(Settings.APP_FIRST_TIME_DATE,DateUtils.now().getTime());
            scheduleAlarms();
        }
    }

    public synchronized Model getModel() {
        if (model == null){
            model = new Model(getApplicationContext());
            model.onCreate();
        }
        return model;
    }

    public void onRemoteControlNotificationCloseRequest() {
        getModel().stopNotificationControlService();
        updateStickyNotification(false);
        if (settings().getAndSet(Settings.FIRST_TIME_CLOSE_STICKY_NOTIFICATION, false)){
            Intent intent = new Intent(this, WizardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("PAGE_INDEX", 0);
            intent.putExtra("PAGE_STACK", new ArrayList<SetupPage>(Arrays.asList(SetupPage.UI)));
            intent.putExtra("FORCE", false);
            startActivity(intent);
        }
        closeSystemDialogs();
    }

    final public Settings settings() {
        return getModel().usingService(Settings.class);
    }


    public void onRemoteControlNotificationAddSmokeRequest() {
        getModel().execute(AddSmoke.class, null);
        Toast.makeText(this.getApplicationContext(), getString(R.string.one_smoke_added), Toast.LENGTH_SHORT).show();
        closeSystemDialogs();
    }


    public void closeSystemDialogs() {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.getApplicationContext().sendBroadcast(it);
    }

    public void updateStickyNotification(boolean enabled) {
        if (enabled){
            getModel().startNotificationControlService();
        } else {
            getModel().stopNotificationControlService();
        }
        settings().set(Settings.ENABLED_STICKY_NOTIFICATION, enabled);
    }

    public Pair<Boolean, List<SetupPage>> getRequiredSetupPages() {
        final List<SetupPage> answer = new ArrayList<SetupPage>(4);
        boolean required = false;

        if(settings().getAndSet(Settings.FIRST_TIME_ENTER_APP, false)){
            answer.add(SetupPage.WELCOME_PAGE);
            required = true;
        }

        if (!settings().has(Settings.SMOKE_PRICE)){
            answer.add(SetupPage.GENERAL);
            required = true;
        }

        return new Pair<Boolean, List<SetupPage>>(required,answer);
    }
    public boolean firstSetupDoneTrigger() {
        return settings().getAndSet(Settings.FIRST_TIME_AFTER_SETUP,false);
    }

    public void onSetupPageShown(SetupPage setupPage) {
        if (setupPage == SetupPage.UI){
            settings().set(Settings.FIRST_TIME_CLOSE_STICKY_NOTIFICATION, false);
        } else if(setupPage == SetupPage.QUIT_PROGRAM){
            settings().set(Settings.FIRST_TIME_QUIT_SMOKE_PAGE, false);
            NotificationManager manager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
            manager.cancel(QUIT_SMOKE_PROPOSAL_NOTIFICATION_ID);
        }
    }

    public void onDashboardCreate() {
        if (settings().get(Settings.ENABLED_STICKY_NOTIFICATION)){
            updateStickyNotification(true);
        }
        if (!settings().get(Settings.FIRST_TIME_AFTER_SETUP) && settings().get(Settings.FIRST_TIME_QUIT_SMOKE_PAGE)){
            NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            Intent dashBoardIntent = new Intent(this, DashboardActivity.class);
            dashBoardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dashBoardIntent.putExtra("PAGE_INDEX", 0);
            dashBoardIntent.putExtra("PAGE_STACK", new ArrayList<SetupPage>(Arrays.asList(SetupPage.QUIT_PROGRAM)));
            dashBoardIntent.putExtra("FORCE", false);

            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    DashboardActivity.WIZARD_ACTIVITY_REQUEST,
                    dashBoardIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);


            Intent dropNotificationIntent = new Intent(getApplicationContext(), SmokeQuitNotificationClosedReceiver.class);
            PendingIntent dropNotificationPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 2, dropNotificationIntent, 0);


            builder.setAutoCancel(true)
                    .setContentTitle(getString(R.string.quit_smoke_assistance_title))
                    .setContentText(getString(R.string.quit_smoking_program_suggestion))
                    .setSubText(getString(R.string.quit_smoke_program_suggestion_manual))
                    .setSmallIcon(R.drawable.notif_quit_assistance)
                    .setDeleteIntent(dropNotificationPendingIntent)
                    .setContentIntent(pendingIntent);

            manager.notify(QUIT_SMOKE_PROPOSAL_NOTIFICATION_ID, builder.build());
        }
    }

    public void scheduleAlarms() {

        scheduleNextSmokeAlarm();

        //Daily alarms

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Intent intent = new Intent(this, SystemAlarmReceiver.class);
        intent.putExtra("TIME_TO_UPDATE_STATISTICS",true);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 401, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);

        intent = new Intent(this, SystemAlarmReceiver.class);
        intent.putExtra("TIME_TO_NOTIFICATION_STATISTICS", true);
        alarmIntent = PendingIntent.getBroadcast(this, 402, intent, 0);

        alarmManager.setInexactRepeating(AlarmManager.RTC,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);


        intent = new Intent(this, SystemAlarmReceiver.class);
        intent.putExtra("TIME_TO_UPDATE_CALENDAR_WIDGET",true);
        alarmIntent = PendingIntent.getBroadcast(this, 403, intent, 0);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                AlarmManager.INTERVAL_HALF_HOUR,
                AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);


    }

    public CalendarWidget.CalendarWidgetUpdate fetchCalendarWidgetContent() {
        UpdateQuitSmokeSchedule.QuitSmokeSchedule smokeSchedule = getModel().execute(UpdateQuitSmokeSchedule.class, null);
        if (smokeSchedule == null || smokeSchedule.getNearestFuture() == null){
            //construct non smoking time schedule
            GetStatisticState.StatisticState state = model.execute(GetStatisticState.class, new GetStatisticState.StatisticRequest().with(GetStatisticState.StatisticName.LAST_LOGGED_SMOKE));
            long[] DHMS = DateUtils.splitPeriod(DateUtils.now(), state.getLastSmokeDate());

            String details = getString(R.string.short_min);
            String content = ""+DHMS[2];
            if (DHMS[0] > 0){
                details = getString(R.string.short_days);
                content =  ""+DHMS[0];
            } else if(DHMS[1] > 0){
                details = getString(R.string.short_hours);
                content =  ""+DHMS[1];
            }
            return new CalendarWidget.CalendarWidgetUpdate(
                    content,
                    details,
                    getString(R.string.time_since_last_smoke),
                    DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.SHORT).format(state.getLastSmokeDate())
            );
        }else {
            UpdateQuitSmokeSchedule.QuitSmokeSchedule.DayModel future = smokeSchedule.getNearestFuture();
            return new CalendarWidget.CalendarWidgetUpdate(
                    future.isToday()? getString(R.string.today):new SimpleDateFormat("dd").format(future.getDate()),
                    future.isToday()? new SimpleDateFormat("dd MMM yyyy").format(future.getDate()):new SimpleDateFormat("MMM yyyy").format(future.getDate()),
                    future.getText(),
                    getString(R.string.new_day_limit)
            );
        }
    }

    public void doMorningNotification() {
        if (settings().get(Settings.ENABLED_STATISTIC_NOTIFICATION)){
            GetStatisticState.StatisticState state =  getModel().execute(GetStatisticState.class, new GetStatisticState.StatisticRequest().with(
                    GetStatisticState.StatisticName.QUIT_SMOKE,
                    GetStatisticState.StatisticName.SMOKE_YESTERDAY,
                    GetStatisticState.StatisticName.SMOKE_TODAY));

            NotificationManager manager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);

            if (state.getQuitSmokeDifficult() != QuitSmokeDifficultLevel.DISABLED && state.getSmokeLimitChangedToday()){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setAutoCancel(true)
                        .setTicker(getString(R.string.smoke_limit_decreased))
                        .setContentTitle(getString(R.string.quit_smoke_assistance_title))
                        .setContentText(getString(R.string.smoke_limit_decreased))
                        .setSubText(getString(R.string.pattern_new_smoke_limit_wit_value, state.getTodaySmokeLimit()))
                        .setSmallIcon(R.drawable.notif_quit_assistance)
                        .setContentIntent(DashboardActivity.openDashboard(this));
                manager.notify(QUIT_SMOKE_UPDATE_NOTIFICATION, builder.build());
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setAutoCancel(true)
                    .setTicker(getString(R.string.smoke_statistic))
                    .setContentTitle(getString(R.string.smoke_statistic))
                    .setContentText(getString(R.string.pattern_yesterday_and_average_smokes_with_both_values,
                            state.getYesterdaySmokeDates().size(), state.getAverageSmoke()))
                    .setSubText((state.getQuitSmokeDifficult() != QuitSmokeDifficultLevel.DISABLED) ? getString(R.string.pattern_today_smoke_limit_with_value, state.getTodaySmokeLimit()) : null)
                    .setSmallIcon(R.drawable.notif_white_small)
                    .setContentIntent(DashboardActivity.openDashboard(this));
            manager.notify(STATISTIC_UPDATE_NOTIFICATION, builder.build());

        }
    }

    public List<Date> recalculateSmokingSchedule(Date scheduleStart, Date scheduleStop, int leftSmokes) {
        long period = scheduleStop.getTime() - scheduleStart.getTime();
        if (period < 0 ) return Collections.EMPTY_LIST;
        period = period/60000;
        int deltaMinutes = (int) period / leftSmokes;
        if (deltaMinutes < 30){
            deltaMinutes = 30;
            leftSmokes = (int) (period/deltaMinutes);
        }
        List<Date> answer = new ArrayList<Date>();
        for (int i = 1; i < leftSmokes + 1; i++){
            answer.add(DateUtils.mathMinutes(scheduleStart, deltaMinutes * i));
        }
        return answer;
    }

    public void scheduleNextSmokeAlarm() {

        cancelNextSmokeNotification(false);

        PendingIntent alarmIntent = SystemAlarmReceiver.createIntent(this, 410, "TIME_TO_NEXT_SMOKE");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        GetStatisticState.StatisticState statistics = getModel().execute(GetStatisticState.class, new GetStatisticState.StatisticRequest().with(
                GetStatisticState.StatisticName.QUIT_SMOKE,
                GetStatisticState.StatisticName.SMOKE_TODAY,
                GetStatisticState.StatisticName.LAST_LOGGED_SMOKE));

        if (statistics.getTodaySmokeLimit() > 1 && statistics.getTodaySmokeDates().size() > 0){
            int leftSmokes = statistics.getTodaySmokeLimit() - statistics.getTodaySmokeDates().size();
            if (leftSmokes > 0){
                long startMs = Math.max(settings().get(Settings.LAST_SMOKE_SUGGESTED_DATE),statistics.getLastSmokeDate().getTime());
                Date scheduleStart = new Date(startMs);
                Date scheduleStop = DateUtils.mathDays(DateUtils.dateOnly(DateUtils.now()),1);
                List<Date> scheduledSmokesList = recalculateSmokingSchedule(scheduleStart, scheduleStop, leftSmokes);
                if (!scheduledSmokesList.isEmpty()){
                    Date date = scheduledSmokesList.get(0);
                    //TODO: add notification without alarm
                    //schedule alarm
                    alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), alarmIntent);
                } else {
                    alarmManager.cancel(alarmIntent);
                }
            }else {
                    alarmManager.cancel(alarmIntent);
            }
        }else {
            alarmManager.cancel(alarmIntent);
        }
     }


    public void cancelNextSmokeNotification(boolean updateDate) {
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NEXT_SCHEDULE_SMOKE_NOTIFICATION_ID);
        if (updateDate) {
            isSmokeSuggestNotificationOpened = false;
            settings().set(Settings.LAST_SMOKE_SUGGESTED_DATE,DateUtils.now().getTime());
        }
    }

    public void showNextSmokeNotification() {

        if (!settings().get(Settings.ENABLED_ASSISTANCE_NOTIFICATION)) return;

        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        PendingIntent openApp = DashboardActivity.openDashboard(getApplicationContext());
        PendingIntent addSmoke = RemoteControlNotificationReceiver.createAddSmokeIntent(getApplicationContext());

        builder.setAutoCancel(true)
                .setContentTitle(getString(R.string.quit_smoke_assistance_title))
                .setContentText(getString(R.string.time_to_smoke))
                .setSubText(getString(R.string.not_to_smoke_suggestion))
                .setSmallIcon(R.drawable.notif_quit_assistance)
                .setContentIntent(openApp)
                .setDeleteIntent(SystemAlarmReceiver.createIntent(this, 411, "SKIP_SMOKE"))
                .addAction(R.drawable.notif_white_small, getString(R.string.skip_this_time), SystemAlarmReceiver.createIntent(this, 411, "SKIP_SMOKE"))
                .addAction(R.drawable.notif_orange_small, getString(R.string.add_one_smoke), addSmoke);

        if (!isSmokeSuggestNotificationOpened) {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(alarmSound);
            builder.setVibrate(new long[]{500, 500, 500, 500, 1000, 1000});
        }
        isSmokeSuggestNotificationOpened = true;
        manager.notify(NEXT_SCHEDULE_SMOKE_NOTIFICATION_ID, builder.build());
    }
}
