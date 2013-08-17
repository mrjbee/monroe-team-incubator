package org.monroe.team.aas.model;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.support.v4.app.NotificationCompat;
import org.monroe.team.aas.R;
import org.monroe.team.aas.common.model.ModelService;
import org.monroe.team.aas.ui.DashboardActivity;

/**
 * User: MisterJBee
 * Date: 8/7/13
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class PublicGatewayService extends ModelService<PublicGatewayService.PublicGatewayModel> {

    @Override
    protected void onFirstStart() {
        Intent openApplicationIntent = new Intent(this, DashboardActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openApplicationIntent, 0);

        startForeground(1, new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("AAS service")
                .setContentInfo("Run....")
                .setContentText("Text...")
                .setContentIntent(pendingIntent).build());
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
