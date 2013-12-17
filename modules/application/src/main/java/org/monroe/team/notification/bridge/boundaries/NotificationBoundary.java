package org.monroe.team.notification.bridge.boundaries;

import java.util.Map;

public interface NotificationBoundary {

    public interface Required{
        public void sendNotification(RemoteClientBoundary.RemoteClient client, Notification... notification);
    }

    public interface Declare{
        public void onNotificationSendSuccess(Notification notification, RemoteClientBoundary.RemoteClient client);
        public void onNotificationSendFails(Notification notification, RemoteClientBoundary.RemoteClient client);
        public void onInternalNotification(Map<String, String> notificationBody);
        public void onExternalNotification(Notification notification);
    }


    public interface Notification {
        public String getMessageId();
        public String getOwner();
        public Map<String,String> getBody();
    }

}
