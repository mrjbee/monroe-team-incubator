package org.monroe.team.aas.model;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.os.Process;

import org.monroe.team.aas.ui.common.Logs;
import org.monroe.team.aas.ui.common.logging.*;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ModelService extends Service {

    private final PublicModel mPublicModelInstance = new PublicModelImpl();

    @Override
    public IBinder onBind(Intent intent) {
        return mPublicModelInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logs.MODEL.i("Start model with service onStartCommand(). Intent = %s, " +
                "flags = %d, startId = %d. Service = %s", intent, flags, startId, this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.MODEL.v("Model service onDestroy() Instance = %s", this);
    }


    private class PublicModelImpl extends Binder implements PublicModel{
        @Override
        public boolean isPublicGatewayEnabled() {
            return false;
        }
    }

    public static interface PublicModel extends IBinder {
        public boolean isPublicGatewayEnabled();
    }

}
