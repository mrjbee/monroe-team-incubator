package org.monroe.team.notification.bridge.android.delivery.bluetooth;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import org.monroe.team.notification.bridge.boundaries.RemoteClientBoundary;

import java.io.IOException;

public class BluetoothRemoteClient implements RemoteClientBoundary.RemoteClient {

    private final BluetoothDevice mDevice;

    public BluetoothRemoteClient(BluetoothDevice device) {
        mDevice = device;
    }

    @Override
    public String getClientId() {
        return mDevice.getAddress();
    }

    public BluetoothSocket openConnection() {
        BluetoothSocket bluetoothSocket;
        try {
            bluetoothSocket = mDevice.createRfcommSocketToServiceRecord(BluetoothGateway.SERVICE_UUID);
        } catch (IOException e) {
            return null;
        }
        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            if (bluetoothSocket != null){
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {}
            }
            return null;
        }
        return bluetoothSocket;
    }
}
