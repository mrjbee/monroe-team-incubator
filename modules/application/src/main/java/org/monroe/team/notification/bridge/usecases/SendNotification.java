package org.monroe.team.notification.bridge.usecases;

import org.monroe.team.notification.bridge.entities.Notification;

public class SendNotification {

    private final Gateway mGateway;

    public SendNotification(Gateway gateway) {
        mGateway = gateway;
    }

    public void doSend(Notification notification){

    }


    public static interface Gateway{
        public void doSend(Notification notification);
    }

}
