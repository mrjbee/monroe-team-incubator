package org.monroe.team.notification.bridge.usecases;

import org.monroe.team.libdroid.commons.Should;
import org.monroe.team.notification.bridge.boundaries.NotificationBoundary;
import org.monroe.team.notification.bridge.boundaries.RemoteClientBoundary;
import org.monroe.team.notification.bridge.usecases.common.UseCaseContext;

import java.util.Map;

final public class UseCaseSetup implements NotificationBoundary.Declare {

    public static void define(UseCaseContext useCaseContext) {
        UseCaseSetup caseSetup = new UseCaseSetup(useCaseContext);
        useCaseContext.installBoundary(caseSetup, NotificationBoundary.Declare.class);
    }

    private final SendInternalNotification mSendInternalNotification;

    public UseCaseSetup(UseCaseContext useCaseContext) {
        mSendInternalNotification = new SendInternalNotification(useCaseContext);
    }

    @Override
    public void onInternalNotification(Map<String, String> notificationBody) {
        mSendInternalNotification.perform(notificationBody);
    }

    @Override
    public void onNotificationSendSuccess(NotificationBoundary.Notification notification, RemoteClientBoundary.RemoteClient client) {
        Should.failsHere("Not implemented yet.");
    }

    @Override
    public void onNotificationSendFails(NotificationBoundary.Notification notification, RemoteClientBoundary.RemoteClient client) {
        //Should.beImplemented("Not implemented yet.");
    }



    @Override
    public void onExternalNotification(NotificationBoundary.Notification notification) {
        Should.failsHere("Not implemented yet.");
    }
}
