package org.monroe.team.aas.model;

import android.content.Context;
import android.os.*;

import org.monroe.team.aas.ui.common.ListenerSupport;
import org.monroe.team.aas.ui.common.Logs;
import org.monroe.team.aas.ui.common.command.ArgumentLessCommand;
import org.monroe.team.aas.ui.common.command.ResultLessCommand;
import org.monroe.team.libdroid.mservice.ModelClient;
import org.monroe.team.libdroid.mservice.ModelService;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class PublicModelModelService extends ModelService<PublicModelModelService.PublicModel>
        implements ModelClient.ModelServiceClient<PublicGatewayService.PublicGatewayModel>{

   private final ModelClient<PublicGatewayService.PublicGatewayModel> mGatewayManagerModel =
            new ModelClient<PublicGatewayService.PublicGatewayModel>(this, PublicGatewayService.class);

    public PublicModelModelService() {
        super(new AutoShutdownClientBindingHandlingStrategy());
    }

    @Override
    protected PublicModel createModelInstance() {
        PublicModelImpl model = new PublicModelImpl(mGatewayManagerModel);
        model.init();
        return model;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onObtain(PublicGatewayService.PublicGatewayModel publicGatewayModel) {
        ((PublicModelImpl)getModelInstance()).setPublicGatewayVisibility(true);
    }

    @Override
    public void onRelease(PublicGatewayService.PublicGatewayModel publicGatewayModel) {
        ((PublicModelImpl)getModelInstance()).setPublicGatewayVisibility(false);
    }

    private class PublicModelImpl extends Binder implements PublicModel, ServiceDestroyAware{

        private boolean mPublicGatewayVisibility = false;
        private final ListenerSupport<PublicGatewayVisibilityListener> mGatewayVisibilityListenerSupport
                = new ListenerSupport<PublicGatewayVisibilityListener>();
        private final ModelClient<PublicGatewayService.PublicGatewayModel> mGatewayManagerModel;

        private PublicModelImpl(ModelClient<PublicGatewayService.PublicGatewayModel> mGatewayManagerModel) {
            this.mGatewayManagerModel = mGatewayManagerModel;
        }

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

        @Override
        public void openPublicGateway() {
            mGatewayManagerModel.obtain();
        }

        @Override
        public void closePublicGateway() {
           mGatewayManagerModel.get().shutdown();
        }

        public void init() {
            if(mGatewayManagerModel.isServiceRunning()){
                mGatewayManagerModel.obtain();
            }
        }

        @Override
        public void destroy() {
            mGatewayManagerModel.releaseAndDestroy();
            try {
                Logs.MODEL.i("Start test sleeping");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Logs.MODEL.w(e, "Sleep test interupted");
            }
            Logs.MODEL.i("Stop test sleeping");
        }
    }

    public static interface PublicModel {

        boolean isPublicGatewayVisible();
        void addPublicGatewayVisibilityListener(PublicGatewayVisibilityListener gatewayVisibilityListener);
        void removePublicGatewayVisibilityListener(PublicGatewayVisibilityListener gatewayVisibilityListener);
        void removeAllListeners();
        void openPublicGateway();
        void closePublicGateway();

        public static interface PublicGatewayVisibilityListener{
            public void onVisibilityChange(boolean newVisibility);
        }
    }

}
