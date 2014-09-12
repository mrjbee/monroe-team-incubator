package org.monroe.team.smooker.app.uc;

import org.monroe.team.smooker.app.common.Preferences;
import org.monroe.team.smooker.app.common.Registry;
import org.monroe.team.smooker.app.common.SetupPage;
import org.monroe.team.smooker.app.dp.DAO;
import org.monroe.team.smooker.app.dp.TransactionManager;
import org.monroe.team.smooker.app.uc.common.UserCaseSupport;

import java.util.ArrayList;
import java.util.List;

public class CalculateRequiredSetupPages extends UserCaseSupport<Void,CalculateRequiredSetupPages.RequiredSetupResponse> {

    public CalculateRequiredSetupPages(Registry registry) {
        super(registry);
    }

    @Override
    public RequiredSetupResponse execute(Void request) {

        final List<SetupPage> answer = new ArrayList<SetupPage>(4);
        boolean required = false;

        if(using(Preferences.class).isFirstStart()){
            answer.add(SetupPage.WELCOME_PAGE);
            using(Preferences.class).markAsFirstStartDone();
            required = true;
        }

        if (isSmokePerDayUndefined()){
            answer.add(SetupPage.SMOKE_PER_DAYS);
            required = true;
        }

        if (!isSmokePerDayUndefined() && !using(Preferences.class).isQuitProgramSuggested()){
            using(TransactionManager.class).execute(new TransactionManager.TransactionAction<Object>() {
                @Override
                public Object execute(DAO dao) {
                    DAO.Result result = dao.getFirstLoggedSmoke();
                    //TODO: add date logic
                    if (result != null) {
                        answer.add(SetupPage.QUIT_SMOKING);
                        using(Preferences.class).markAsQuitProgramSuggested();
                    }
                    return null;
                }
            });
        }

        return new RequiredSetupResponse(answer,required);
    }

    private boolean isSmokePerDayUndefined() {
        return using(Preferences.class).getSmokePerDay() == GetGeneralDetails.GeneralDetailsResponse.SMOKE_PER_DAY_UNDEFINED;
    }

    public static class RequiredSetupResponse{

        public final List<SetupPage> setupPageList;
        public final boolean force;

        public RequiredSetupResponse(List<SetupPage> setupPageList, boolean force) {
            this.setupPageList = setupPageList;
            this.force = force;
        }
    }

}
