package org.monroe.team.smooker.promo.common;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import org.monroe.team.android.box.app.AndroidModel;
import org.monroe.team.android.box.data.DataManger;
import org.monroe.team.android.box.data.DataProvider;
import org.monroe.team.android.box.data.UcDataProvider;
import org.monroe.team.android.box.db.DAOFactory;
import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.DBHelper;
import org.monroe.team.android.box.db.TransactionManager;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.promo.android.controller.SmokeQuitCalendarDisplayManager;
import org.monroe.team.smooker.promo.android.service.StickyNotificationService;
import org.monroe.team.smooker.promo.common.constant.Settings;
import org.monroe.team.smooker.promo.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.promo.db.Dao;
import org.monroe.team.smooker.promo.db.SmookerSchema;
import org.monroe.team.smooker.promo.uc.GetDaySmokeSchedule;
import org.monroe.team.smooker.promo.uc.GetSmokeQuitDetails;
import org.monroe.team.smooker.promo.uc.GetSmokeQuitSchedule;
import org.monroe.team.smooker.promo.uc.PrepareMoneyBoxProgress;
import org.monroe.team.smooker.promo.uc.PreparePeriodStatistic;
import org.monroe.team.smooker.promo.uc.GetSmokeStatistic;
import org.monroe.team.smooker.promo.uc.PrepareSmokeClockDetails;
import org.monroe.team.smooker.promo.uc.PrepareSmokeQuitDetails;
import org.monroe.team.smooker.promo.uc.PrepareTodaySmokeDetails;
import org.monroe.team.smooker.promo.uc.PrepareTodaySmokeSchedule;

import java.io.Serializable;

public class SmookerModel extends AndroidModel{

    private Context context;
    private DataProvider<PrepareTodaySmokeDetails.TodaySmokeDetails> todaySmokeDetailsDataProvider;
    private UcDataProvider<PrepareTodaySmokeSchedule.TodaySmokeSchedule> todaySmokeScheduleDataProvider;
    private UcDataProvider<PrepareSmokeClockDetails.SmokeClockDetails> smokeClockDataProvider;
    private UcDataProvider<PreparePeriodStatistic.PeriodStatistic> periodStatsProvider;
    private UcDataProvider<PrepareSmokeQuitDetails.Details> basicQuitSmokeDetailsProvider;
    private DataProvider<MoneyBoxTargetDescription> moneyBoxTargetDescriptionProvider;
    private UcDataProvider<PrepareMoneyBoxProgress.MoneyBoxProgress> moneyBoxProgressProvider;

    public SmookerModel(Context context) {
        super("SMOOKER", context);
    }

    @Override
    protected void constructor(String appName, final Context context, ServiceRegistry serviceRegistry) {
        this.context = context;
        serviceRegistry.registrate(SmookerModel.class, this);

        final SmookerSchema schema = new SmookerSchema();
        DBHelper helper = new DBHelper(context, schema);
        TransactionManager transactionManager = new TransactionManager(helper, new DAOFactory() {
            @Override
            public DAOSupport createInstanceFor(SQLiteDatabase database) {
                return new Dao(database, schema);
            }
        });
        serviceRegistry.registrate(TransactionManager.class, transactionManager);
        serviceRegistry.registrate(QuitSmokeProgramManager.class, new QuitSmokeProgramManager(this.context));

        final DataProvider<GetSmokeQuitSchedule.QuitSchedule> smokeQuitScheduleDataProvider = new UcDataProvider<GetSmokeQuitSchedule.QuitSchedule>(
                SmookerModel.this,
                context,
                GetSmokeQuitSchedule.QuitSchedule.class,
                GetSmokeQuitSchedule.class
        );

        serviceRegistry.registrate(SmokeQuitCalendarDisplayManager.class,
                new SmokeQuitCalendarDisplayManager(smokeQuitScheduleDataProvider));

        serviceRegistry.registrate(DataManger.class, new DataManger() {
            @Override
            protected void construct() {
                put(GetSmokeStatistic.SmokeStatistic.class,
                        new UcDataProvider<GetSmokeStatistic.SmokeStatistic>(
                                SmookerModel.this,
                                context,
                                GetSmokeStatistic.SmokeStatistic.class,
                                GetSmokeStatistic.class)
                );
                put(GetSmokeQuitDetails.Details.class,
                        new UcDataProvider<GetSmokeQuitDetails.Details>(
                                SmookerModel.this,
                                context,
                                GetSmokeQuitDetails.Details.class,
                                GetSmokeQuitDetails.class)
                );
                put(GetDaySmokeSchedule.SmokeSuggestion.class,
                        new UcDataProvider<GetDaySmokeSchedule.SmokeSuggestion>(
                                SmookerModel.this,
                                context,
                                GetDaySmokeSchedule.SmokeSuggestion.class,
                                GetDaySmokeSchedule.class
                        ));

                put(GetSmokeQuitSchedule.QuitSchedule.class,smokeQuitScheduleDataProvider);

            }
        });



        todaySmokeDetailsDataProvider = new UcDataProvider<PrepareTodaySmokeDetails.TodaySmokeDetails>(this, context,
                PrepareTodaySmokeDetails.TodaySmokeDetails.class,
                PrepareTodaySmokeDetails.class);

        todaySmokeScheduleDataProvider = new UcDataProvider<PrepareTodaySmokeSchedule.TodaySmokeSchedule>(this,
                context,
                PrepareTodaySmokeSchedule.TodaySmokeSchedule.class,
                PrepareTodaySmokeSchedule.class);

        smokeClockDataProvider = new UcDataProvider<PrepareSmokeClockDetails.SmokeClockDetails>(this,
                context,
                PrepareSmokeClockDetails.SmokeClockDetails.class,
                PrepareSmokeClockDetails.class);

        periodStatsProvider = new UcDataProvider<PreparePeriodStatistic.PeriodStatistic>(this,
                context,
                PreparePeriodStatistic.PeriodStatistic.class,
                PreparePeriodStatistic.class);

        basicQuitSmokeDetailsProvider = new UcDataProvider<PrepareSmokeQuitDetails.Details>(this,
                context,
                PrepareSmokeQuitDetails.Details.class,
                PrepareSmokeQuitDetails.class);

        moneyBoxProgressProvider = new UcDataProvider<PrepareMoneyBoxProgress.MoneyBoxProgress>(this,
                context,
                PrepareMoneyBoxProgress.MoneyBoxProgress.class,
                PrepareMoneyBoxProgress.class);

        moneyBoxTargetDescriptionProvider = new DataProvider<MoneyBoxTargetDescription>(MoneyBoxTargetDescription.class,this,context){
            @Override
            protected MoneyBoxTargetDescription provideData() {
                return new MoneyBoxTargetDescription(
                        usingService(SettingManager.class).get(Settings.MONEYBOX_SOMETHING_IMAGE_ID),
                        usingService(SettingManager.class).get(Settings.MONEYBOX_SOMETHING_TITLE),
                        usingService(SettingManager.class).get(Settings.MONEYBOX_SOMETHING_DESCRIPTION));
            }
        };

    }


    public DataProvider<PrepareTodaySmokeDetails.TodaySmokeDetails> getTodaySmokeDetailsDataProvider() {
        return todaySmokeDetailsDataProvider;
    }

    public UcDataProvider<PrepareTodaySmokeSchedule.TodaySmokeSchedule> getTodaySmokeScheduleDataProvider() {
        return todaySmokeScheduleDataProvider;
    }

    public UcDataProvider<PrepareSmokeClockDetails.SmokeClockDetails> getSmokeClockDataProvider() {
        return smokeClockDataProvider;
    }

    public UcDataProvider<PreparePeriodStatistic.PeriodStatistic> getPeriodStatsProvider() {
        return periodStatsProvider;
    }

    public UcDataProvider<PrepareSmokeQuitDetails.Details> getBasicQuitSmokeDetailsProvider() {
        return basicQuitSmokeDetailsProvider;
    }

    public DataProvider<MoneyBoxTargetDescription> getMoneyBoxTargetDescriptionProvider() {
        return moneyBoxTargetDescriptionProvider;
    }

    public UcDataProvider<PrepareMoneyBoxProgress.MoneyBoxProgress> getMoneyBoxProgressProvider() {
        return moneyBoxProgressProvider;
    }

    public void stopNotificationControlService() {
        context.stopService(new Intent(context,StickyNotificationService.class));
    }

    public void startNotificationControlService() {
        context.startService(new Intent(context,StickyNotificationService.class));
    }


    public <Type> Type usingService(Class<Type> serviceClass) {
        return serviceRegistry.get(serviceClass);
    }

    public String getString(int id) {
        return context.getResources().getString(id);
    }

    public static class MoneyBoxTargetDescription implements Serializable {
        public final String imageId;
        public final String title;
        public final String description;

        public MoneyBoxTargetDescription(String imageId, String title, String description) {
            this.imageId = imageId;
            this.title = title;
            this.description = description;
        }

        public boolean isActivated(){
            return title != null;
        }
    }
}