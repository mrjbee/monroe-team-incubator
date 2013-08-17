package org.monroe.team.aas.model;

import android.content.Context;
import android.os.*;

import org.monroe.team.aas.common.model.ModelService;
import org.monroe.team.aas.ui.common.ListenerSupport;
import org.monroe.team.aas.common.model.ModelServiceManager;
import org.monroe.team.aas.ui.common.command.ArgumentLessCommand;
import org.monroe.team.aas.ui.common.command.ResultLessCommand;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class PublicModelService extends ModelService<PublicModelService.PublicModel>
        implements ModelServiceManager.ServiceClient<PublicGatewayService.PublicGatewayModel> {

    private final ModelServiceManager<PublicGatewayService.PublicGatewayModel> mGatewayManagerModel =
            new ModelServiceManager<PublicGatewayService.PublicGatewayModel>(this, PublicGatewayService.class);

    public PublicModelService() {
        super(new AutoShutdownClientBindingHandlingStrategy());
    }

    @Override
    protected PublicModel createModelInstance() {
        return new PublicModelImpl(mGatewayManagerModel);
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
    public void onRelease() {
        ((PublicModelImpl)getModelInstance()).setPublicGatewayVisibility(false);
    }


    private class PublicModelImpl extends Binder implements PublicModel{

        private boolean mPublicGatewayVisibility = false;
        private final ListenerSupport<PublicGatewayVisibilityListener> mGatewayVisibilityListenerSupport
                = new ListenerSupport<PublicGatewayVisibilityListener>();
        private final ModelServiceManager<PublicGatewayService.PublicGatewayModel> mGatewayManagerModel;

        private PublicModelImpl(ModelServiceManager<PublicGatewayService.PublicGatewayModel> mGatewayManagerModel) {
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

        public void destroy() {
            if(mGatewayManagerModel.isObtained()){
                mGatewayManagerModel.get().shutdown();
            }
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
