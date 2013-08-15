package org.monroe.team.aas.ui.common;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.ArrayList;

/**
 * User: MisterJBee
 * Date: 8/15/13 Time: 8:50 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ServiceManager <ServiceBinder> {

    private final ServiceBinderOwner<ServiceBinder> mOwner;
    private final Class<? extends Service> mServiceClass;
    private final ServiceConnection mServiceConnection = new ServiceConnection();
    private ServiceBinder mServiceBinder;
    private State mState = State.PREOBTAIN;

    public ServiceManager(ServiceBinderOwner<ServiceBinder> mOwner, Class<? extends Service> mServiceClass) {
        this.mOwner = mOwner;
        this.mServiceClass = mServiceClass;
    }

    private synchronized void uninstallServiceBinder() {
        mState = State.RELEASED;
        mOwner.onRelease();
    }

    private synchronized void installServiceBinder(ServiceBinder serviceBinder) {

        if(mState == State.RELEASED || mState == State.RELEASING){
           return;
        }

        mServiceBinder = serviceBinder;
        mState = State.OBTAINED;
        mOwner.onObtain(mServiceBinder);
    }

    public synchronized void obtain(){
        if (mState != State.PREOBTAIN) return;
        mOwner.getContext().startService(new Intent(mOwner.getContext(),mServiceClass));
        mOwner.getContext().bindService(
                new Intent(mOwner.getContext(), mServiceClass),
                mServiceConnection, Context.BIND_ADJUST_WITH_ACTIVITY);
    };

    public synchronized void release(){
        if (mState != State.OBTAINED || mState != State.OBTAINING){
            return;
        }
        mState = State.RELEASING;
        mOwner.getContext().unbindService(mServiceConnection);
    }

    public synchronized ServiceBinder get(){
      if (mState != State.OBTAINED) throw new IllegalStateException("Invalid state = "+mState);
      return mServiceBinder;
    }

    public synchronized State getState() {
        return mState;
    }

    public synchronized boolean isObtained(){
       return mState == State.OBTAINED;
    }

    private class ServiceConnection implements android.content.ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            installServiceBinder((ServiceBinder) iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            uninstallServiceBinder();
        }
    }


    public static interface ServiceBinderOwner <ServiceBinder>{
        Context getContext();
        public void onObtain(ServiceBinder serviceBinder);
        public void onRelease();
    }

    public static enum State{
        OBTAINED, OBTAINING, RELEASED, RELEASING, PREOBTAIN;
    }
}
