package org.monroe.team.notification.bridge.android.delivery.socket;

/**
 * User: MisterJBee
 * Date: 12/28/13 Time: 1:37 AM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface SocketClientFactory<DestinationDescriptor> {
    public SocketClient getBy(DestinationDescriptor destinationDescriptor);
}
