package org.monroe.team.notification.bridge.boundaries;

import org.monroe.team.notification.bridge.common.IdAwareData;

import java.util.Date;
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


    public interface Notification extends IdAwareData {
        public String getOwner();
        public Map<String,String> getBody();
        public Date getCreationDate();
    }

}
