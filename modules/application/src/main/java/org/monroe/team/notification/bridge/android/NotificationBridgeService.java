package org.monroe.team.notification.bridge.android;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import org.monroe.team.libdroid.commons.Should;
import org.monroe.team.libdroid.mservice.ModelService;

import org.monroe.team.notification.bridge.R;
import org.monroe.team.notification.bridge.android.delivery.bluetooth.BluetoothGateway;
import org.monroe.team.notification.bridge.android.delivery.bluetooth.BluetoothRemoteClient;
import org.monroe.team.notification.bridge.boundaries.DeviceNameBoundary;
import org.monroe.team.notification.bridge.boundaries.NotificationBoundary;
import org.monroe.team.notification.bridge.boundaries.RemoteClientBoundary;
import org.monroe.team.notification.bridge.common.IdAwareData;
import org.monroe.team.notification.bridge.strategies.DateStrategy;
import org.monroe.team.notification.bridge.strategies.IdGeneratorStrategy;
import org.monroe.team.notification.bridge.usecases.common.InjectionSupportedUseCaseContext;
import org.monroe.team.notification.bridge.usecases.UseCaseSetup;
import org.monroe.team.notification.bridge.usecases.common.UseCaseContext;

import java.util.*;

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

    private final static class NotificationBridgeManagerImpl extends Binder
            implements NotificationBridgeManager,
            NotificationBoundary.Required,
            RemoteClientBoundary.Required,
            DeviceNameBoundary.Required{

        private final NotificationBridgeService mService;
        private final BluetoothGateway mBluetoothGateway;
        private String mDeviceName = "OldFuck";
        private UseCaseContext mUseCaseContext = new InjectionSupportedUseCaseContext();
        private SharedPreferences mPreferences;

        private NotificationBridgeManagerImpl(NotificationBridgeService service, BluetoothGateway bluetoothGateway) {
            mService = service;
            mBluetoothGateway = bluetoothGateway;
        }

        public void initiate(Context context){

            mUseCaseContext.installStrategy(new DateStrategy(), DateStrategy.class);
            mUseCaseContext.installStrategy(new IdGeneratorStrategy(), IdGeneratorStrategy.class);

            mUseCaseContext.installBoundary(this, NotificationBoundary.Required.class);
            mUseCaseContext.installBoundary(this, RemoteClientBoundary.Required.class);
            mUseCaseContext.installBoundary(this, DeviceNameBoundary.Required.class);

            UseCaseSetup.define(mUseCaseContext);
            mUseCaseContext.startup();

            mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            if(SettingAccessor.SERVICE_ACTIVE.getValue(mPreferences)){
                activate();
            }

            if(SettingAccessor.ACCEPT_NOTIFICATION.getValue(mPreferences)
                    && SettingAccessor.ACCEPT_OVER_BLUETOOTH.getValue(mPreferences)){
                activateBluetoothForIncoming();
            }
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
        public boolean isBluetoothGatewayEnabled() {
            return mBluetoothGateway.isBluetoothEnabled();
        }

        @Override
        public boolean isBluetoothGatewaySupported() {
            return mBluetoothGateway.isSupported();
        }

        @Override
        public void activateBluetoothForIncoming() {
            mBluetoothGateway.startServer();
        }

        @Override
        public void deactivateBluetoothForIncoming() {
            mBluetoothGateway.stopServer();
        }

        @Override
        public void sendTestNotification() {
            Map<String,String> notificationBody = new HashMap<String, String>();
            notificationBody.put("text","Test notification");
            mUseCaseContext.getBoundary(NotificationBoundary.Declare.class).onInternalNotification(notificationBody);
        }

        @Override
        public void sendNotification(RemoteClientBoundary.RemoteClient client, NotificationBoundary.Notification... notifications) {
            if (client instanceof BluetoothRemoteClient){
                mBluetoothGateway.send((BluetoothRemoteClient) client, notifications, mBluetoothDeliveryCallback);
            } else {
                Should.failsHere("Unsupported client type");
            }

        }

        @Override
        public RemoteClientBoundary.RemoteClient[] getClientsToNotify() {
            List<RemoteClientBoundary.RemoteClient> remoteClientList = new ArrayList<RemoteClientBoundary.RemoteClient>();
            if (isBluetoothAvailable() && isShareOverBluetoothAllowed()){
               remoteClientList.addAll(mBluetoothGateway.getKnownDevices());
            }
            return remoteClientList.toArray(new RemoteClientBoundary.RemoteClient[0]);
        }


        private boolean isShareOverBluetoothAllowed() {
            return SettingAccessor.SHARE_NOTIFICATION.getValue(mPreferences) && SettingAccessor.SHARE_OVER_BLUETOOTH.getValue(mPreferences);
        }

        private boolean isBluetoothAvailable() {
            return isBluetoothGatewaySupported()
                    && isBluetoothGatewayEnabled();
        }

        private BluetoothGateway.BluetoothDeliveryCallback mBluetoothDeliveryCallback = new BluetoothGateway.BluetoothDeliveryCallback() {
            @Override
            public void onFailBeforeSend(IdAwareData[] dataset, BluetoothRemoteClient client) {
                for (IdAwareData idAwareData : dataset) {
                    mUseCaseContext.getBoundary(NotificationBoundary.Declare.class)
                            .onNotificationSendFails((NotificationBoundary.Notification) idAwareData, client);
                }
            }

            @Override
            public void onSuccessDeliver(IdAwareData data, BluetoothRemoteClient client) {
                mUseCaseContext.getBoundary(NotificationBoundary.Declare.class)
                        .onNotificationSendSuccess((NotificationBoundary.Notification) data, client);
            }

            @Override
            public void onFailDuringSend(IdAwareData data, BluetoothRemoteClient client) {
                mUseCaseContext.getBoundary(NotificationBoundary.Declare.class)
                        .onNotificationSendFails((NotificationBoundary.Notification) data, client);
            }
        };


        @Override
        public String getDeviceName() {
            return mDeviceName;
        }
    }

}
