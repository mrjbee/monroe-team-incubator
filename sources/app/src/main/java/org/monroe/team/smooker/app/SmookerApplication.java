package org.monroe.team.smooker.app;

import android.app.Application;

import org.monroe.team.smooker.app.common.Model;


public class SmookerApplication extends Application {

    public static SmookerApplication instance;
    private Model model;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public synchronized Model getModel() {
        if (model == null){
            model = new Model(getApplicationContext());
        }
        return model;
    }
}
