package org.monroe.team.aas.ui;

import android.app.Application;

import org.monroe.team.aas.ui.common.Logs;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class AasApplication extends Application{

    @Override
    public void onCreate() {
        Logs.UI.i("Start application");
        super.onCreate();
    }

}
