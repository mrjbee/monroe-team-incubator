package org.monroe.team.smooker.app.common;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import org.monroe.team.android.box.app.AndroidModel;
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

public class Model extends AndroidModel{

    private Context context;

    public Model(Context context) {
        super("SMOOKER", context);
    }

    @Override
    protected void constructor(String appName, Context context, ServiceRegistry serviceRegistry) {
        this.context = context;
        serviceRegistry.registrate(Model.class, this);

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
