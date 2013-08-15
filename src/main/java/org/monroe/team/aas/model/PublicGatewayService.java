package org.monroe.team.aas.model;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import org.monroe.team.aas.R;
import org.monroe.team.aas.ui.common.ListenerSupport;
import org.monroe.team.aas.ui.common.Logs;
import org.monroe.team.aas.ui.common.command.ArgumentLessCommand;
import org.monroe.team.aas.ui.common.command.ResultLessCommand;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class PublicGatewayService extends Service {

    private PublicGatewayModelImpl mGatewayModel;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        if (mGatewayModel == null){
            mGatewayModel = new PublicGatewayModelImpl();
            startForeground(startId, new Notification.Builder(this)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("AAS service")
                    .setContentInfo("Run....")
                    .setContentText("Text...").build());
        }
        return result;
    }

    private void shutdownService() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mGatewayModel;
    }

    public class PublicGatewayModelImpl extends Binder implements PublicGatewayModel{
        @Override
        public void shutdown() {
           PublicGatewayService.this.shutdownService();
        }
    }


    public static interface PublicGatewayModel{
        public void shutdown();
        public static interface PublicGatewayListener {}
    }

}
