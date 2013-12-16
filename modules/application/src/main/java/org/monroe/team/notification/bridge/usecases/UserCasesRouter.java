package org.monroe.team.notification.bridge.usecases;


import org.monroe.team.notification.bridge.boundaries.NotificationBoundary;

import java.util.Map;

public class UserCasesRouter {

    private final UserCasesContext mCasesContext;

    public UserCasesRouter(UserCasesContext casesContext) {
        mCasesContext = casesContext;
    }

    public void initialize(){
        mCasesContext.startup();
    }

    public void sendTestMessage(Map<String, String> notificationBody) {
        mCasesContext.getBoundary(NotificationBoundary.class).onInternal(notificationBody);
    }
}
