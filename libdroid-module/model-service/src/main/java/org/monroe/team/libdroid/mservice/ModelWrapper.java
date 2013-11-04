package org.monroe.team.libdroid.mservice;

import android.app.Service;
import android.content.Context;
import org.monroe.team.libdroid.commons.Closure;
import org.monroe.team.libdroid.commons.ListenerSupport;

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
    private final ListenerSupport<ModelListener<ModelWrapper<ModelApi>>> mListenerSupport =
            new ListenerSupport<ModelListener<ModelWrapper<ModelApi>>>();

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
        mListenerSupport.notify(new Closure<Void, ModelListener<ModelWrapper<ModelApi>>>() {
            @Override
            public Void call(ModelListener<ModelWrapper<ModelApi>> in) {
                in.onModelObtain(ModelWrapper.this);
                return null;
            }
        });
    }

    @Override
    final public void onRelease(ModelApi modelApi) {
        modelApi = null;
        mListenerSupport.notify(new Closure<Void, ModelListener<ModelWrapper<ModelApi>>>() {
            @Override
            public Void call(ModelListener<ModelWrapper<ModelApi>> in) {
                in.onModelRelease(ModelWrapper.this);
                return null;
            }
        });
    }

    final public void addListener(ModelListener<ModelWrapper<ModelApi>> listener){
        mListenerSupport.add(listener);
    }


    final public boolean deleteListener(ModelListener<ModelWrapper<ModelApi>> listener){
        return mListenerSupport.remove(listener);
    }

    final public void deleteAll(){
        mListenerSupport.removeAll();
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

    public static interface ModelListener<ModelWrapperType extends ModelWrapper> {
        public void onModelObtain(ModelWrapperType modelWrapper);
        public void onModelRelease(ModelWrapperType modelWrapper);
    }

    public static abstract class ModelListenerAdapter<ModelWrapperType extends ModelWrapper> implements ModelListener<ModelWrapperType> {
        public void onModelObtain(ModelWrapperType modelWrapper){};
        public void onModelRelease(ModelWrapperType modelWrapper){};
    }
}
