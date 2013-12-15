package org.monroe.team.notification.bridge.android.connectivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import java.util.UUID;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 7:14 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class BluetoothGateway {

    static final String SERVICE_NAME = "NotificationBridgeService";
    static final String SERVICE_UUID_PLAIN = "1e3e867b-aa65-4dc0-a400-6bc4762ef15e";
    static final UUID SERVICE_UUID = UUID.fromString(SERVICE_UUID_PLAIN);

    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothServer mBluetoothServer;
    private boolean outActivated = false;

    public BluetoothGateway(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothServer = new BluetoothServer(mBluetoothAdapter);
    }

    public boolean isSupported() {
        return mBluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public void activateIncoming() {
        mBluetoothServer.openServerConnection();
    }

    public void deactivateIncoming() {
        mBluetoothServer.closeServerConnection();
    }

    public void activateOutgoing() {
       outActivated = true;
    }

    public void deactivateOutgoing() {
       outActivated = false;
    }
}
