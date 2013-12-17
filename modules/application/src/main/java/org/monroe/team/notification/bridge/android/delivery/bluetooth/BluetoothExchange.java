package org.monroe.team.notification.bridge.android.delivery.bluetooth;

/**
 * User: MisterJBee
 * Date: 12/16/13 Time: 1:00 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class BluetoothExchange {

    private final IdAware body;

    public BluetoothExchange(IdAware body) {
        this.body = body;
    }

    public IdAware getBody() {
        return body;
    }

    public String getId(){
        return body.getId();
    }

    public static interface IdAware {
        public String getId();
    }
}
