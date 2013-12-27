package org.monroe.team.notification.bridge.android.delivery.socket;

import java.io.*;

/**
 * User: MisterJBee
 * Date: 12/26/13 Time: 11:08 PM
 * Open source: MIT Licence
 * (Do whatever you want with the source code)
 */
public interface SocketClient {
    ObjectInputStream getInputStream() throws IOException;
    ObjectOutputStream getOutputStream() throws IOException;
    boolean isConnectionOwner();
    void init();
    void deInit();
}
