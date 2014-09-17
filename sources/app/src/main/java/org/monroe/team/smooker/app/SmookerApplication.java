package org.monroe.team.smooker.app;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;
import android.widget.Toast;

import org.monroe.team.smooker.app.common.Closure;
import org.monroe.team.smooker.app.common.Events;
import org.monroe.team.smooker.app.common.Model;
import org.monroe.team.smooker.app.common.Preferences;
import org.monroe.team.smooker.app.common.Settings;
import org.monroe.team.smooker.app.common.SetupPage;
import org.monroe.team.smooker.app.event.Event;
import org.monroe.team.smooker.app.uc.AddSmoke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SmookerApplication extends Application {

    public static SmookerApplication instance;
    private Model model;

    private final static int QUIT_SMOKE_PROPOSAL_NOTIFICATION_ID = 333;

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
        if (settings().getAndSet(Settings.FIRST_TIME_CLOSE_STICKY_NOTIFICATION, false)){
            Intent intent = new Intent(this, WizardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("PAGE_INDEX", 0);
            intent.putExtra("PAGE_STACK", new ArrayList<SetupPage>(Arrays.asList(SetupPage.UI)));
            intent.putExtra("FORCE", false);
            startActivity(intent);
        }
        closeSystemDialogs();
    }

    final public Settings settings() {
        return getModel().usingService(Settings.class);
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

    public void updateStickyNotification(boolean enabled) {
        if (enabled){
            getModel().startNotificationControlService();
        } else {
            getModel().stopNotificationControlService();
        }
        settings().set(Settings.ENABLED_STICKY_NOTIFICATION, enabled);
    }

    public Pair<Boolean, List<SetupPage>> getRequiredSetupPages() {
        final List<SetupPage> answer = new ArrayList<SetupPage>(4);
        boolean required = false;

        if(settings().getAndSet(Settings.FIRST_TIME_ENTER_APP, false)){
            answer.add(SetupPage.WELCOME_PAGE);
            required = true;
        }

        if (!settings().has(Settings.SMOKE_PRICE)){
            answer.add(SetupPage.GENERAL);
            required = true;
        }

        return new Pair<Boolean, List<SetupPage>>(required,answer);
    }
    public boolean firstSetupDoneTrigger() {
        return settings().getAndSet(Settings.FIRST_TIME_AFTER_SETUP,false);
    }

    public void onSetupPageShown(SetupPage setupPage) {
        if (setupPage == SetupPage.UI){
            settings().set(Settings.FIRST_TIME_CLOSE_STICKY_NOTIFICATION, false);
        } else if(setupPage == SetupPage.QUIT_PROGRAM){
            settings().set(Settings.FIRST_TIME_QUIT_SMOKE_PAGE, false);
            NotificationManager manager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
            manager.cancel(QUIT_SMOKE_PROPOSAL_NOTIFICATION_ID);
        }
    }

    public void onDashboardCreate() {
        if (settings().get(Settings.ENABLED_STICKY_NOTIFICATION)){
            updateStickyNotification(true);
        }
        if (!settings().get(Settings.FIRST_TIME_AFTER_SETUP) && settings().get(Settings.FIRST_TIME_QUIT_SMOKE_PAGE)){
            NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            Intent dashBoardIntent = new Intent(this, DashboardActivity.class);
            dashBoardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dashBoardIntent.putExtra("PAGE_INDEX", 0);
            dashBoardIntent.putExtra("PAGE_STACK", new ArrayList<SetupPage>(Arrays.asList(SetupPage.QUIT_PROGRAM)));
            dashBoardIntent.putExtra("FORCE", false);

            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    DashboardActivity.WIZARD_ACTIVITY_REQUEST,
                    dashBoardIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);


            Intent dropNotificationIntent = new Intent(getApplicationContext(), SmokeQuitNotificationClosedReceiver.class);
            PendingIntent dropNotificationPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 2, dropNotificationIntent, 0);


            builder.setAutoCancel(true)
                    .setContentTitle("Quit Smoking")
                    .setContentText("Choose a way to quit smoking")
                    .setSubText("... which fits to you")
                    .setSmallIcon(R.drawable.smooker_logo)
                    .setDeleteIntent(dropNotificationPendingIntent)
                    .setContentIntent(pendingIntent);

            manager.notify(QUIT_SMOKE_PROPOSAL_NOTIFICATION_ID, builder.build());
        }
    }
}
