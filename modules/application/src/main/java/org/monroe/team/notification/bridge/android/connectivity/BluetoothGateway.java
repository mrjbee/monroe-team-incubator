package org.monroe.team.notification.bridge.android.connectivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

/**
 * User: MisterJBee
 * Date: 12/15/13 Time: 7:14 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class BluetoothGateway {

    private final BluetoothAdapter mBluetoothAdapter;

    public BluetoothGateway(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    public boolean isSupported() {
        return mBluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }
}
