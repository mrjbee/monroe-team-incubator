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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class PublicGatewayService extends ModelService<PublicGatewayService.PublicGatewayModel> {

    @Override
    protected void onFirstStart() {
        startForeground(1, new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("AAS service")
                .setContentInfo("Run....")
                .setContentText("Text...").build());
    }

    private void shutdownService() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    protected PublicGatewayModel createModelInstance() {
        return new PublicGatewayModelImpl();
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
