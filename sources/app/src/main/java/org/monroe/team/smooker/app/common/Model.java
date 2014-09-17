package org.monroe.team.smooker.app.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.monroe.team.smooker.app.RemoteControlNotificationService;
import org.monroe.team.smooker.app.db.DBHelper;
import org.monroe.team.smooker.app.db.TransactionManager;
import org.monroe.team.smooker.app.uc.common.UserCase;
import org.monroe.team.smooker.app.uc.common.UserCaseSupport;

public class Model {

    private final Registry registry = new Registry();
    private final Context context;

    public Model(Context applicationContext) {
        context = applicationContext;
        registry.registrate(Model.class, this);
        DBHelper dbHelper = new DBHelper(applicationContext);
        TransactionManager transactionManager = new TransactionManager(dbHelper);
        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("SMOOKER_Preferences", Context.MODE_PRIVATE);
        Preferences preferences = new Preferences(sharedPreferences);
        Settings settings = new Settings(sharedPreferences);
        EventMessenger messenger = new EventMessenger(applicationContext);
        registry.registrate(TransactionManager.class, transactionManager);
        registry.registrate(Preferences.class, preferences);
        registry.registrate(Settings.class, settings);
        registry.registrate(EventMessenger.class, messenger);
    }


    public <RequestType,ResponseType> ResponseType execute(
            Class<? extends UserCase<RequestType,ResponseType>> ucId,
            RequestType request){
        UserCase<RequestType,ResponseType> uc = getUserCase(ucId);
        return uc.execute(request);
    }

    private <RequestType,ResponseType> UserCase<RequestType, ResponseType> getUserCase(Class<? extends UserCase<RequestType, ResponseType>> ucId) {
        if (!registry.contains(ucId)){
            UserCase<RequestType, ResponseType> ucInstance;
            try {
                if (UserCaseSupport.class.isAssignableFrom(ucId)) {
                    ucInstance = ucId.getConstructor(Registry.class).newInstance(registry);
                } else {
                    ucInstance = ucId.newInstance();
                }
                registry.registrate((Class<UserCase<RequestType, ResponseType>>) ucId,ucInstance);
            } catch (Exception e) {
                throw new RuntimeException("Error during creating uc = "+ucId, e);
            }
        }
        return registry.get(ucId);
    }

    public void stopNotificationControlService() {
        context.stopService(new Intent(context,RemoteControlNotificationService.class));
    }

    public void startNotificationControlService() {
        context.startService(new Intent(context,RemoteControlNotificationService.class));
    }

    public void onCreate() {
        if (registry.get(Settings.class).get(Settings.ENABLED_STICKY_NOTIFICATION)) {
            startNotificationControlService();
        }
    }

    public <Type> Type usingService(Class<Type> serviceClass) {
        return registry.get(serviceClass);
    }

}
