package org.monroe.team.smooker.app.android;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;

import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.data.DataManger;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.android.box.event.Event;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.android.box.utils.AndroidLogImplementation;
import org.monroe.team.corebox.log.L;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.app.actors.ActorSmoker;
import org.monroe.team.smooker.app.android.controller.SmokeScheduleController;
import org.monroe.team.smooker.app.R;
import org.monroe.team.smooker.app.actors.ActorSystemAlarm;
import org.monroe.team.smooker.app.common.SmookerModel;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.constant.SetupPage;
import org.monroe.team.smooker.app.uc.AddSmoke;
import org.monroe.team.smooker.app.uc.CalculateSchedule;
import org.monroe.team.smooker.app.uc.GetBasicSmokeQuitDetails;
import org.monroe.team.smooker.app.uc.GetSmokeStatistic;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeDetails;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeSchedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SmookerApplication extends ApplicationSupport<SmookerModel> {

    public static SmookerApplication instance;

    private SmokeScheduleController suggestionsController;

    private final static int QUIT_SMOKE_PROPOSAL_NOTIFICATION_ID = 333;
    private final static int QUIT_SMOKE_UPDATE_NOTIFICATION = 335;
    private final static int STATISTIC_UPDATE_NOTIFICATION = 336;

    static {
        L.setup(new AndroidLogImplementation());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        model().onCreate();
    }

    @Override
    protected void onPostCreate() {
        if (!settings().has(Settings.APP_FIRST_TIME_DATE)){
            settings().set(Settings.APP_FIRST_TIME_DATE, DateUtils.now().getTime());
            scheduleAlarms();
        }
        getSuggestionsController();
    }

    public synchronized SmokeScheduleController getSuggestionsController(){
        if (suggestionsController == null){
            suggestionsController = new SmokeScheduleController(this,model());
            suggestionsController.initialize();
        }
        return suggestionsController;
    }

    @Override
    protected SmookerModel createModel() {
        SmookerModel smookerModel = new SmookerModel(getApplicationContext());
        return smookerModel;
    }

    public void onRemoteControlNotificationCloseRequest() {
        model().stopNotificationControlService();
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

    final public SettingManager settings() {
        return model().usingService(SettingManager.class);
    }

    public void closeSystemDialogs() {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.getApplicationContext().sendBroadcast(it);
    }

    public void updateStickyNotification(boolean enabled) {
        if (enabled){
            model().startNotificationControlService();
        } else {
            model().stopNotificationControlService();
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
        if (!settings().get(Settings.FIRST_TIME_AFTER_SETUP)){
            doQuitSmokeSuggestionNotification();
        }
    }

    private void doQuitSmokeSuggestionNotification() {
        if (settings().get(Settings.FIRST_TIME_QUIT_SMOKE_PAGE)) {
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


            PendingIntent dropNotificationPendingIntent = ActorSmoker.CLOSE_QUIT_SUGGESTION.createPendingIntent(getApplicationContext());

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
        //Daily alarms
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        PendingIntent alarmIntent = ActorSystemAlarm.createIntent(this, ActorSystemAlarm.Alarms.TIME_TO_UPDATE_STATISTICS);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.HOUR, 8);
        calendar.set(Calendar.MINUTE, 0);

        alarmIntent = ActorSystemAlarm.createIntent(this, ActorSystemAlarm.Alarms.TIME_TO_NOTIFICATION_STATISTICS);

        alarmManager.setInexactRepeating(AlarmManager.RTC,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        alarmIntent = ActorSystemAlarm.createIntent(this, ActorSystemAlarm.Alarms.TIME_TO_UPDATE_CALENDAR_WIDGET);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                AlarmManager.INTERVAL_HALF_HOUR,
                AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);


    }

    public void doMorningNotification() {
     /*   if (settings().get(Settings.ENABLED_STATISTIC_NOTIFICATION)){
            GetStatisticState.StatisticState state =  model().execute(
                    GetStatisticState.class,
                    new GetStatisticState.StatisticRequest().with(
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
        doQuitSmokeSuggestionNotification();*/
    }

    public String getSmokePriceString() {
        return settings().get(Settings.SMOKE_PRICE).toString() + " "
                + settings().getAs(Settings.CURRENCY_ID, Settings.CONVERT_CURRENCY).symbol;
    }

    public void addSmoke() {
        model().execute(AddSmoke.class,null, new org.monroe.team.corebox.app.Model.BackgroundResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean response) {
                if (response){

                    model().usingService(DataManger.class).invalidate(GetSmokeStatistic.SmokeStatistic.class);
                    model().usingService(DataManger.class).invalidate(CalculateSchedule.SmokeSuggestion.class);
                    model().usingService(DataManger.class).invalidate(GetBasicSmokeQuitDetails.BasicSmokeQuitDetails.class);

                    model().getTodaySmokeDetailsDataProvider().invalidate();
                    model().getTodaySmokeScheduleDataProvider().invalidate();
                    model().getSmokeClockDataProvider().invalidate();

                } else {
                    warn(AddSmoke.class);
                }
            }

            @Override
            public void onFails(Throwable e) {
                warn(AddSmoke.class);
                debug_exception(e);
            }
        });
    }

    private void warn(Serializable warnData) {
        Event.send(getApplicationContext(), Events.WARNING, warnData);
    }

    public DataProvider<PrepareTodaySmokeDetails.TodaySmokeDetails> data_smokeDetails() {
        return model().getTodaySmokeDetailsDataProvider();
    }

    public DataProvider<PrepareTodaySmokeSchedule.TodaySmokeSchedule> data_smokeSchedule() {
        return model().getTodaySmokeScheduleDataProvider();
    }

    public DataProvider<org.monroe.team.smooker.app.uc.PrepareSmokeClockDetails.SmokeClockDetails> data_smokeClock() {
        return model().getSmokeClockDataProvider();
    }
}
