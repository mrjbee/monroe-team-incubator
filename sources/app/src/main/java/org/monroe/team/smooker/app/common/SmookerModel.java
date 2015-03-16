package org.monroe.team.smooker.app.common;

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
import org.monroe.team.smooker.app.android.service.StickyNotificationService;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.db.Dao;
import org.monroe.team.smooker.app.db.SmookerSchema;
import org.monroe.team.smooker.app.uc.CalculateSchedule;
import org.monroe.team.smooker.app.uc.GetBasicSmokeQuitDetails;
import org.monroe.team.smooker.app.uc.GetSmokeStatistic;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeDetails;
import org.monroe.team.smooker.app.uc.PrepareTodaySmokeSchedule;

public class SmookerModel extends AndroidModel{

    private Context context;
    private DataProvider<PrepareTodaySmokeDetails.TodaySmokeDetails> todaySmokeDetailsDataProvider;
    private UcDataProvider<PrepareTodaySmokeSchedule.TodaySmokeSchedule> todaySmokeScheduleDataProvider;

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
                put(GetBasicSmokeQuitDetails.BasicSmokeQuitDetails.class,
                        new UcDataProvider<GetBasicSmokeQuitDetails.BasicSmokeQuitDetails>(
                                SmookerModel.this,
                                context,
                                GetBasicSmokeQuitDetails.BasicSmokeQuitDetails.class,
                                GetBasicSmokeQuitDetails.class)
                );
                put(CalculateSchedule.SmokeSuggestion.class,
                        new UcDataProvider<CalculateSchedule.SmokeSuggestion>(
                                SmookerModel.this,
                                context,
                                CalculateSchedule.SmokeSuggestion.class,
                                CalculateSchedule.class
                        ));
            }
        });


        todaySmokeDetailsDataProvider = new UcDataProvider<PrepareTodaySmokeDetails.TodaySmokeDetails>(this, context,
                PrepareTodaySmokeDetails.TodaySmokeDetails.class,
                PrepareTodaySmokeDetails.class);

        todaySmokeScheduleDataProvider = new UcDataProvider<PrepareTodaySmokeSchedule.TodaySmokeSchedule>(this,
                context,
                PrepareTodaySmokeSchedule.TodaySmokeSchedule.class,
                PrepareTodaySmokeSchedule.class);

    }


    public DataProvider<PrepareTodaySmokeDetails.TodaySmokeDetails> getTodaySmokeDetailsDataProvider() {
        return todaySmokeDetailsDataProvider;
    }

    public UcDataProvider<PrepareTodaySmokeSchedule.TodaySmokeSchedule> getTodaySmokeScheduleDataProvider() {
        return todaySmokeScheduleDataProvider;
    }

    public void stopNotificationControlService() {
        context.stopService(new Intent(context,StickyNotificationService.class));
    }

    public void startNotificationControlService() {
        context.startService(new Intent(context,StickyNotificationService.class));
    }

    public void onCreate() {
        if (serviceRegistry.get(SettingManager.class).get(Settings.ENABLED_STICKY_NOTIFICATION)) {
            startNotificationControlService();
        }
    }

    public <Type> Type usingService(Class<Type> serviceClass) {
        return serviceRegistry.get(serviceClass);
    }

    public String getString(int id) {
        return context.getResources().getString(id);
    }
}