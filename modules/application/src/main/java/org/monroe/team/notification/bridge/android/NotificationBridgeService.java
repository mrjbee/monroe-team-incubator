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
        public void activateBluetooth() {

        }

        @Override
        public void deactivateBluetooth() {

        }

        @Override
        public boolean isBluetoothGatewayEnabled() {
            return mBluetoothGateway.isBluetoothEnabled();  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isBluetoothGatewaySupported() {
            return mBluetoothGateway.isSupported();
        }

        @Override
        public void activateBluetoothForIncomings() {
            mBluetoothGateway.activateServer();
        }

        @Override
        public void deactivateBluetoothForIncomings() {
            mBluetoothGateway.deactivateServer();
        }
    }

}
