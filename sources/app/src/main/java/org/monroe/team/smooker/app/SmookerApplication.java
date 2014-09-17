package org.monroe.team.smooker.app;

import android.app.Application;
import android.content.Intent;
import android.util.Pair;
import android.widget.Toast;

import org.monroe.team.smooker.app.common.Model;
import org.monroe.team.smooker.app.common.Preferences;
import org.monroe.team.smooker.app.common.SetupPage;
import org.monroe.team.smooker.app.db.DAO;
import org.monroe.team.smooker.app.db.TransactionManager;
import org.monroe.team.smooker.app.uc.AddSmoke;
import org.monroe.team.smooker.app.uc.GetGeneralDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
        updateStickyNotification(false);
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
        setStickyNotificationEnabled(enabled);
    }

    public void setStickyNotificationEnabled(boolean enabled) {
        preferences().setStickyNotificationEnabled(enabled);
    }

    public void onUISettingSetupPageShown() {
        preferences().setStickyNotificationFirstTimeClose(false);
    }

    public Pair<Boolean, List<SetupPage>> getRequiredSetupPages() {
        final List<SetupPage> answer = new ArrayList<SetupPage>(4);
        boolean required = false;

        if(preferences().isFirstStart()){
            answer.add(SetupPage.WELCOME_PAGE);
            preferences().markAsFirstStartDone();
            required = true;
        }

        if (isSmokePerDayUndefined()){
            answer.add(SetupPage.GENERAL);
            required = true;
        }

        if (preferences().getSmokePerDay() > 0 && !preferences().isQuitProgramSuggested()){
            preferences().markAsQuitProgramSuggested();
            answer.add(SetupPage.QUIT_PROGRAM);
        }
        return new Pair<Boolean, List<SetupPage>>(required,answer);
    }


    private boolean isSmokePerDayUndefined() {
        return preferences().getSmokePerDay() == GetGeneralDetails.GeneralDetailsResponse.SMOKE_PER_DAY_UNDEFINED;
    }

    public boolean firstSetupDoneTrigger() {
        return preferences().markFirstSetup();
    }
}
