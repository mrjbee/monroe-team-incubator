package org.monroe.team.notification.bridge.android.delivery.socket;

import java.io.Serializable;

/**
 * User: MisterJBee
 * Date: 12/28/13 Time: 12:06 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface SocketMessage<BodyType> extends Serializable {
    String getId();
    BodyType getBody();
}
