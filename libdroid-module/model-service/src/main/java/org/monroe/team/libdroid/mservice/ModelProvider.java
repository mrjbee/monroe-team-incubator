package org.monroe.team.libdroid.mservice;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import org.monroe.team.libdroid.logging.Logger;
import org.monroe.team.libdroid.logging.LoggerSetup;

import java.util.List;

/**
 * User: MisterJBee
 * Date: 8/15/13 Time: 8:50 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ModelProvider<ModelApi> {

    private ModelProviderOwner<ModelApi> mOwner;
    private final Class<? extends Service> mServiceClass;
    private final ServiceConnection mServiceConnection = new ServiceConnection();
    private ModelApi mModelApi;
    private State mState = State.RELEASED;
    private final Logger mLog = LoggerSetup.createLogger("ms-manager").extend(getClass().getSimpleName());


    public ModelProvider(ModelProviderOwner<ModelApi> mOwner, Class<? extends Service> mServiceClass) {
        this.mOwner = mOwner;
        this.mServiceClass = mServiceClass;
    }

    private synchronized void uninstallServiceBinder() {
        ModelApi model = mModelApi;
        mState = State.RELEASED;
        mModelApi = null;
        if(mOwner != null){
            mOwner.onRelease(model);
        }
    }

    private synchronized void installServiceBinder(ModelApi modelApi) {

        if(mState == State.RELEASED || mState == State.RELEASING){
           return;
        }

        mModelApi = modelApi;
        mState = State.OBTAINED;
        if (mOwner != null){
            mOwner.onObtain(mModelApi);
        }
    }

    public synchronized void obtain(){
        if (mOwner == null) throw new IllegalStateException("Manager is dead.");
        if (mState != State.RELEASED) return;
        mState = State.OBTAINING;
        if(!isServiceRunning()){
            mOwner.getContext().startService(new Intent(mOwner.getContext(),mServiceClass));
        }
        mOwner.getContext().bindService(
                new Intent(mOwner.getContext(), mServiceClass),
                mServiceConnection, 0);
    };

    public synchronized void release(){
        if (mOwner == null) throw new IllegalStateException("Manager is dead.");
        if (mState != State.OBTAINED && mState != State.OBTAINING){
            return;
        }
        mState = State.RELEASING;
        mOwner.getContext().unbindService(mServiceConnection);
    }

    public synchronized ModelApi get(){
      if (mState != State.OBTAINED) throw new IllegalStateException("Invalid state = "+mState);
      return mModelApi;
    }

    public synchronized State getState() {
        return mState;
    }

    public synchronized boolean isObtained(){
       return mState == State.OBTAINED;
    }

    public synchronized boolean isServiceRunning(){
       return ServiceUtils.isServiceRunning(mServiceClass, mOwner.getContext());
    }

    public void releaseAndDestroy() {
        release();
        mOwner = null;
    }

    private class ServiceConnection implements android.content.ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            installServiceBinder((ModelApi) iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            uninstallServiceBinder();
        }
    }

    public static interface ModelProviderOwner<ClientInterface>{
        Context getContext();
        public void onObtain(ClientInterface clientInterface);
        public void onRelease(ClientInterface clientInterface);
    }

    public static enum State{
        OBTAINED, OBTAINING, RELEASED, RELEASING;
    }
}
