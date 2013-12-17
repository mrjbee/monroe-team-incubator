package org.monroe.team.notification.bridge.android;

import android.app.Application;
import org.monroe.team.notification.bridge.android.delivery.bluetooth.BluetoothGateway;

/**
 * User: MisterJBee
 * Date: 9/21/13 Time: 8:08 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class NotificationBridgeApplication extends Application {

    private BluetoothGateway mBluetoothGateway;

    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothGateway = new BluetoothGateway(this);
    }


    public BluetoothGateway getBluetoothGateway() {
        return mBluetoothGateway;
    }
}
