package org.monroe.team.notification.bridge.boundaries;

import java.util.Map;

public interface NotificationBoundary {

    public interface Required{
        public void send(RemoteClientBoundary.RemoteClient client, Notification... notification);
    }

    public interface Declare{
        public void onSendSuccess(Notification notification, RemoteClientBoundary.RemoteClient client);
        public void onSendFails(Notification notification, RemoteClientBoundary.RemoteClient client);
        public void onInternal(Map<String,String> notificationBody);
        public void onExternal(Notification notification);
    }


    public interface Notification {
        public String getMessageId();
        public String getOwner();
        public Map<String,String> getBody();
    }

}
