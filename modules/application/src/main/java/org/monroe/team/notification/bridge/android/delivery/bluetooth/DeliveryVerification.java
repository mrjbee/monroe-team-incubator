package org.monroe.team.notification.bridge.android.delivery.bluetooth;

import org.monroe.team.notification.bridge.common.IdAwareData;

/**
 * User: MisterJBee
 * Date: 12/16/13 Time: 1:09 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public class DeliveryVerification implements IdAwareData {

    private final String id;

    public DeliveryVerification(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
