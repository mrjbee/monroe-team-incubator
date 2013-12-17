package org.monroe.team.notification.bridge.android.delivery;

/**
 * User: MisterJBee
 * Date: 12/16/13 Time: 1:09 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class MessageVerification implements BluetoothExchange.IdAware {

    private final String id;

    public MessageVerification(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
