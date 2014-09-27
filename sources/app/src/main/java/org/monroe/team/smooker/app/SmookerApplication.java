package org.monroe.team.smooker.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

    private final static int QUIT_SMOKE_PROPOSAL_NOTIFICATION_ID = 333;
    private final static int QUIT_SMOKE_UPDATE_NOTIFICATION = 335;
    private final static int STATISTIC_UPDATE_NOTIFICATION = 336;

    @Override public void onCreate() {
        super.onCreate();
        instance = this;
        if (!settings().has(Settings.APP_FIRST_TIME_DATE)){
            settings().set(Settings.APP_FIRST_TIME_DATE,DateUtils.now().getTime());
        }
        scheduleAlarms();
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
        Toast.makeText(this.getApplicationContext(), "One smoke break added", Toast.LENGTH_SHORT).show();
        closeSystemDialogs();
    }


    private void closeSystemDialogs() {
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
                    .setContentTitle("Quit Smoking")
                    .setContentText("Choose a way to quit smoking")
                    .setSubText("... which fits to you")
                    .setSmallIcon(R.drawable.smooker_logo)
                    .setDeleteIntent(dropNotificationPendingIntent)
                    .setContentIntent(pendingIntent);

            manager.notify(QUIT_SMOKE_PROPOSAL_NOTIFICATION_ID, builder.build());
        }
    }

    public void scheduleAlarms() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);

        Intent intent = new Intent(this, SystemAlarmBroadcastReceiver.class);
        intent.putExtra("TIME_TO_UPDATE_STATISTICS",true);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 401, intent, 0);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 0);

        intent = new Intent(this, SystemAlarmBroadcastReceiver.class);
        intent.putExtra("TIME_TO_NOTIFICATION_STATISTICS",true);
        alarmIntent = PendingIntent.getBroadcast(this, 402, intent, 0);

        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 0);

        intent = new Intent(this, SystemAlarmBroadcastReceiver.class);
        intent.putExtra("TIME_TO_UPDATE_CALENDAR_WIDGET",true);
        alarmIntent = PendingIntent.getBroadcast(this, 403, intent, 0);

        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
    }

    public CalendarWidget.CalendarWidgetUpdate fetchCalendarWidgetContent() {
        UpdateQuitSmokeSchedule.QuitSmokeSchedule smokeSchedule = getModel().execute(UpdateQuitSmokeSchedule.class, null);
        if (smokeSchedule == null || smokeSchedule.getNearestFuture() == null){
            //construct non smoking time schedule
            GetStatisticState.StatisticState state = model.execute(GetStatisticState.class, new GetStatisticState.StatisticRequest().with(GetStatisticState.StatisticName.LAST_LOGGED_SMOKE));
            long[] DHMS = DateUtils.splitPeriod(DateUtils.now(), state.getLastSmokeDate());

            String details = "min";
            String content = ""+DHMS[2];
            if (DHMS[0] > 0){
                details = "days";
                content =  ""+DHMS[0];
            } else if(DHMS[1] > 0){
                details = "hours";
                content =  ""+DHMS[1];
            }
            return new CalendarWidget.CalendarWidgetUpdate(
                    content,
                    details,
                    "Time since last smoking",
                    DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.SHORT).format(state.getLastSmokeDate())
            );
        }else {
            UpdateQuitSmokeSchedule.QuitSmokeSchedule.DayModel future = smokeSchedule.getNearestFuture();
            return new CalendarWidget.CalendarWidgetUpdate(
                    future.isToday()? "Today":new SimpleDateFormat("dd").format(future.getDate()),
                    future.isToday()? new SimpleDateFormat("dd MMM yyyy").format(future.getDate()):new SimpleDateFormat("MMM yyyy").format(future.getDate()),
                    future.getText(),
                    "New day limit"
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
                        .setTicker("Smoke limit decreased")
                        .setContentTitle("Quit Smoking")
                        .setContentText("Smoke limit decreased")
                        .setSubText("New smoke limit "+state.getTodaySmokeLimit()+" smokes per day")
                        .setSmallIcon(R.drawable.notif_white_small)
                        .setContentIntent(DashboardActivity.openDashboard(this));
                manager.notify(QUIT_SMOKE_UPDATE_NOTIFICATION, builder.build());
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setAutoCancel(true)
                    .setTicker("Smoke statistic")
                    .setContentTitle("Smoke statistic")
                    .setContentText("Yesterday smokes "+state.getYesterdaySmokeDates().size()+" . Average per day "+ state.getAverageSmoke())
                    .setSubText((state.getQuitSmokeDifficult() != QuitSmokeDifficultLevel.DISABLED)?"Today smoke limit  "+state.getTodaySmokeLimit():null)
                    .setSmallIcon(R.drawable.notif_white_small)
                    .setContentIntent(DashboardActivity.openDashboard(this));
            manager.notify(STATISTIC_UPDATE_NOTIFICATION, builder.build());

        }
    }

    public List<Date> recalculateSmokingSchedule(Date scheduleStart, Date scheduleStop, int leftSmokes) {
        long period = scheduleStop.getTime() - scheduleStart.getTime();
        if (period < 0 ) return Collections.EMPTY_LIST;
        int deltaMinutes = (int) ((period / leftSmokes)/60000);
        List<Date> answer = new ArrayList<Date>();
        for (int i = 1; i < leftSmokes + 1; i++){
            answer.add(DateUtils.mathMinutes(scheduleStart, deltaMinutes * i));
        }
        return answer;
    }
}
