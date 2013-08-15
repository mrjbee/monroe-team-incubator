package org.monroe.team.aas.model;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.os.Process;

import org.monroe.team.aas.ui.common.ListenerSupport;
import org.monroe.team.aas.ui.common.Logs;
import org.monroe.team.aas.ui.common.ServiceManager;
import org.monroe.team.aas.ui.common.command.ArgumentLessCommand;
import org.monroe.team.aas.ui.common.command.ResultLessCommand;
import org.monroe.team.aas.ui.common.logging.*;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class ModelService extends Service
        implements ServiceManager.ServiceBinderOwner<PublicGatewayService.PublicGatewayModel> {

    private PublicModelImpl mPublicModelInstance;
    private final ServiceManager<PublicGatewayService.PublicGatewayModel> mGatewayManager =
            new ServiceManager<PublicGatewayService.PublicGatewayModel>(this, PublicGatewayService.class);


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
        int answer = super.onStartCommand(intent, flags, startId);
        if (mPublicModelInstance == null){
            mPublicModelInstance = new PublicModelImpl();
            mGatewayManager.obtain();
        }
        return answer;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.MODEL.v("Model service onDestroy() Instance = %s", this);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onObtain(PublicGatewayService.PublicGatewayModel publicGatewayModel) {
        mPublicModelInstance.setPublicGateway(publicGatewayModel);
    }

    @Override
    public void onRelease() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    private class PublicModelImpl extends Binder implements PublicModel{

        private boolean mPublicGatewayVisibility = false;
        private final ListenerSupport<PublicGatewayVisibilityListener> mGatewayVisibilityListenerSupport
                = new ListenerSupport<PublicGatewayVisibilityListener>();

        @Override
        public boolean isPublicGatewayVisible() {
            return mPublicGatewayVisibility;
        }

        private void setPublicGatewayVisibility(boolean visibility){
            final boolean oldValue = mPublicGatewayVisibility;
            mPublicGatewayVisibility = visibility;
            mGatewayVisibilityListenerSupport.fireIf(new ArgumentLessCommand<Boolean>() {
                @Override
                protected Boolean call() {
                    return oldValue != mPublicGatewayVisibility;
                }
            }, new ResultLessCommand<PublicGatewayVisibilityListener>() {
                 @Override
                 protected void call(PublicGatewayVisibilityListener argument) {
                    argument.onVisibilityChange(mPublicGatewayVisibility);
                 }
             });
        }

        @Override
        public void addPublicGatewayVisibilityListener(PublicGatewayVisibilityListener gatewayVisibilityListener) {
            mGatewayVisibilityListenerSupport.add(gatewayVisibilityListener);
        }

        @Override
        public void removePublicGatewayVisibilityListener(PublicGatewayVisibilityListener gatewayVisibilityListener) {
            mGatewayVisibilityListenerSupport.remove(gatewayVisibilityListener);
        }

        @Override
        public void removeAllListeners() {
            mGatewayVisibilityListenerSupport.removeAll();
        }

        private void setPublicGateway(PublicGatewayService.PublicGatewayModel publicGatewayModel) {

        }
    }

    public static interface PublicModel {

        public boolean isPublicGatewayVisible();
        void addPublicGatewayVisibilityListener(PublicGatewayVisibilityListener gatewayVisibilityListener);
        void removePublicGatewayVisibilityListener(PublicGatewayVisibilityListener gatewayVisibilityListener);
        void removeAllListeners();

        public static interface PublicGatewayVisibilityListener{
            public void onVisibilityChange(boolean newVisibility);
        }
    }

}
