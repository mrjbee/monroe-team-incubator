package org.monroe.team.smooker.app.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Pair;

import org.monroe.team.android.box.BitmapUtils;
import org.monroe.team.android.box.actor.ActorAction;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.data.DataManger;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.android.box.event.Event;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.android.box.utils.AndroidLogImplementation;
import org.monroe.team.android.box.utils.FileUtils;
import org.monroe.team.corebox.app.Model;
import org.monroe.team.corebox.log.L;
import org.monroe.team.corebox.services.BackgroundTaskManager;
import org.monroe.team.corebox.utils.DateUtils;
import org.monroe.team.smooker.app.android.controller.SmokeQuitCalendarDisplayManager;
import org.monroe.team.smooker.app.android.controller.SmokeScheduleController;
import org.monroe.team.smooker.app.actors.ActorSystemAlarm;
import org.monroe.team.smooker.app.common.SmookerModel;
import org.monroe.team.smooker.app.common.constant.Events;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.constant.SetupPage;
import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.uc.AddSmoke;
import org.monroe.team.smooker.app.uc.CancelSmoke;
import org.monroe.team.smooker.app.uc.GetDaySmokeSchedule;
import org.monroe.team.smooker.app.uc.GetSmokeQuitDetails;
import org.monroe.team.smooker.app.uc.GetSmokeQuitSchedule;
import org.monroe.team.smooker.app.uc.GetSmokeStatistic;
import org.monroe.team.smooker.app.uc.OverNightUpdate;
import org.monroe.team.smooker.app.uc.PreparePeriodStatistic;
import org.monroe.team.smooker.app.uc.PrepareSmokeClockDetails;
import org.monroe.team.smooker.app.uc.PrepareSmokeQuitDateDetails;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeDetails;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeSchedule;
import org.monroe.team.smooker.app.uc.RemoveData;
import org.monroe.team.smooker.app.uc.SetupSmokeQuitProgram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

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
    }

    @Override
    protected void onPostCreate() {
        if (!settings().has(Settings.APP_FIRST_TIME_DATE)){
            settings().set(Settings.APP_FIRST_TIME_DATE, DateUtils.now().getTime());
        }
        scheduleAlarms();
        if (getSetting(Settings.ENABLED_STICKY_NOTIFICATION)) {
            model().startNotificationControlService();
        }
        doOverNightUpdate();
    }

    public synchronized SmokeScheduleController getSuggestionsController(){
        if (suggestionsController == null){
            suggestionsController = new SmokeScheduleController(model(),this);
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
    }

    final public SettingManager settings() {
        return model().usingService(SettingManager.class);
    }

    public void closeSystemDialogsOld() {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.getApplicationContext().sendBroadcast(it);
    }

    public void updateStickyNotification(boolean enabled) {
        boolean oldValue = settings().get(Settings.ENABLED_STICKY_NOTIFICATION);
        if (oldValue == enabled){
            return;
        }
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

    public void scheduleAlarms() {
        //If first time will call init which will schedule alarm
        getSuggestionsController();
        scheduleOvernightUpdateIfRequired();
    }

    private void scheduleOvernightUpdateIfRequired() {
        PendingIntent checkIntent = ActorSystemAlarm.ACTION_OVERNIGHT_UPDATE.checkPendingIntent(this);
        if (checkIntent != null) return;

        //Daily alarms
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        PendingIntent overNightUpdateIntent = ActorSystemAlarm.ACTION_OVERNIGHT_UPDATE.createPendingIntent(this);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, overNightUpdateIntent);
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
                    model().usingService(DataManger.class).invalidate(GetDaySmokeSchedule.SmokeSuggestion.class);
                    model().usingService(DataManger.class).invalidate(GetSmokeQuitDetails.Details.class);

                    model().getTodaySmokeDetailsDataProvider().invalidate();
                    model().getTodaySmokeScheduleDataProvider().invalidate();
                    model().getSmokeClockDataProvider().invalidate();
                    model().getPeriodStatsProvider().invalidate();

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

    public void changeQuitSmokeProgram(QuitSmokeDifficultLevel difficult, int startCount, int endCount) {
        model().execute(SetupSmokeQuitProgram.class, new SetupSmokeQuitProgram.QuitSmokeProgramRequest(difficult,startCount,endCount), new Model.BackgroundResultCallback<Void>() {
            @Override
            public void onResult(Void response) {
                model().usingService(DataManger.class).invalidate(GetSmokeQuitSchedule.QuitSchedule.class);
                model().usingService(DataManger.class).invalidate(GetSmokeQuitDetails.Details.class);
                model().usingService(DataManger.class).invalidate(GetDaySmokeSchedule.SmokeSuggestion.class);
                model().getTodaySmokeDetailsDataProvider().invalidate();
                model().getTodaySmokeScheduleDataProvider().invalidate();
                model().getBasicQuitSmokeDetailsProvider().invalidate();
            }

            @Override
            public void onFails(Throwable e) {
                warn(SetupSmokeQuitProgram.class);
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

    public DataProvider<PrepareSmokeClockDetails.SmokeClockDetails> data_smokeClock() {
        return model().getSmokeClockDataProvider();
    }

    public DataProvider<PreparePeriodStatistic.PeriodStatistic> data_periodStat() {
        return model().getPeriodStatsProvider();
    }

    public DataProvider<org.monroe.team.smooker.app.uc.PrepareSmokeQuitDetails.Details> data_smokeQuit() {
        return model().getBasicQuitSmokeDetailsProvider();
    }

    public DataProvider<SmookerModel.MoneyBoxTargetDescription> data_moneyBoxTarget() {
        return model().getMoneyBoxTargetDescriptionProvider();
    }


    public DataProvider<org.monroe.team.smooker.app.uc.PrepareMoneyBoxProgress.MoneyBoxProgress> data_moneyBoxProgress() {
        return model().getMoneyBoxProgressProvider();
    }


    public SmokeQuitCalendarDisplayManager getSmockQuitDataManager(){
        return model().usingService(SmokeQuitCalendarDisplayManager.class);
    }

    public void getSmokeQuitDetailsForDate(Date date, final OnDateDetailsObserver observer){
        model().execute(PrepareSmokeQuitDateDetails.class,date,new Model.BackgroundResultCallback<PrepareSmokeQuitDateDetails.DateDetails>() {
            @Override
            public void onResult(PrepareSmokeQuitDateDetails.DateDetails response) {
                observer.onResult(response);
            }

            @Override
            public void onFails(Throwable e) {
                debug_exception(e);
                observer.onFail();
            }
        });
    }

    public void saveImage(final InputStream fromIs, final OnSaveImageObserver observer) {
        model().usingService(BackgroundTaskManager.class).execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                File saveFile =  FileUtils.storageFile(getApplicationContext(), FileUtils.timeName());
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(saveFile);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                byte[] buffer = new byte[1024];
                int length;

                try {
                    while((length = fromIs.read(buffer)) > 0){
                        outputStream.write(buffer, 0, length);
                    }
                    return saveFile.getAbsolutePath();
                } catch (IOException e) {
                    if (outputStream != null){
                        try {
                            outputStream.close();
                            outputStream = null;
                        } catch (IOException e1) {}
                        saveFile.delete();
                    }
                    throw new RuntimeException(e);
                } finally {
                    if (outputStream != null){
                        try {
                            outputStream.flush();
                        } catch (IOException e) {throw new RuntimeException("Flush error", e);}
                        try {
                            outputStream.close();
                        } catch (IOException e) {}
                    }
                    if (fromIs != null){
                        try {
                            fromIs.close();
                        } catch (IOException e) {}
                    }
                }
            }
        }, new BackgroundTaskManager.TaskCompletionNotificationObserver<String>() {
            @Override
            public void onSuccess(final String s) {
                model().ui(new Runnable() {
                    @Override
                    public void run() {
                        observer.onResult(s);
                    }
                });
            }

            @Override
            public void onFails(final Exception e) {
                model().ui(new Runnable() {
                    @Override
                    public void run() {
                        debug_exception(e);
                        observer.onFail();
                    }
                });
            }
        });
    }

    public void loadToBitmap(final String imageId, final int reqHeight, final int reqWidth, final OnImageLoadedObserver observer) {
        model().usingService(BackgroundTaskManager.class).execute(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                File file = new File(imageId);
                if (!file.exists()){
                    throw new RuntimeException("File not exists = "+imageId);
                }
                return BitmapUtils.decodeBitmap(BitmapUtils.fromFile(file),
                        reqWidth,
                        reqHeight);
            }
        }, new BackgroundTaskManager.TaskCompletionNotificationObserver<Bitmap>() {
            @Override
            public void onSuccess(final Bitmap bitmap) {
                model().ui(new Runnable() {
                    @Override
                    public void run() {
                        observer.onResult(imageId, bitmap);
                    }
                });
            }

            @Override
            public void onFails(final Exception e) {
                model().ui(new Runnable() {
                    @Override
                    public void run() {
                        debug_exception(e);
                        observer.onFail();
                    }
                });
            }
        });
    }

    public void deleteImage(String newImageId) {
        new File(newImageId).delete();
    }


    public void changeMoneyBoxTargetDescription() {
        model().getMoneyBoxTargetDescriptionProvider().invalidate();
    }


    public void changeMoneyBoxTarget() {
        model().getMoneyBoxProgressProvider().invalidate();
    }

    public boolean isStickyNotificationEnabled() {
        return settings().get(Settings.ENABLED_STICKY_NOTIFICATION);
    }

    public boolean isAssistantNotificationEnabled() {
        return getSuggestionsController().isEnabled();
    }

    public void enableAssistantNotifications(boolean isChecked) {
        if (isAssistantNotificationEnabled() == isChecked){
            return;
        }
        setSetting(Settings.ENABLED_ASSISTANCE_NOTIFICATION, isChecked);
        if (!isAssistantNotificationEnabled()){
            //cancel alarm and notification
            getSuggestionsController().cancelAlarmAndNotification();
        }else{
            //schedule alarm
            getSuggestionsController().scheduleAlarm();
        }
    }

    public void skipSmoke(SmokeCancelReason reason) {
        model().execute(CancelSmoke.class,reason,new Model.BackgroundResultCallback<Void>() {
            @Override
            public void onResult(Void response) {
                model().usingService(DataManger.class).invalidate(GetDaySmokeSchedule.SmokeSuggestion.class);
                data_smokeSchedule().invalidate();
                data_smokeClock().invalidate();
            }

            @Override
            public void onFails(Throwable e) {
                debug_exception(e);
                warn(CancelSmoke.class);
            }
        });
    }

    public void doOverNightUpdate() {
        model().execute(OverNightUpdate.class,null, new Model.BackgroundResultCallback<Void>() {
            @Override
            public void onResult(Void response) {
                model().usingService(DataManger.class).invalidate(GetSmokeStatistic.SmokeStatistic.class);
                model().usingService(DataManger.class).invalidate(GetDaySmokeSchedule.SmokeSuggestion.class);
                model().usingService(DataManger.class).invalidate(GetSmokeQuitDetails.Details.class);
                model().getTodaySmokeDetailsDataProvider().invalidate();
                model().getTodaySmokeScheduleDataProvider().invalidate();
                model().getSmokeClockDataProvider().invalidate();
                model().getPeriodStatsProvider().invalidate();
            }

            @Override
            public void onFails(Throwable e) {
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            sleep(2000);
                        } catch (InterruptedException e1) {

                        }
                        doOverNightUpdate();
                    }
                }.start();
            }
        });
    }

    public void removeData(boolean todayOnly, final ValueObserver<Void> observer) {
        fetchValue(RemoveData.class,todayOnly,new NoOpValueAdapter<Void>(), new ValueObserver<Void>() {
            @Override
            public void onSuccess(Void value) {
                model().usingService(DataManger.class).invalidate(GetSmokeQuitSchedule.QuitSchedule.class);
                model().usingService(DataManger.class).invalidate(GetSmokeStatistic.SmokeStatistic.class);
                model().usingService(DataManger.class).invalidate(GetDaySmokeSchedule.SmokeSuggestion.class);
                model().usingService(DataManger.class).invalidate(GetSmokeQuitDetails.Details.class);

                model().getTodaySmokeDetailsDataProvider().invalidate();
                model().getTodaySmokeScheduleDataProvider().invalidate();
                model().getSmokeClockDataProvider().invalidate();
                model().getPeriodStatsProvider().invalidate();
                model().getBasicQuitSmokeDetailsProvider().invalidate();

                observer.onSuccess(value);
            }

            @Override
            public void onFail(int errorCode) {
                observer.onFail(errorCode);
            }
        });
    }

    public static interface OnImageLoadedObserver {
        public void onResult(String imageId, Bitmap bitmap);
        public void onFail();
    }


    public static interface OnSaveImageObserver {
        public void onResult(String imageId);
        public void onFail();
    }

    public static interface OnDateDetailsObserver{
        public void onResult(PrepareSmokeQuitDateDetails.DateDetails details);
        public void onFail();
    }

}
