package org.monroe.team.notification.bridge.android.connectivity;

import android.bluetooth.BluetoothSocket;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 10:14 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class BluetoothClientPool {


    public BluetoothClient getForIncomingClient(BluetoothSocket clientSocket) {
        BluetoothClient client = new BluetoothClient();
        client.init(clientSocket);
        return null;
    }
}
