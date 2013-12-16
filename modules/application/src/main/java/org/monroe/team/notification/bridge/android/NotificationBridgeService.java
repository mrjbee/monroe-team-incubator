package org.monroe.team.notification.bridge.android;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import org.monroe.team.libdroid.mservice.ModelService;
import org.monroe.team.notification.bridge.R;
import org.monroe.team.notification.bridge.android.connectivity.BluetoothGateway;
import org.monroe.team.notification.bridge.entities.Notification;
import org.monroe.team.notification.bridge.services.DateProvider;
import org.monroe.team.notification.bridge.usecases.UserCaseRouter;
import org.monroe.team.notification.bridge.services.IdGenerator;

public class NotificationBridgeService extends ModelService<NotificationBridgeManager> {

    public NotificationBridgeService() {
        super(new AutoShutdownClientBindingHandlingStrategy());
    }

    @Override
    protected NotificationBridgeManager createModelInstance() {
        NotificationBridgeManagerImpl bridgeManager = new NotificationBridgeManagerImpl(this, getOwner().getBluetoothGateway());
        bridgeManager.initiate(this);
        return bridgeManager;
    }

    private NotificationBridgeApplication getOwner(){
        return (NotificationBridgeApplication) getApplication();
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
        private final BluetoothGateway mBluetoothGateway;
        private final UserCaseRouter mController = new UserCaseRouter();
        private final IdGenerator mIdGenerator = new IdGenerator();
        private final DateProvider mDateProvider = new DateProvider();
        private String mDeviceName = "OldFuck";

        private NotificationBridgeManagerImpl(NotificationBridgeService service, BluetoothGateway bluetoothGateway) {
            mService = service;
            mBluetoothGateway = bluetoothGateway;
        }

        public void initiate(Context context){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if(SettingAccessor.SERVICE_ACTIVE.getValue(preferences)){
                activate();
            };
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

        @Override
        public void activateBluetoothForOutgoings() {
            mBluetoothGateway.activateOutgoing();
        }

        @Override
        public void deactivateBluetoothForOutGoings() {
            mBluetoothGateway.deactivateOutgoing();
        }

        @Override
        public boolean isBluetoothGatewayEnabled() {
            return mBluetoothGateway.isBluetoothEnabled();
        }

        @Override
        public boolean isBluetoothGatewaySupported() {
            return mBluetoothGateway.isSupported();
        }

        @Override
        public void activateBluetoothForIncomings() {
            mBluetoothGateway.activateIncoming();
        }

        @Override
        public void deactivateBluetoothForIncomings() {
            mBluetoothGateway.deactivateIncoming();
        }

        @Override
        public void sendTestNotification() {
            Notification notification = new Notification(
                    mIdGenerator.generateId(mDeviceName),
                    mDeviceName,
                    mDateProvider.getNow());

            notification.body.put("text", "This is test notification");
            mController.sendNotification(notification);
        }
    }

}
