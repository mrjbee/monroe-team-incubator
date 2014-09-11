package org.monroe.team.smooker.app.common;

import android.content.Context;
import android.content.SharedPreferences;

import org.monroe.team.smooker.app.dp.DBHelper;
import org.monroe.team.smooker.app.dp.TransactionManager;
import org.monroe.team.smooker.app.uc.common.UserCase;
import org.monroe.team.smooker.app.uc.common.UserCaseSupport;

public class Model {

    private final Registry registry = new Registry();

    public Model(Context applicationContext) {
        DBHelper dbHelper = new DBHelper(applicationContext);
        TransactionManager transactionManager = new TransactionManager(dbHelper);
        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("SMOOKER_Preferences", Context.MODE_PRIVATE);
        Preferences preferences = new Preferences(sharedPreferences);

        registry.registrate(TransactionManager.class, transactionManager);
        registry.registrate(Preferences.class, preferences);
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
}
