package org.monroe.team.libdroid.mservice;

import android.app.Service;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 11/4/13 Time: 11:39 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public abstract class ModelWrapper <ModelApi> implements ModelProvider.ModelProviderOwner<ModelApi> {

    private final Context mCurrentContext;
    private final ModelProvider<ModelApi> mModelProvider;
    private final ModelAwaitingQueue mAwaitingQueue;
    private ModelApi mModelApi;

    public ModelWrapper(Context currentContext, Class<? extends Service> modelServiceClass) {
        mCurrentContext = currentContext;
        mModelProvider = new ModelProvider<ModelApi>(this, modelServiceClass);
        mAwaitingQueue = new ModelAwaitingQueue();
    }

    final public void initialize(){
       mModelProvider.obtain();
       initializeImpl();
    }

    final public void destroy(){
        destroyImpl();
        mModelProvider.releaseAndDestroy();
    }

    protected void initializeImpl() {}
    protected void destroyImpl() {}

    @Override
    final public Context getContext() {
        return mCurrentContext;
    }

    @Override
    final public void onObtain(ModelApi modelApi) {
        mModelApi = modelApi;
        mAwaitingQueue.onModelObtained();
    }

    @Override
    final public void onRelease(ModelApi modelApi) {
        modelApi = null;
    }

    protected ModelApi getModel() {
        return mModelApi;
    }

    private static class ModelAwaitingQueue{

        private final List<Runnable> mAwaitingTasksList = new ArrayList<Runnable>(5);
        private boolean mIsModelObtained = false;

        private synchronized void publish(Runnable runnable){
            if (mIsModelObtained){
                mAwaitingTasksList.add(runnable);
            } else {
                runnable.run();
            }
        }

        private synchronized void onModelObtained(){
            mIsModelObtained = true;
            for (Runnable runnable : mAwaitingTasksList) {
                runnable.run();
            }
            mAwaitingTasksList.clear();
        }
    }
}
