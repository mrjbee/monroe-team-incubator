package org.monroe.team.notification.bridge.android;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.support.v4.app.NotificationCompat;
import org.monroe.team.libdroid.mservice.ModelService;
import org.monroe.team.notification.bridge.R;

public class NotificationBridgeService extends ModelService<NotificationBridgeManager> {

    public NotificationBridgeService() {
        super(new AutoShutdownClientBindingHandlingStrategy());
    }

    @Override
    protected NotificationBridgeManager createModelInstance() {
        return new NotificationBridgeManagerImpl(this);
    }

    private void enableForegroundMode() {

        //The intent to launch when the user clicks the expanded notification
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        startForeground(12345, new NotificationCompat.Builder(this)
                .setContentTitle("Title")
                .setContentInfo("Info")
                .setContentText("Text")
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendIntent)
                .getNotification());
    }

    private void disableForegroundMode() {
        stopForeground(true);
    }

    private final static class NotificationBridgeManagerImpl extends Binder implements NotificationBridgeManager {

        private final NotificationBridgeService mService;

        private NotificationBridgeManagerImpl(NotificationBridgeService service) {
            mService = service;
        }

        @Override
        public void activate() {
            mService.enableForegroundMode();
        }

        @Override
        public void disable() {
            mService.disableForegroundMode();
        }

        @Override
        public void onSettingChange(SettingAccessor<?> accessor) {

        }
    }

}
