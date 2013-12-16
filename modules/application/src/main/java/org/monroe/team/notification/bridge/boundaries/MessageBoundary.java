package org.monroe.team.notification.bridge.boundaries;

import java.util.Map;

public interface MessageBoundary {

    //Dedicated to be used inside user case
    public void sendMessage(Message message, RemoteClientBoundary.RemoteClient client);

    //Dedicated to be used outside user case
    public void onMessageSendSuccess(Message message, RemoteClientBoundary.RemoteClient client);
    public void onMessageSendFails(Message message, RemoteClientBoundary.RemoteClient client);
    public void onInternalMessage(Message message);
    public void onExternalMessage(Message message);

    public interface Message{
        public String getMessageId();
        public String getOwner();
        public Map<String,String> getBody();
    }

}
