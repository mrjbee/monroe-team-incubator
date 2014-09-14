package org.monroe.team.smooker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AddSmokeReceiver extends BroadcastReceiver {

    public AddSmokeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "One smoke break added", Toast.LENGTH_SHORT).show();
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        SmookerRemoteControlNotificationService.setText("You did some smoke breaks");
        context.sendBroadcast(it);
    }
}
