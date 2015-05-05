package org.monroe.team.smooker.app.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Pair;

import org.monroe.team.android.box.BitmapUtils;
import org.monroe.team.android.box.app.ApplicationSupport;
import org.monroe.team.android.box.data.DataManger;
import org.monroe.team.android.box.data.DataProvider;
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
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.constant.SmokeCancelReason;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeDifficultLevel;
import org.monroe.team.smooker.app.uc.AddSmoke;
import org.monroe.team.smooker.app.uc.CancelLastLoggedAction;
import org.monroe.team.smooker.app.uc.CancelSmoke;
import org.monroe.team.smooker.app.uc.GetDaySmokeSchedule;
import org.monroe.team.smooker.app.uc.GetLastAction;
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
import org.monroe.team.smooker.app.uc.common.ActionDetails;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;

public class SmookerApplication extends ApplicationSupport<SmookerModel> {

    public static SmookerApplication instance;

    private SmokeScheduleController suggestionsController;

    static {
      //  L.setup(new AndroidLogImplementation());
    }

    private Throwable exception;

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

    public String getSmokePriceString() {
        return settings().get(Settings.SMOKE_PRICE).toString() + " "
                + settings().getAs(Settings.CURRENCY_ID, Settings.CONVERT_CURRENCY).symbol;
    }

    public void addSmoke() {
        model().execute(AddSmoke.class,null, new org.monroe.team.corebox.app.Model.BackgroundResultCallback<Void>() {
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
                handleException(e, null);
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
                handleException(e, null);
            }
        });
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
                handleException(e, null);
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

    public void getSmokeQuitDetailsForDate(Date date, final Observer<PrepareSmokeQuitDateDetails.DateDetails> observer){
        model().execute(PrepareSmokeQuitDateDetails.class,date,new Model.BackgroundResultCallback<PrepareSmokeQuitDateDetails.DateDetails>() {
            @Override
            public void onResult(PrepareSmokeQuitDateDetails.DateDetails response) {
                observer.onSuccess(response);
            }

            @Override
            public void onFails(Throwable e) {
                handleException(e, observer);
            }
        });
    }

    private void handleException(Throwable e, Observer<?> observer) {
        if (observer != null && observer.onFail()){
            return;
        }
        processException(e);
    }

    public void saveImage(final InputStream fromIs, final Observer<String> observer) {
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
                        observer.onSuccess(s);
                    }
                });
            }

            @Override
            public void onFails(final Exception e) {
                model().ui(new Runnable() {
                    @Override
                    public void run() {
                        handleException(e, observer);
                    }
                });
            }
        });
    }

    public void removeData(boolean todayOnly, final Observer<Void> observer) {
        fetchValue(RemoveData.class, todayOnly, new NoOpValueAdapter<Void>(), new ValueObserver<Void>() {
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
            public void onFail(Throwable error) {
                handleException(error, observer);
            }
        });
    }

    public void getLastLoggedAction(final Observer<ActionDetails> valueObserver) {
        fetchValue(GetLastAction.class, null, new NoOpValueAdapter<ActionDetails>(), new ValueObserver<ActionDetails>() {
            @Override
            public void onSuccess(ActionDetails value) {
                valueObserver.onSuccess(value);
            }

            @Override
            public void onFail(Throwable exception) {
                handleException(exception,valueObserver);
            }
        });
    }

    public void removeLoggedAction(ActionDetails action, final Observer<Void> observer) {
        fetchValue(CancelLastLoggedAction.class, action, new NoOpValueAdapter<Void>(), new ValueObserver<Void>() {
            @Override
            public void onSuccess(Void value) {
                model().usingService(DataManger.class).invalidate(GetSmokeStatistic.SmokeStatistic.class);
                model().usingService(DataManger.class).invalidate(GetDaySmokeSchedule.SmokeSuggestion.class);

                model().getTodaySmokeDetailsDataProvider().invalidate();
                model().getTodaySmokeScheduleDataProvider().invalidate();
                model().getSmokeClockDataProvider().invalidate();
                model().getPeriodStatsProvider().invalidate();

                observer.onSuccess(value);
            }

            @Override
            public void onFail(Throwable error) {
                handleException(error, observer);
            }
        });
    }

    public void loadToBitmap(final String imageId, final int reqHeight, final int reqWidth, final Observer<Pair<String, Bitmap>> observer) {
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
                        observer.onSuccess(new Pair<String, Bitmap>(imageId, bitmap));
                    }
                });
            }

            @Override
            public void onFails(final Exception e) {
                model().ui(new Runnable() {
                    @Override
                    public void run() {
                        handleException(e, observer);
                    }
                });
            }
        });
    }

    @Override
    public void processException(Throwable e) {
        if (getSetting(Settings.ENABLED_BUG_SUBMISSION)) {
            exception = e;
            startActivity(new Intent(getApplicationContext(), BugSubmitActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            throw new RuntimeException(e);
        }
    }

    public Throwable getAwaitingError() {
        return exception;
    }

    public static abstract class Observer<ValueType>{
        public abstract void onSuccess(ValueType value);
        public boolean onFail() {return false;}
    }
}
