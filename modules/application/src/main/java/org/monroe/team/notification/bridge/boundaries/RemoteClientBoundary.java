package org.monroe.team.notification.bridge.boundaries;

public interface RemoteClientBoundary {

    public interface Required {
        public RemoteClient[] getAvailableClients();
    }

    public static interface RemoteClient {
        public String getClientId();
    }
}
