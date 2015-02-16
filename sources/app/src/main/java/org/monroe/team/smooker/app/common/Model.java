package org.monroe.team.smooker.app.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.monroe.team.android.box.services.AndroidServiceRegistry;
import org.monroe.team.android.box.services.EventMessenger;
import org.monroe.team.android.box.services.SettingManager;
import org.monroe.team.corebox.services.ServiceRegistry;
import org.monroe.team.smooker.app.android.service.StickyNotificationService;
import org.monroe.team.smooker.app.common.constant.Settings;
import org.monroe.team.smooker.app.common.quitsmoke.QuitSmokeProgramManager;
import org.monroe.team.smooker.app.db.DBHelper;
import org.monroe.team.smooker.app.db.TransactionManager;
import org.monroe.team.smooker.app.uc.common.UserCase;
import org.monroe.team.smooker.app.uc.common.UserCaseSupport;

public class Model {

    private final ServiceRegistry serviceRegistry;
    private final Context context;

    public Model(Context context) {
        this.context = context;
        serviceRegistry = new AndroidServiceRegistry(context);
        serviceRegistry.registrate(Model.class, this);
        DBHelper dbHelper = new DBHelper(context);
        TransactionManager transactionManager = new TransactionManager(dbHelper);
        SharedPreferences sharedPreferences = context.getSharedPreferences("SMOOKER_Preferences", Context.MODE_PRIVATE);
        SettingManager settingManager = new SettingManager(sharedPreferences);
        EventMessenger messenger = new EventMessenger(context);
        serviceRegistry.registrate(TransactionManager.class, transactionManager);
        serviceRegistry.registrate(SettingManager.class, settingManager);
        serviceRegistry.registrate(EventMessenger.class, messenger);
        serviceRegistry.registrate(QuitSmokeProgramManager.class, new QuitSmokeProgramManager(this.context));
    }


    public <RequestType,ResponseType> ResponseType execute(
            Class<? extends UserCase<RequestType,ResponseType>> ucId,
            RequestType request){
        UserCase<RequestType,ResponseType> uc = getUserCase(ucId);
        return uc.execute(request);
    }

    private <RequestType,ResponseType> UserCase<RequestType, ResponseType> getUserCase(Class<? extends UserCase<RequestType, ResponseType>> ucId) {
        if (!serviceRegistry.contains(ucId)){
            UserCase<RequestType, ResponseType> ucInstance;
            try {
                if (UserCaseSupport.class.isAssignableFrom(ucId)) {
                    ucInstance = ucId.getConstructor(ServiceRegistry.class).newInstance(serviceRegistry);
                } else {
                    ucInstance = ucId.newInstance();
                }
                serviceRegistry.registrate((Class<UserCase<RequestType, ResponseType>>) ucId,ucInstance);
            } catch (Exception e) {
                throw new RuntimeException("Error during creating uc = "+ucId, e);
            }
        }
        return serviceRegistry.get(ucId);
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
