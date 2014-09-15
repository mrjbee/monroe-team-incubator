package org.monroe.team.smooker.app;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import org.monroe.team.smooker.app.common.Model;
import org.monroe.team.smooker.app.common.Preferences;
import org.monroe.team.smooker.app.common.SetupPage;
import org.monroe.team.smooker.app.uc.AddSmoke;

import java.util.ArrayList;
import java.util.Arrays;


public class SmookerApplication extends Application {

    public static SmookerApplication instance;
    private Model model;

    @Override public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public synchronized Model getModel() {
        if (model == null){
            model = new Model(getApplicationContext());
            model.onCreate();
        }
        return model;
    }

    public void onRemoteControlNotificationCloseRequest() {
        getModel().stopNotificationControlService();
        preferences().setStickyNotificationEnabled(false);
        if (preferences().isStickyNotificationFirstTimeClose()){
            preferences().setStickyNotificationFirstTimeClose(false);
            Intent intent = new Intent(this, WizardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("PAGE_INDEX", 0);
            intent.putExtra("PAGE_STACK", new ArrayList<SetupPage>(Arrays.asList(SetupPage.UI)));
            intent.putExtra("FORCE", false);
            startActivity(intent);
        }
        closeSystemDialogs();
    }

    private Preferences preferences() {
        return getModel().usingService(Preferences.class);
    }

    public void onRemoteControlNotificationAddSmokeRequest() {
        getModel().execute(AddSmoke.class, null);
        Toast.makeText(this.getApplicationContext(), "One smoke break added", Toast.LENGTH_SHORT).show();
        closeSystemDialogs();
    }


    private void closeSystemDialogs() {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.getApplicationContext().sendBroadcast(it);
    }

    public boolean isStickyNotificationEnabled() {
        return preferences().isStickyNotificationEnabled();
    }

    public void updateStickyNotification(boolean enabled) {
        if (enabled){
            getModel().startNotificationControlService();
        } else {
            getModel().stopNotificationControlService();
        }
        preferences().setStickyNotificationEnabled(enabled);
    }
}
