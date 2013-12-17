package org.monroe.team.notification.bridge.usecases;

import org.monroe.team.notification.bridge.boundaries.DeviceNameBoundary;
import org.monroe.team.notification.bridge.boundaries.NotificationBoundary;
import org.monroe.team.notification.bridge.boundaries.RemoteClientBoundary;
import org.monroe.team.notification.bridge.boundaries.entries.DefaultNotification;
import org.monroe.team.notification.bridge.strategies.DateStrategy;
import org.monroe.team.notification.bridge.strategies.IdGeneratorStrategy;
import org.monroe.team.notification.bridge.usecases.common.AbstractUseCase;
import org.monroe.team.notification.bridge.usecases.common.UseCaseContext;

import java.util.Date;
import java.util.Map;

public class SendInternalNotification extends AbstractUseCase <Void, Map<String,String>> {

    public SendInternalNotification(UseCaseContext caseContext) {
        super(caseContext);
    }

    @Override
    public void perform(Map<String, String> msgBody) {

        Date nowDate = strategy(DateStrategy.class).getNow();
        String ownerId = boundary(DeviceNameBoundary.Required.class).getDeviceName();
        String msgId = strategy(IdGeneratorStrategy.class).generateId(ownerId);
        DefaultNotification notification = new DefaultNotification(ownerId,msgId,msgBody,nowDate);

        RemoteClientBoundary.RemoteClient[] remoteClients = boundary(RemoteClientBoundary.Required.class).getClientsToNotify();
        for (RemoteClientBoundary.RemoteClient remoteClient : remoteClients) {
            boundary(NotificationBoundary.Required.class).sendNotification(remoteClient, notification);
        }

    }

}
