package org.monroe.team.notification.bridge.boundaries;

import java.util.List;

public interface RemoteClientBoundary {

    public RemoteClient[] getAvailableClients();

    public static interface RemoteClient {
        public String getClientId();
    }
}
