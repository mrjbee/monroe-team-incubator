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
    private ServiceHandler mServiceHandler;

    @Override
    public IBinder onBind(Intent intent) {
        Logs.MODEL.v("Model service onBind(). Intent = %s. Instance = %s", intent, this);
        return mPublicModelInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logs.MODEL.v("Model service onCreate() Instance = %s", this);
        HandlerThread thread = new HandlerThread("AAsModelService", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceHandler = new ServiceHandler(thread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logs.MODEL.i("Start model with service onStartCommand(). Intent = %s, " +
                "flags = %d, startId = %d. Service = %s", intent, flags, startId, this);
        mServiceHandler.sendMessage(mServiceHandler.obtainMessage());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.MODEL.v("Model service onDestroy() Instance = %s", this);
    }


    private class PublicModelImpl extends Binder implements PublicModel{

    }

    public static interface PublicModel extends IBinder {

    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            int iterateCount = 20;
            Logs.MODEL.d("Going to iterate %d times and stop service.", iterateCount);
            for (int i=0; i < iterateCount; i++){
                Logs.MODEL.d("Do nothing with using service. %d iteration.", i);
                try {
                    org.monroe.team.aas.ui.common.logging.Debug.v("Do nothing with using service [sleep]. %d iteration.", i);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logs.MODEL.w(e, "Do nothing with using service [error]. %d iteration.", i);
                }
            }
            Logs.MODEL.d("Going to stop service.");
            stopSelf();
        }
    }

}
