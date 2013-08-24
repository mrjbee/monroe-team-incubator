package org.monroe.team.aas.common.model;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import org.monroe.team.aas.ui.common.Logs;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public abstract class ModelService <ModelClass> extends Service {

    private IBinder mModelInstance;
    private final ClientBindingHandlingStrategy mClientBindingHandlingStrategy;
    private AtomicBoolean mFirstStart = new AtomicBoolean(true);

    protected ModelService(ClientBindingHandlingStrategy mClientBindingHandlingStrategy) {
        this.mClientBindingHandlingStrategy = mClientBindingHandlingStrategy;
    }

    public ModelService() {
        this(new NoOpClientBindingHandlingStrategy());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logs.SERVICE.v("onBind() service = %s", this);
        mClientBindingHandlingStrategy.onClientBind(this);
        return mModelInstance;
    }

    @Override
    public void onRebind(Intent intent) {
        Logs.SERVICE.v("onRebind() service = %s", this);
        super.onRebind(intent);
        mClientBindingHandlingStrategy.onClientBind(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logs.SERVICE.v("onUnbind() service = %s", this);
        mClientBindingHandlingStrategy.onClientUnbind(this);
        return true;
    }

    @Override
    public void onCreate() {
        Logs.SERVICE.v("onCreate() service = %s", this);
        super.onCreate();
        mModelInstance = (IBinder) createModelInstance();
    }

    public void onTaskRemoved(Intent rootIntent) {
        Logs.SERVICE.v("onTaskRemoved() service = %s", this);
    }

    public void onTrimMemory(int level) {
        Logs.SERVICE.v("onTrimMemory() service = %s level = %d", this, level);
    }

    @Override
    public void onLowMemory() {
        Logs.SERVICE.v("onLowMemory() service = %s", this);
        super.onLowMemory();
    }


    public ModelClass getModelInstance() {
        return (ModelClass) mModelInstance;
    }

    protected abstract ModelClass createModelInstance();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logs.SERVICE.v("onStartCommand(). Intent = %s, " +
                "flags = %d, startId = %d. Service = %s", intent, flags, startId, this);
        super.onStartCommand(intent, flags, startId);
        if (mFirstStart.getAndSet(false)){
            onFirstStart();
        }
        return START_NOT_STICKY;
    }

    protected void onFirstStart(){};


    @Override
    public void onDestroy() {
        Logs.SERVICE.v("onDestroy() Instance = %s", this);
        super.onDestroy();
        if (mModelInstance instanceof ServiceDestroyAware){
            ((ServiceDestroyAware) mModelInstance).destroy();
        }
    }



    public static interface ServiceDestroyAware {
        public void destroy();
    }

    public static interface ClientBindingHandlingStrategy {

        public void onClientBind(ModelService owner);

        public void onClientUnbind(ModelService owner);

    }

    public static class NoOpClientBindingHandlingStrategy implements ClientBindingHandlingStrategy {
        @Override
        public void onClientBind(ModelService owner) {}
        @Override
        public void onClientUnbind(ModelService owner) {}
    }

    public static class AutoShutdownClientBindingHandlingStrategy implements ClientBindingHandlingStrategy {

        private final long mMsToKill;
        private ExecutorService mShutdownExecutor = Executors.newSingleThreadExecutor();
        private Future mLastScheduledShutdownTask;

        public AutoShutdownClientBindingHandlingStrategy() {
            this(3000);
        }

        public AutoShutdownClientBindingHandlingStrategy(long msToKill) {
            this.mMsToKill = msToKill;
        }

        @Override
        public synchronized void onClientBind(ModelService owner) {
            if (mLastScheduledShutdownTask != null){
                mLastScheduledShutdownTask.cancel(true);
                mLastScheduledShutdownTask = null;
            }
        }

        @Override
        public synchronized void onClientUnbind(final ModelService owner) {
            mLastScheduledShutdownTask = mShutdownExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(mMsToKill);
                    } catch (InterruptedException e) {
                        //thread could be interrupted by its future during new client
                        return;
                    }
                    doShutdown(owner);
                }
            });
        }

        private synchronized void doShutdown(ModelService owner) {
            if (mLastScheduledShutdownTask != null){
                //last check on now clients
                owner.stopSelf();
            }
        }
    }

}
