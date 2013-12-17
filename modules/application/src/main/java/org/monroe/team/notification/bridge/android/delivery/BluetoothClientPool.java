package org.monroe.team.notification.bridge.android.delivery;

import android.bluetooth.BluetoothSocket;

import java.util.ArrayList;
import java.util.List;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 10:14 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class BluetoothClientPool {

    private List<BluetoothClient> mClientPool = new ArrayList<BluetoothClient>(5);

    public BluetoothClient getForIncomingClient(BluetoothSocket clientSocket) {
        BluetoothClient client = getCachedClient();
        client.init(clientSocket);
        return client;
    }

    public synchronized void releaseClient(BluetoothClient bluetoothClient){
        bluetoothClient.setBluetoothClientListener(null);
        bluetoothClient.release();
        if(mClientPool.size()<5){
            mClientPool.add(bluetoothClient);
        }
    }

    private synchronized BluetoothClient getCachedClient(){
        if (mClientPool.isEmpty()){
            return new BluetoothClient();
        } else {
            return mClientPool.remove(0);
        }
    }

}
