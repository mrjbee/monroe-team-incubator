package org.monroe.team.libdroid.mservice;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import org.monroe.team.libdroid.commons.Should;
import org.monroe.team.libdroid.logging.Logger;
import org.monroe.team.libdroid.logging.LoggerSetup;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final Logger mLog;

    protected ModelService(ClientBindingHandlingStrategy mClientBindingHandlingStrategy) {
        this.mClientBindingHandlingStrategy = mClientBindingHandlingStrategy;
        mLog = LoggerSetup.createLogger("mservice").extend(getClass().getSimpleName());
    }

    public ModelService() {
        this(new NoOpClientBindingHandlingStrategy());
    }

    @Override
    public IBinder onBind(Intent intent) {
        mLog.v("onBind() service = %s", this);
        mClientBindingHandlingStrategy.onClientBind(this);
        return mModelInstance;
    }

    @Override
    public void onRebind(Intent intent) {
        mLog.v("onRebind() service = %s", this);
        super.onRebind(intent);
        mClientBindingHandlingStrategy.onClientBind(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mLog.v("onUnbind() service = %s", this);
        mClientBindingHandlingStrategy.onClientUnbind(this);
        return true;
    }

    @Override
    public void onCreate() {
        mLog.v("onCreate() service = %s", this);
        super.onCreate();
        mModelInstance = (IBinder) createModelInstance();
    }

    public void onTaskRemoved(Intent rootIntent) {
        mLog.v("onTaskRemoved() service = %s", this);
    }

    public void onTrimMemory(int level) {
        mLog.v("onTrimMemory() service = %s level = %d", this, level);
    }

    @Override
    public void onLowMemory() {
        mLog.v("onLowMemory() service = %s", this);
        super.onLowMemory();
    }


    public ModelClass getModelInstance() {
        return (ModelClass) mModelInstance;
    }

    protected abstract ModelClass createModelInstance();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLog.v("onStartCommand(). Intent = %s, " +
                "flags = %d, startId = %d. Service = %s", intent, flags, startId, this);
        super.onStartCommand(intent, flags, startId);
        if (mFirstStart.getAndSet(false)){
            onFirstStart();
        }
        return START_NOT_STICKY;
    }

    protected void onFirstStart(){}


    @Override
    public void onDestroy() {
        mLog.v("onDestroy() Instance = %s", this);
        super.onDestroy();
        if (mModelInstance instanceof ServiceDestroyAware){
            ((ServiceDestroyAware) mModelInstance).destroy();
        }
    }

    public ModelClass createBinderProxy(ModelClass model){
       return (ModelClass) Proxy.newProxyInstance(
               this.getClassLoader(),
               new Class<?>[]{model.getClass(), IBinder.class},
               new DynamicInvocationHandler(model));
    }

    private static class DynamicInvocationHandler implements InvocationHandler{

        private final IBinder mBinder = new Binder();
        private final Object mModel;

        private DynamicInvocationHandler(Object model) {
            mModel = model;
        }

        private String generateMethodId(Method method) {
            return method.getName()+":"+method.getParameterTypes().length;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(isMethodOf(mModel, method)){
               return method.invoke(mModel, args);
            }

            if(isMethodOf(mBinder, method)){
                return method.invoke(mBinder, args);
            }

            throw Should.failsHere("Couldn`t find implementation for method = "+method);
        }

        private boolean isMethodOf(Object objectToExplore, Method method) {
            Method[] methods = objectToExplore.getClass().getMethods();
            for (Method methodToProbe : methods) {
                if (methodToProbe == method){
                    return true;
                }
            }
            return false;
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

            if (ServiceUtils.isServiceForeground(owner.getClass(), owner)){
                return;
            }

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
