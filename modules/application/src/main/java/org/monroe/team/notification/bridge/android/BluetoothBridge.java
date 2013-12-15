package org.monroe.team.notification.bridge.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import org.monroe.team.libdroid.logging.Debug;

import java.util.Set;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 5:14 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class BluetoothBridge {

    private final BluetoothAdapter mBluetoothAdapter;

    public BluetoothBridge(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setEnable(boolean enable) {
        if (enable){
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                Debug.i("Paired device name = %s class = %s address = %s", device.getName(),
                        device.getBluetoothClass(),
                        device.getAddress() );
            }

        }
    }
}
