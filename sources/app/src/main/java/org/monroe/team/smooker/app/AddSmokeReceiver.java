package org.monroe.team.smooker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.monroe.team.smooker.app.uc.AddSmoke;

public class AddSmokeReceiver extends BroadcastReceiver {

    public AddSmokeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SmookerApplication.instance.getModel().execute(AddSmoke.class,null);
        Toast.makeText(context, "One smoke break added", Toast.LENGTH_SHORT).show();
        closeNotifications(context);
        //SmookerRemoteControlNotificationService.setText("You did some smoke breaks");
    }

    private void closeNotifications(Context context) {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }
}
