package org.monroe.team.notification.bridge.usecases;


public class UserCasesRouter {

    private final UserCasesContext mCasesContext;

    public UserCasesRouter(UserCasesContext casesContext) {
        mCasesContext = casesContext;
    }

    public void initialize(){
        mCasesContext.startup();
    }

}
