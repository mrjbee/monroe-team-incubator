package org.monroe.team.notification.bridge.android.delivery;


import android.bluetooth.BluetoothDevice;
import org.monroe.team.notification.bridge.boundaries.RemoteClientBoundary;

public class BluetoothRemoteClient implements RemoteClientBoundary.RemoteClient {

    private final BluetoothDevice mDevice;

    public BluetoothRemoteClient(BluetoothDevice device) {
        mDevice = device;
    }

    @Override
    public String getClientId() {
        return mDevice.getAddress();
    }
}
