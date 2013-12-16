package org.monroe.team.notification.bridge.boundaries;

import java.util.Map;

public interface NotificationBoundary {

    //Dedicated to be used inside user case
    public void send(RemoteClientBoundary.RemoteClient client, Notification... notification);

    //Dedicated to be used outside user case
    public void onSendSuccess(Notification notification, RemoteClientBoundary.RemoteClient client);
    public void onSendFails(Notification notification, RemoteClientBoundary.RemoteClient client);
    public void onInternal(Notification notification);
    public void onExternal(Notification notification);

    public interface Notification {
        public String getMessageId();
        public String getOwner();
        public Map<String,String> getBody();
    }

}
