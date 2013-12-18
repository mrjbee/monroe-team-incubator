package org.monroe.team.notification.bridge.android.delivery.bluetooth;

import org.monroe.team.notification.bridge.common.IdAwareData;

import java.io.Serializable;

/**
 * User: MisterJBee
 * Date: 12/16/13 Time: 1:00 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class BluetoothExchange implements Serializable{

    private final IdAwareData body;

    public BluetoothExchange(IdAwareData body) {
        this.body = body;
    }

    public IdAwareData getBody() {
        return body;
    }

    public String getId(){
        return body.getId();
    }

    @Override
    public String toString() {
        return "BluetoothExchange{" +
                "body=" + body +
                '}';
    }
}
