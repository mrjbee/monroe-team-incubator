package org.monroe.team.aas.common.model;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.List;

/**
 * User: MisterJBee
 * Date: 8/15/13 Time: 8:50 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ModelServiceManager<ClientAwareInterface> {

    private final ModelServiceClient<ClientAwareInterface> mOwner;
    private final Class<? extends Service> mServiceClass;
    private final ServiceConnection mServiceConnection = new ServiceConnection();
    private ClientAwareInterface mClientAwareInterface;
    private State mState = State.RELEASED;

    public ModelServiceManager(ModelServiceClient<ClientAwareInterface> mOwner, Class<? extends Service> mServiceClass) {
        this.mOwner = mOwner;
        this.mServiceClass = mServiceClass;
    }

    private synchronized void uninstallServiceBinder() {
        mState = State.RELEASED;
        mOwner.onRelease();
    }

    private synchronized void installServiceBinder(ClientAwareInterface clientAwareInterface) {

        if(mState == State.RELEASED || mState == State.RELEASING){
           return;
        }

        mClientAwareInterface = clientAwareInterface;
        mState = State.OBTAINED;
        mOwner.onObtain(mClientAwareInterface);
    }

    public synchronized void obtain(){
        if (mState != State.RELEASED) return;
        mState = State.OBTAINING;
        mOwner.getContext().startService(new Intent(mOwner.getContext(),mServiceClass));
        mOwner.getContext().bindService(
                new Intent(mOwner.getContext(), mServiceClass),
                mServiceConnection, Context.BIND_ADJUST_WITH_ACTIVITY);
    };

    public synchronized void release(){
        if (mState != State.OBTAINED && mState != State.OBTAINING){
            return;
        }
        mState = State.RELEASING;
        mOwner.getContext().unbindService(mServiceConnection);
    }

    public synchronized ClientAwareInterface get(){
      if (mState != State.OBTAINED) throw new IllegalStateException("Invalid state = "+mState);
      return mClientAwareInterface;
    }

    public synchronized State getState() {
        return mState;
    }

    public synchronized boolean isObtained(){
       return mState == State.OBTAINED;
    }

    public synchronized boolean isServiceRunning(){
        final ActivityManager activityManager = (ActivityManager) mOwner.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(mServiceClass.getName())){
                return runningServiceInfo.started;
            }
        }
        return false;
    }

    private class ServiceConnection implements android.content.ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            installServiceBinder((ClientAwareInterface) iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            uninstallServiceBinder();
        }
    }

    public static interface ModelServiceClient<ClientInterface>{
        Context getContext();
        public void onObtain(ClientInterface clientInterface);
        public void onRelease();
    }

    public static enum State{
        OBTAINED, OBTAINING, RELEASED, RELEASING;
    }
}
